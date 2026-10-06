import re
import threading
from datetime import date

import repository


class ApiError(Exception):
    def __init__(self, status, message, fields=None):
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

RULES = {
    "name": (NAME_PATTERN, "Введите минимум фамилию и имя: только буквы, дефис и апостроф внутри слов"),
    "group": (GROUP_PATTERN, "Группа: латинская заглавная буква, потом 3 или 4 и еще три цифры, например M3301"),
    "isu": (ISU_PATTERN, "ИСУ должен состоять из 6 цифр, первая не 0"),
    "room": (ROOM_PATTERN, "Комната: этаж от 1 до 20 и номер от 01 до 99, например 1203"),
}

lock = threading.Lock()


def is_date(value):
    if not re.fullmatch(r"[0-9]{4}-[0-9]{2}-[0-9]{2}", value):
        return False
    try:
        date.fromisoformat(value)
    except ValueError:
        return False
    return True


def check_field(field, value):
    if field == "dormitory":
        if type(value) is not int:
            return "Номер общежития должен быть числом"
        if not 1 <= value <= 8:
            return "Номер общежития от 1 до 8"
        return ""

    if field == "international":
        if type(value) is not bool:
            return "Нужно значение true или false"
        return ""

    if type(value) is not str:
        return "Значение должно быть строкой"
    if field == "name" and len(value) > 100:
        return "ФИО должно быть не длиннее 100 символов"
    if field == "note" and len(value) > 2000:
        return "Заметки не длиннее 2000 символов"
    if field == "term" and not (is_date(value) and "1970-01-01" <= value <= "2026-12-31"):
        return "Дата заселения от 1970-01-01 до 2026-12-31"
    if field in RULES:
        pattern, message = RULES[field]
        if not re.fullmatch(pattern, value):
            return message
    return ""


def validate_student(data, for_update=False):
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


def check_isu_is_free(isu):
    for student in repository.get_all():
        if student["isu"] == isu:
            raise ApiError(409, "Студент с таким ИСУ уже есть", {"isu": "Этот ИСУ уже занят другим студентом"})


def check_filters(filters):
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
            raise ApiError(400, "Фильтр dormitory должен быть числом от 1 до 8")
        if key == "international" and value not in ("true", "false"):
            raise ApiError(400, "Фильтр international должен быть true или false")
        if key in ("term_from", "term_to") and not is_date(value):
            raise ApiError(400, "Фильтр " + key + " должен быть датой в формате ГГГГ-ММ-ДД")
        result[key] = value
    return result


def matches(student, filters):
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


def get_students(filters):
    filters = check_filters(filters)
    result = []
    for student in repository.get_all():
        if matches(student, filters):
            result.append(student)
    return result


def get_student(student_id):
    student = repository.get_by_id(student_id)
    if student is None:
        raise ApiError(404, "Студент не найден")
    return student


def create_student(data):
    validate_student(data)
    with lock:
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


def update_student(student_id, data):
    with lock:
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


def delete_student(student_id):
    with lock:
        get_student(student_id)
        repository.delete(student_id)
