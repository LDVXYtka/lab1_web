create table users (
    id bigint generated always as identity primary key,
    username varchar(32) not null unique,
    email varchar(254) not null unique,
    password_hash varchar(60) not null,
    role varchar(5) not null check (role in ('ADMIN', 'USER'))
);
