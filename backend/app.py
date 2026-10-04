# Точка входа: создаёт приложение, подключает маршруты, раздаёт страницы и ловит ошибки

import json

from flask import Flask
from werkzeug.exceptions import HTTPException

from routes import bp
from service import ApiError

app = Flask(__name__, static_folder="../frontend", static_url_path="")
app.json.ensure_ascii = False
app.json.sort_keys = False
app.register_blueprint(bp)

HTTP_MESSAGES = {
    404: "Такого адреса нет",
    405: "Этот метод не поддерживается по этому адресу",
}


@app.route("/")
def index():
    return app.send_static_file("table.html")


@app.errorhandler(ApiError)
def handle_api_error(error):
    return {"status": error.status, "message": error.message, "fields": error.fields}, error.status


@app.errorhandler(HTTPException)
def handle_http_error(error):
    response = error.get_response()
    message = HTTP_MESSAGES.get(error.code, error.description)
    response.data = json.dumps({"status": error.code, "message": message, "fields": {}}, ensure_ascii=False)
    response.content_type = "application/json"
    return response


@app.errorhandler(Exception)
def handle_other_error(error):
    app.logger.exception(error)
    return {"status": 500, "message": "Ошибка на сервере", "fields": {}}, 500


if __name__ == "__main__":
    app.run(port=5000)
