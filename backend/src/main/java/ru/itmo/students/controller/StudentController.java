package ru.itmo.students.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import ru.itmo.students.dto.StudentPage;
import ru.itmo.students.dto.StudentPatchRequest;
import ru.itmo.students.dto.StudentRequest;
import ru.itmo.students.dto.StudentResponse;
import ru.itmo.students.service.StudentService;

import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Студенты")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Список студентов с фильтрами, сортировкой и пагинацией")
    @Parameter(name = "name", in = ParameterIn.QUERY, description = "Часть ФИО без учета регистра")
    @Parameter(name = "group", in = ParameterIn.QUERY, description = "Группа, например M3301")
    @Parameter(name = "isu", in = ParameterIn.QUERY, description = "Номер ИСУ")
    @Parameter(name = "dormitory", in = ParameterIn.QUERY, description = "Общежитие от 1 до 8")
    @Parameter(name = "room", in = ParameterIn.QUERY, description = "Комната, например 1203")
    @Parameter(name = "term_from", in = ParameterIn.QUERY, description = "Заселение не раньше, ГГГГ-ММ-ДД")
    @Parameter(name = "term_to", in = ParameterIn.QUERY, description = "Заселение не позже, ГГГГ-ММ-ДД")
    @Parameter(name = "international", in = ParameterIn.QUERY, description = "true или false")
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Номер страницы с 1, по умолчанию 1")
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Записей на странице от 1 до 100, по умолчанию 10")
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "id, name, group, isu, dormitory, room, term или international")
    @Parameter(name = "order", in = ParameterIn.QUERY, description = "asc или desc")
    public StudentPage list(@Parameter(hidden = true) @RequestParam Map<String, String> params) {
        return service.findStudents(params);
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
