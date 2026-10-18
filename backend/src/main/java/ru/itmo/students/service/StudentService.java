package ru.itmo.students.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.students.dto.StudentPage;
import ru.itmo.students.dto.StudentPatchRequest;
import ru.itmo.students.dto.StudentRequest;
import ru.itmo.students.dto.StudentResponse;
import ru.itmo.students.exception.BadRequestException;
import ru.itmo.students.exception.ConflictException;
import ru.itmo.students.exception.NotFoundException;
import ru.itmo.students.exception.ValidationException;
import ru.itmo.students.jooq.tables.records.StudentsRecord;
import ru.itmo.students.repository.StudentFilter;
import ru.itmo.students.repository.StudentRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private static final List<String> PARAMS = List.of(
            "name", "group", "isu", "dormitory", "room", "term_from", "term_to", "international",
            "page", "size", "sort", "order");
    private static final List<String> SORT_FIELDS = List.of(
            "id", "name", "group", "isu", "dormitory", "room", "term", "international");
    private static final LocalDate MIN_TERM = LocalDate.of(1970, 1, 1);

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public StudentPage findStudents(Map<String, String> params) {
        for (String key : params.keySet()) {
            if (!PARAMS.contains(key)) {
                throw new BadRequestException("Неизвестный параметр: " + key);
            }
        }

        String group = param(params, "group");
        if (group != null) {
            group = group.toUpperCase();
        }
        StudentFilter filter = new StudentFilter(
                param(params, "name"),
                group,
                param(params, "isu"),
                dormitoryParam(params),
                param(params, "room"),
                dateParam(params, "term_from"),
                dateParam(params, "term_to"),
                internationalParam(params));

        int page = numberParam(params, "page", 1, 1, 1_000_000);
        int size = numberParam(params, "size", 10, 1, 100);

        String sort = param(params, "sort");
        if (sort == null) {
            sort = "id";
        }
        if (!SORT_FIELDS.contains(sort)) {
            throw new BadRequestException("Сортировать можно по полям: " + String.join(", ", SORT_FIELDS));
        }
        String order = param(params, "order");
        if (order == null) {
            order = "asc";
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

    private String param(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Integer dormitoryParam(Map<String, String> params) {
        String value = param(params, "dormitory");
        if (value == null) {
            return null;
        }
        if (!value.matches("[1-8]")) {
            throw new BadRequestException("Фильтр dormitory должен быть числом от 1 до 8");
        }
        return Integer.parseInt(value);
    }

    private Boolean internationalParam(Map<String, String> params) {
        String value = param(params, "international");
        if (value == null) {
            return null;
        }
        if (!value.equals("true") && !value.equals("false")) {
            throw new BadRequestException("Фильтр international должен быть true или false");
        }
        return value.equals("true");
    }

    private LocalDate dateParam(Map<String, String> params, String key) {
        String value = param(params, key);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Фильтр " + key + " должен быть датой в формате ГГГГ-ММ-ДД");
        }
    }

    private int numberParam(Map<String, String> params, String key, int defaultValue, int min, int max) {
        String value = param(params, key);
        if (value == null) {
            return defaultValue;
        }
        String message = "Параметр " + key + " должен быть целым числом от " + min + " до " + max;
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(message);
        }
        if (number < min || number > max) {
            throw new BadRequestException(message);
        }
        return number;
    }
}
