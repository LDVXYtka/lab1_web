import json
import os

FILE_PATH = os.path.join(os.path.dirname(__file__), "students.json")


def read_file():
    if not os.path.exists(FILE_PATH):
        return []
    with open(FILE_PATH, "r", encoding="utf-8") as file:
        return json.load(file)


def save_file(students):
    with open(FILE_PATH, "w", encoding="utf-8") as file:
        json.dump(students, file, ensure_ascii=False, indent=2)


def get_all():
    return read_file()


def get_by_id(student_id):
    for student in read_file():
        if student["id"] == student_id:
            return student
    return None


def next_id():
    students = read_file()
    if len(students) == 0:
        return 1
    return max(student["id"] for student in students) + 1


def add(student):
    students = read_file()
    students.append(student)
    save_file(students)


def replace(student_id, new_student):
    students = read_file()
    for i in range(len(students)):
        if students[i]["id"] == student_id:
            students[i] = new_student
            save_file(students)
            return


def delete(student_id):
    students = read_file()
    for student in students:
        if student["id"] == student_id:
            students.remove(student)
            save_file(students)
            return
