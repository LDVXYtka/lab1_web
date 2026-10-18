package ru.itmo.students.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.students.dto.StudentFilter;
import ru.itmo.students.dto.StudentPage;
import ru.itmo.students.dto.StudentPatchRequest;
import ru.itmo.students.dto.StudentRequest;
import ru.itmo.students.dto.StudentResponse;
import ru.itmo.students.exception.BadRequestException;
import ru.itmo.students.exception.ConflictException;
import ru.itmo.students.exception.NotFoundException;
import ru.itmo.students.exception.ValidationException;
import ru.itmo.students.jooq.tables.records.StudentsRecord;
import ru.itmo.students.repository.StudentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private static final List<String> SORT_FIELDS = List.of(
            "id", "name", "group", "isu", "dormitory", "room", "term", "international");
    private static final LocalDate MIN_TERM = LocalDate.of(1970, 1, 1);

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public StudentPage findStudents(StudentFilter filter, int page, int size, String sort, String order) {
        if (page < 1) {
            throw new BadRequestException("Параметр page должен быть не меньше 1");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Параметр size должен быть от 1 до 100");
        }
        if (!SORT_FIELDS.contains(sort)) {
            throw new BadRequestException("Сортировать можно по полям: " + String.join(", ", SORT_FIELDS));
        }
        if (!order.equals("asc") && !order.equals("desc")) {
            throw new BadRequestException("Параметр order должен быть asc или desc");
        }

        List<StudentResponse> items = new ArrayList<>();
        for (StudentsRecord student : repository.findPage(filter, sort, order, page, size)) {
            items.add(toResponse(student));
        }
        return new StudentPage(items, repository.count(filter), page, size);
    }

    public StudentResponse findStudent(long id) {
        return toResponse(getExisting(id));
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        checkTerm(request.term());
        if (repository.existsByIsu(request.isu())) {
            throw isuTaken();
        }
        StudentsRecord student = new StudentsRecord();
        fill(student, request);
        StudentsRecord saved = repository.insert(student);
        log.info("Создан студент id={} isu={}", saved.getId(), saved.getIsu());
        return toResponse(saved);
    }

    @Transactional
    public StudentResponse replaceStudent(long id, StudentRequest request) {
        getExisting(id);
        checkTerm(request.term());
        if (repository.existsByIsuAndIdNot(request.isu(), id)) {
            throw isuTaken();
        }
        StudentsRecord student = new StudentsRecord();
        fill(student, request);
        StudentsRecord saved = repository.update(id, student);
        log.info("Полностью изменен студент id={}", id);
        return toResponse(saved);
    }

    @Transactional
    public StudentResponse updateStudent(long id, StudentPatchRequest request) {
        StudentsRecord student = getExisting(id);
        if (request.name() != null) {
            student.setName(request.name());
        }
        if (request.group() != null) {
            student.setGroupName(request.group());
        }
        if (request.isu() != null) {
            if (repository.existsByIsuAndIdNot(request.isu(), id)) {
                throw isuTaken();
            }
            student.setIsu(request.isu());
        }
        if (request.dormitory() != null) {
            student.setDormitory(request.dormitory());
        }
        if (request.room() != null) {
            student.setRoom(request.room());
        }
        if (request.term() != null) {
            checkTerm(request.term());
            student.setTerm(request.term());
        }
        if (request.international() != null) {
            student.setInternational(request.international());
        }
        if (request.note() != null) {
            student.setNote(request.note());
        }
        if (!student.touched()) {
            return toResponse(student);
        }
        StudentsRecord saved = repository.update(id, student);
        log.info("Частично изменен студент id={}", id);
        return toResponse(saved);
    }

    @Transactional
    public void deleteStudent(long id) {
        if (repository.delete(id) == 0) {
            throw new NotFoundException("Студент не найден");
        }
        log.info("Удален студент id={}", id);
    }

    private StudentsRecord getExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Студент не найден"));
    }

    private void checkTerm(LocalDate term) {
        LocalDate maxTerm = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        if (term.isBefore(MIN_TERM) || term.isAfter(maxTerm)) {
            throw new ValidationException("Проверьте поля формы",
                    Map.of("term", "Дата заселения от " + MIN_TERM + " до " + maxTerm));
        }
    }

    private ConflictException isuTaken() {
        return new ConflictException("Студент с таким ИСУ уже есть",
                Map.of("isu", "Этот ИСУ уже занят другим студентом"));
    }

    private void fill(StudentsRecord student, StudentRequest request) {
        student.setName(request.name());
        student.setGroupName(request.group());
        student.setIsu(request.isu());
        student.setDormitory(request.dormitory());
        student.setRoom(request.room());
        student.setTerm(request.term());
        student.setInternational(request.international());
        if (request.note() == null) {
            student.setNote("");
        } else {
            student.setNote(request.note());
        }
    }

    private StudentResponse toResponse(StudentsRecord student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getGroupName(),
                student.getIsu(),
                student.getDormitory(),
                student.getRoom(),
                student.getTerm(),
                student.getInternational(),
                student.getNote());
    }
}
