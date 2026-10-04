# Лабораторная 2: управление студентами общежития

- `backend` — сервер на Flask: REST API и раздача страниц
- `frontend` — страницы на HTML, CSS и JS из лабы 1, переделанные под работу с сервером

## Запуск

Из корня репозитория:

```
python -m venv .venv
.venv\Scripts\python -m pip install -r backend\requirements.txt
.venv\Scripts\python backend\app.py
```

После запуска открыть http://127.0.0.1:5000

## API

| Запрос | Что делает | Успешный ответ |
|---|---|---|
| `GET /api/requests?фильтры` | список студентов | 200 |
| `QUERY /api/requests` | список студентов, фильтры в теле (JSON) | 200 |
| `GET /api/requests/<id>` | один студент | 200 |
| `POST /api/requests` | добавить студента | 201 |
| `PATCH /api/requests/<id>` | изменить студента | 200 |
| `DELETE /api/requests/<id>` | удалить студента | 204 |

Фильтры: `name`, `group`, `isu`, `dormitory`, `room`, `term_from`, `term_to`, `international`.
Если фильтров больше трёх, фронт отправляет QUERY, иначе GET.

Ошибки приходят в одном формате: `{"status": 422, "message": "...", "fields": {"isu": "..."}}`.
Коды ошибок: 400 — неправильный запрос, 404 — нет студента или адреса, 405 — метод не поддерживается,
409 — ИСУ уже занят, 422 — поля не прошли проверку, 500 — ошибка на сервере.

Данные хранятся в `backend/students.json`.
