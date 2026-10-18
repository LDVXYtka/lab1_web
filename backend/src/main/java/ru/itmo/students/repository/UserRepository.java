package ru.itmo.students.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.itmo.students.jooq.tables.records.UsersRecord;

import java.util.Optional;

import static ru.itmo.students.jooq.Tables.USERS;

@Repository
public class UserRepository {

    private final DSLContext dsl;

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<UsersRecord> findById(long id) {
        return dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional();
    }

    public Optional<UsersRecord> findByUsername(String username) {
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptional();
    }

    public boolean existsByUsername(String username) {
        return dsl.fetchExists(USERS, USERS.USERNAME.eq(username));
    }

    public boolean existsByEmail(String email) {
        return dsl.fetchExists(USERS, USERS.EMAIL.eq(email));
    }

    public UsersRecord insert(String username, String email, String passwordHash, String role) {
        return dsl.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.EMAIL, email)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .set(USERS.ROLE, role)
                .returning()
                .fetchOne();
    }
}
