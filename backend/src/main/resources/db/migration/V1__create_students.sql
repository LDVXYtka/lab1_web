create table students (
    id bigint generated always as identity primary key,
    name varchar(100) not null,
    group_name varchar(5) not null,
    isu varchar(6) not null unique,
    dormitory integer not null check (dormitory between 1 and 8),
    room varchar(4) not null,
    term date not null,
    international boolean not null,
    note varchar(2000) not null default ''
);
