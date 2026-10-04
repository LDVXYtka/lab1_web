# Точка входа: создаёт приложение, подключает маршруты, раздаёт страницы и ловит ошибки

from flask import Flask

from routes import bp
from service import ApiError

app = Flask(__name__, static_folder="../frontend", static_url_path="")
# без этого русские буквы в JSON-ответе приходят кодами вида \u0421\u0442
app.json.ensure_ascii = False
app.register_blueprint(bp)


@app.route("/")
def index():
    return app.send_static_file("table.html")


@app.errorhandler(ApiError)
def handle_api_error(error):
    return {"status": error.status, "message": error.message, "fields": error.fields}, error.status


@app.errorhandler(404)
def handle_not_found(error):
    return {"status": 404, "message": "Такого адреса нет", "fields": {}}, 404


@app.errorhandler(405)
def handle_wrong_method(error):
    return {"status": 405, "message": "Этот метод не поддерживается по этому адресу", "fields": {}}, 405


@app.errorhandler(500)
def handle_server_error(error):
    return {"status": 500, "message": "Ошибка на сервере", "fields": {}}, 500


if __name__ == "__main__":
    app.run(port=5000)
