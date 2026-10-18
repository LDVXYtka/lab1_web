create table refresh_tokens (
    id bigint generated always as identity primary key,
    user_id bigint not null references users (id) on delete cascade,
    token_hash varchar(64) not null unique,
    expires_at timestamp with time zone not null
);
