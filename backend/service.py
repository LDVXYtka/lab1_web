# Бизнес-логика: проверки, уникальность ИСУ, фильтрация

import re
from datetime import date

import repository


class ApiError(Exception):
    def __init__(self, status: int, message: str, fields: dict | None = None):
        super().__init__(message)
        self.status = status
        self.message = message
        self.fields = fields or {}


FIELDS = ["name", "group", "isu", "dormitory", "room", "term", "international", "note"]
FILTERS = ["name", "group", "isu", "dormitory", "room", "term_from", "term_to", "international"]

NAME_PATTERN = r"[A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*(?: [A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*)+"
GROUP_PATTERN = r"[A-Z][34][1-4][0-9]{2}"
ISU_PATTERN = r"[1-9][0-9]{5}"
ROOM_PATTERN = r"(?:[1-9]|1[0-9]|20)(?:0[1-9]|[1-9][0-9])"


def is_date(value: str) -> bool:
    if not re.fullmatch(r"[0-9]{4}-[0-9]{2}-[0-9]{2}", value):
        return False
    try:
        date.fromisoformat(value)
    except ValueError:
        return False
    return True


def check_field(field: str, value) -> str:
    if field == "dormitory":
        if type(value) is not int:
            return "Номер общежития должен быть числом"
        if value < 1 or value > 8:
            return "Номер общежития от 1 до 8"
        return ""

    if field == "international":
        if type(value) is not bool:
            return "Нужно значение true или false"
        return ""

    if type(value) is not str:
        return "Значение должно быть строкой"

    if field == "name":
        if len(value) > 100:
            return "ФИО должно быть не длиннее 100 символов"
        if not re.fullmatch(NAME_PATTERN, value):
            return "Введите минимум фамилию и имя: только буквы, дефис и апостроф внутри слов"
    elif field == "group":
        if not re.fullmatch(GROUP_PATTERN, value):
            return "Группа: латинская заглавная буква, потом 3 или 4 и ещё три цифры, например M3301"
    elif field == "isu":
        if not re.fullmatch(ISU_PATTERN, value):
            return "ИСУ — 6 цифр, первая не 0"
    elif field == "room":
        if not re.fullmatch(ROOM_PATTERN, value):
            return "Комната: этаж от 1 до 20 и номер от 01 до 99, например 1203"
    elif field == "term":
        last_date = str(date.today().year) + "-12-31"
        if not is_date(value) or value < "1970-01-01" or value > last_date:
            return "Дата заселения от 1970-01-01 до " + last_date
    elif field == "note":
        if len(value) > 2000:
            return "Заметки не длиннее 2000 символов"
    return ""


def validate_student(data: dict, for_update: bool = False):
    errors = {}
    for field in FIELDS:
        if field in data:
            error = check_field(field, data[field])
            if error != "":
                errors[field] = error
        elif not for_update and field != "note":
            errors[field] = "Обязательное поле"
    if errors:
        raise ApiError(422, "Проверьте поля формы", errors)


def check_isu_is_free(isu: str):
    for student in repository.get_all():
        if student["isu"] == isu:
            raise ApiError(409, "Студент с таким ИСУ уже есть", {"isu": "Этот ИСУ уже занят другим студентом"})


def check_filters(filters: dict) -> dict:
    result = {}
    for key, value in filters.items():
        if key not in FILTERS:
            raise ApiError(400, "Неизвестный фильтр: " + key)
        if type(value) is not str:
            raise ApiError(400, "Значение фильтра " + key + " должно быть строкой")
        value = value.strip()
        if value == "":
            continue
        if key == "dormitory" and not re.fullmatch(r"[1-8]", value):
            raise ApiError(400, "Фильтр dormitory — число от 1 до 8")
        if key == "international" and value not in ("true", "false"):
            raise ApiError(400, "Фильтр international — true или false")
        if key in ("term_from", "term_to") and not is_date(value):
            raise ApiError(400, "Фильтр " + key + " — дата в формате ГГГГ-ММ-ДД")
        result[key] = value
    return result


def matches(student: dict, filters: dict) -> bool:
    if "name" in filters and filters["name"].lower() not in student["name"].lower():
        return False
    if "group" in filters and filters["group"].upper() != student["group"]:
        return False
    if "isu" in filters and filters["isu"] != student["isu"]:
        return False
    if "dormitory" in filters and int(filters["dormitory"]) != student["dormitory"]:
        return False
    if "room" in filters and filters["room"] != student["room"]:
        return False
    if "term_from" in filters and student["term"] < filters["term_from"]:
        return False
    if "term_to" in filters and student["term"] > filters["term_to"]:
        return False
    if "international" in filters and student["international"] != (filters["international"] == "true"):
        return False
    return True


def get_students(filters: dict) -> list:
    filters = check_filters(filters)
    result = []
    for student in repository.get_all():
        if matches(student, filters):
            result.append(student)
    return result


def get_student(student_id: int) -> dict:
    student = repository.get_by_id(student_id)
    if student is None:
        raise ApiError(404, "Студент не найден")
    return student


def create_student(data: dict) -> dict:
    validate_student(data)
    check_isu_is_free(data["isu"])
    student = {
        "id": repository.next_id(),
        "name": data["name"],
        "group": data["group"],
        "isu": data["isu"],
        "dormitory": data["dormitory"],
        "room": data["room"],
        "term": data["term"],
        "international": data["international"],
        "note": data.get("note", ""),
    }
    repository.add(student)
    return student


def update_student(student_id: int, data: dict) -> dict:
    student = get_student(student_id)
    validate_student(data, for_update=True)
    if "isu" in data and data["isu"] != student["isu"]:
        check_isu_is_free(data["isu"])
    updated = student.copy()
    for field in FIELDS:
        if field in data:
            updated[field] = data[field]
    repository.replace(student_id, updated)
    return updated


def delete_student(student_id: int):
    get_student(student_id)
    repository.delete(student_id)
