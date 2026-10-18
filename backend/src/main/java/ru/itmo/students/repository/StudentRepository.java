package ru.itmo.students.repository;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import ru.itmo.students.jooq.tables.records.StudentsRecord;

import java.util.List;
import java.util.Optional;

import static ru.itmo.students.jooq.Tables.STUDENTS;

@Repository
public class StudentRepository {

    private final DSLContext dsl;

    public StudentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<StudentsRecord> findPage(StudentFilter filter, String sort, String order, int page, int size) {
        return dsl.selectFrom(STUDENTS)
                .where(toCondition(filter))
                .orderBy(toSortField(sort, order), STUDENTS.ID.asc())
                .limit(size)
                .offset((page - 1) * size)
                .fetch();
    }

    public int count(StudentFilter filter) {
        return dsl.fetchCount(STUDENTS, toCondition(filter));
    }

    public Optional<StudentsRecord> findById(long id) {
        return dsl.selectFrom(STUDENTS)
                .where(STUDENTS.ID.eq(id))
                .fetchOptional();
    }

    public boolean existsByIsu(String isu) {
        return dsl.fetchExists(STUDENTS, STUDENTS.ISU.eq(isu));
    }

    public boolean existsByIsuAndIdNot(String isu, long id) {
        return dsl.fetchExists(STUDENTS, STUDENTS.ISU.eq(isu).and(STUDENTS.ID.ne(id)));
    }

    public StudentsRecord insert(StudentsRecord student) {
        return dsl.insertInto(STUDENTS)
                .set(student)
                .returning()
                .fetchOne();
    }

    public StudentsRecord update(long id, StudentsRecord student) {
        return dsl.update(STUDENTS)
                .set(student)
                .where(STUDENTS.ID.eq(id))
                .returning()
                .fetchOne();
    }

    public int delete(long id) {
        return dsl.deleteFrom(STUDENTS)
                .where(STUDENTS.ID.eq(id))
                .execute();
    }

    private Condition toCondition(StudentFilter filter) {
        Condition condition = DSL.noCondition();
        if (filter.name() != null) {
            condition = condition.and(STUDENTS.NAME.containsIgnoreCase(filter.name()));
        }
        if (filter.group() != null) {
            condition = condition.and(STUDENTS.GROUP_NAME.eq(filter.group()));
        }
        if (filter.isu() != null) {
            condition = condition.and(STUDENTS.ISU.eq(filter.isu()));
        }
        if (filter.dormitory() != null) {
            condition = condition.and(STUDENTS.DORMITORY.eq(filter.dormitory()));
        }
        if (filter.room() != null) {
            condition = condition.and(STUDENTS.ROOM.eq(filter.room()));
        }
        if (filter.termFrom() != null) {
            condition = condition.and(STUDENTS.TERM.ge(filter.termFrom()));
        }
        if (filter.termTo() != null) {
            condition = condition.and(STUDENTS.TERM.le(filter.termTo()));
        }
        if (filter.international() != null) {
            condition = condition.and(STUDENTS.INTERNATIONAL.eq(filter.international()));
        }
        return condition;
    }

    private SortField<?> toSortField(String sort, String order) {
        Field<?> field = switch (sort) {
            case "name" -> STUDENTS.NAME;
            case "group" -> STUDENTS.GROUP_NAME;
            case "isu" -> STUDENTS.ISU;
            case "dormitory" -> STUDENTS.DORMITORY;
            case "room" -> STUDENTS.ROOM;
            case "term" -> STUDENTS.TERM;
            case "international" -> STUDENTS.INTERNATIONAL;
            default -> STUDENTS.ID;
        };
        if (order.equals("desc")) {
            return field.desc();
        }
        return field.asc();
    }
}
