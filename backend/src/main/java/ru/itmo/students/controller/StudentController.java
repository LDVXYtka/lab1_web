package ru.itmo.students.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.students.dto.StudentFilter;
import ru.itmo.students.dto.StudentPage;
import ru.itmo.students.dto.StudentPatchRequest;
import ru.itmo.students.dto.StudentQuery;
import ru.itmo.students.dto.StudentRequest;
import ru.itmo.students.dto.StudentResponse;
import ru.itmo.students.exception.BadRequestException;
import ru.itmo.students.service.StudentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Студенты")
public class StudentController {

    private static final List<String> QUERY_PARAMETERS = List.of(
            "id", "name", "group", "isu", "dormitory", "room", "term_from", "term_to", "international", "note",
            "page", "size", "sort", "order");

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Список студентов с фильтрами, сортировкой и пагинацией")
    public StudentPage list(@RequestParam(required = false) Long id,
                            @RequestParam(required = false) String name,
                            @RequestParam(required = false) String group,
                            @RequestParam(required = false) String isu,
                            @RequestParam(required = false) Integer dormitory,
                            @RequestParam(required = false) String room,
                            @RequestParam(name = "term_from", required = false) LocalDate termFrom,
                            @RequestParam(name = "term_to", required = false) LocalDate termTo,
                            @RequestParam(required = false) Boolean international,
                            @RequestParam(required = false) String note,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "id") String sort,
                            @RequestParam(defaultValue = "asc") String order,
                            HttpServletRequest httpRequest) {
        for (String parameter : httpRequest.getParameterMap().keySet()) {
            if (!QUERY_PARAMETERS.contains(parameter)) {
                throw new BadRequestException("Неизвестный параметр: " + parameter);
            }
        }
        String internationalValue = httpRequest.getParameter("international");
        if (internationalValue != null && !internationalValue.isBlank()
                && !List.of("true", "false").contains(internationalValue.trim())) {
            throw new BadRequestException("Фильтр international должен быть true или false");
        }
        StudentFilter filter = new StudentFilter(id, name, group, isu, dormitory, room, termFrom, termTo,
                international, note);
        return service.findStudents(filter, page, size, sort, order);
    }

    @RequestMapping
    @Hidden
    public StudentPage query(HttpServletRequest httpRequest, @RequestBody(required = false) StudentQuery query)
            throws HttpRequestMethodNotSupportedException {
        if (!httpRequest.getMethod().equals("QUERY")) {
            throw new HttpRequestMethodNotSupportedException(httpRequest.getMethod());
        }

        if (query == null) {
            throw new BadRequestException("Передайте фильтры в теле запроса");
        }
        StudentFilter filter = new StudentFilter(query.id(), query.name(), query.group(), query.isu(), query.dormitory(),
                query.room(), query.termFrom(), query.termTo(), query.international(), query.note());
        int page = query.page() == null ? 1 : query.page();
        int size = query.size() == null ? 10 : query.size();
        String sort = query.sort() == null ? "id" : query.sort();
        String order = query.order() == null ? "asc" : query.order();
        return service.findStudents(filter, page, size, sort, order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Один студент")
    public StudentResponse get(@PathVariable long id) {
        return service.findStudent(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать студента")
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return service.createStudent(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Полностью заменить данные студента")
    public StudentResponse replace(@PathVariable long id, @Valid @RequestBody StudentRequest request) {
        return service.replaceStudent(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Изменить часть полей студента")
    public StudentResponse update(@PathVariable long id, @Valid @RequestBody StudentPatchRequest request) {
        return service.updateStudent(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить студента")
    public void delete(@PathVariable long id) {
        service.deleteStudent(id);
    }
}
