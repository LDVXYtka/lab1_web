package ru.itmo.students.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.itmo.students.dto.StudentFilter;
import ru.itmo.students.dto.StudentPage;
import ru.itmo.students.dto.StudentPatchRequest;
import ru.itmo.students.dto.StudentRequest;
import ru.itmo.students.dto.StudentResponse;
import ru.itmo.students.service.StudentService;

import java.time.LocalDate;

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
    public StudentPage list(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String group,
                            @RequestParam(required = false) String isu,
                            @RequestParam(required = false) Integer dormitory,
                            @RequestParam(required = false) String room,
                            @RequestParam(name = "term_from", required = false) LocalDate termFrom,
                            @RequestParam(name = "term_to", required = false) LocalDate termTo,
                            @RequestParam(required = false) Boolean international,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "id") String sort,
                            @RequestParam(defaultValue = "asc") String order) {
        StudentFilter filter = new StudentFilter(name, group, isu, dormitory, room, termFrom, termTo, international);
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
