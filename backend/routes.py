from flask import Blueprint, request

import service
from service import ApiError

bp = Blueprint("students", __name__)


def read_body() -> dict:
    data = request.get_json(silent=True)
    if type(data) is not dict:
        raise ApiError(400, "В теле запроса нужен JSON-объект")
    return data


@bp.route("/api/requests", methods=["GET"])
def list_students():
    return service.get_students(request.args.to_dict())


@bp.route("/api/requests", methods=["QUERY"])
def query_students():
    return service.get_students(read_body())


@bp.route("/api/requests/<int:student_id>", methods=["GET"])
def get_student(student_id):
    return service.get_student(student_id)


@bp.route("/api/requests", methods=["POST"])
def create_student():
    return service.create_student(read_body()), 201


@bp.route("/api/requests/<int:student_id>", methods=["PATCH"])
def update_student(student_id):
    return service.update_student(student_id, read_body())


@bp.route("/api/requests/<int:student_id>", methods=["DELETE"])
def delete_student(student_id):
    service.delete_student(student_id)
    return "", 204
