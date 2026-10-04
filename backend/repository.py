# Работа с данными: студенты лежат в памяти и в файле students.json

import json
import os

# файл лежит рядом с этим .py, так путь не зависит от папки, из которой запустили сервер
FILE_PATH = os.path.join(os.path.dirname(__file__), "students.json")


def read_file() -> list:
    if not os.path.exists(FILE_PATH):
        return []
    with open(FILE_PATH, "r", encoding="utf-8") as file:
        return json.load(file)


def save_file():
    with open(FILE_PATH, "w", encoding="utf-8") as file:
        json.dump(students, file, ensure_ascii=False, indent=2)


students = read_file()


def get_all() -> list:
    return students


def get_by_id(student_id: int) -> dict | None:
    for student in students:
        if student["id"] == student_id:
            return student
    return None


def next_id() -> int:
    if len(students) == 0:
        return 1
    return max(student["id"] for student in students) + 1


def add(student: dict):
    students.append(student)
    save_file()


def replace(student_id: int, new_student: dict):
    for i in range(len(students)):
        if students[i]["id"] == student_id:
            students[i] = new_student
            save_file()
            return


def delete(student_id: int):
    for student in students:
        if student["id"] == student_id:
            students.remove(student)
            save_file()
            return
