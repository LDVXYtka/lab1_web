# Лабораторная работа 4

Vue 3, RxJS, Spring Boot, jOOQ, PostgreSQL.

## Запуск

1. Запустить Docker Desktop и открыть терминал в папке проекта.
2. При первом запуске создать `.env` по корневому `.env.example` и заполнить три обязательных значения: `DB_PASSWORD`, `JWT_SECRET` и `ADMIN_PASSWORD`. Остальные настройки уже заданы в Compose. JWT_SECRET должен содержать минимум 32 ASCII-символа, пароль администратора — от 8 до 72 ASCII-символов без пробелов.
3. Собрать и запустить приложение:

```sh
docker compose up --build
```

Сайт: http://localhost:8080. Swagger: http://localhost:8080/swagger-ui/index.html.

Администратор создается из настроек `.env`, регистрация на сайте создает USER. После первой сборки группу проекта можно запускать и останавливать кнопками Docker Desktop.

## Остановка

```sh
docker compose stop
```

Данные PostgreSQL сохраняются в volume `postgres_data`. Файл `.env` в git не добавляется.

## Bruno

Импортировать папку `bruno`, выбрать окружение `local`. В Secrets задать `adminPassword` и `userPassword`. Запускать запросы по порядку папок, начиная с CSRF. Сохранение и отправка cookies должны быть включены.
