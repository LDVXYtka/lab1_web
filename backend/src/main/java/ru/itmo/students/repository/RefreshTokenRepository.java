package ru.itmo.students.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.itmo.students.jooq.tables.records.RefreshTokensRecord;

import java.time.OffsetDateTime;
import java.util.Optional;

import static ru.itmo.students.jooq.Tables.REFRESH_TOKENS;

@Repository
public class RefreshTokenRepository {

    private final DSLContext dsl;

    public RefreshTokenRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(long userId, String tokenHash, OffsetDateTime expiresAt) {
        dsl.insertInto(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.USER_ID, userId)
                .set(REFRESH_TOKENS.TOKEN_HASH, tokenHash)
                .set(REFRESH_TOKENS.EXPIRES_AT, expiresAt)
                .execute();
    }

    public Optional<RefreshTokensRecord> findValid(String tokenHash) {
        return dsl.selectFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.TOKEN_HASH.eq(tokenHash))
                .and(REFRESH_TOKENS.EXPIRES_AT.gt(OffsetDateTime.now()))
                .fetchOptional();
    }

    public void delete(long id) {
        dsl.deleteFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.ID.eq(id))
                .execute();
    }
}
