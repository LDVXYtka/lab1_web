const form = document.querySelector("#student-form");
const fieldNames = ["name", "group", "isu", "dorm", "room", "term", "note"];
const fields = {};
for (const name of fieldNames) {
    fields[name] = document.querySelector("#" + name);
}

const parameters = new URLSearchParams(window.location.search);
const editingIsu = parameters.get("isu");
const lastDate = new Date().getFullYear() + "-12-31";
fields.term.max = lastDate;
let submitted = false;
let students = loadStudents();

function normalizeName(name) {
    return name.trim().replace(/\s+/g, " ");
}

function getFieldError(name) {
    const field = fields[name];
    const value = field.value;

    if (field.validity.badInput) {
        return "Введите целое число.";
    }
    if (field.validity.valueMissing) {
        return "Заполните это поле.";
    }

    if (name === "name") {
        const normalized = normalizeName(value);
        const namePattern = /^[A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*(?: [A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*)+$/;
        if (normalized.length > 100) {
            return "ФИО должно содержать не больше 100 символов.";
        }
        if (!namePattern.test(normalized)) {
            return "Введите минимум два слова: фамилию и имя. Допустимы русские и латинские буквы, дефисы и апострофы внутри слов.";
        }
    }

    if (name === "group" && !/^[A-Z][0-9]{4}$/.test(value)) {
        return "Укажите латинскую букву и четыре цифры, например P3220.";
    }

    if (name === "isu") {
        if (!/^[1-9][0-9]{5}$/.test(value)) {
            return "ИСУ должен состоять из шести цифр и не начинаться с нуля.";
        }
        if (editingIsu !== null && value !== editingIsu) {
            return "ИСУ нельзя изменять при редактировании.";
        }
        if (students.some(student => student.isu === value && student.isu !== editingIsu)) {
            return "Студент с таким ИСУ уже существует.";
        }
    }

    if (name === "dorm" && !/^[1-8]$/.test(value)) {
        return "Введите целый номер общежития от 1 до 8.";
    }

    if (name === "room" && !/^(?:[1-9]|1[0-9]|20)(?:0[1-9]|[1-9][0-9])$/.test(value)) {
        return "Укажите этаж от 1 до 20 и две цифры комнаты от 01 до 99. Например: 101, 1001, 2001. Комнаты 00 нет.";
    }

    if (name === "term") {
        if (!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(value) || value < "1970-01-01" || value > lastDate) {
            return "Дата должна быть от 01.01.1970 до " + formatDate(lastDate) + ".";
        }
    }

    if (name === "note" && value.length > 2000) {
        return "Заметки должны содержать не больше 2000 символов.";
    }

    if (!field.validity.valid) {
        return "Проверьте формат и допустимое значение поля.";
    }
    return "";
}

function checkField(name) {
    const error = getFieldError(name);
    const errorElement = document.querySelector("#" + name + "-error");
    errorElement.textContent = error;
    errorElement.hidden = error === "";
    fields[name].setAttribute("aria-invalid", error === "" ? "false" : "true");
    return error === "";
}

function fillForm(student) {
    for (const name of fieldNames) {
        fields[name].value = student[name];
    }
    document.querySelector("#international").checked = student.international;
    fields.isu.readOnly = true;
    document.querySelector("#isu-help").textContent = "ИСУ является идентификатором студента и не изменяется.";
    document.querySelector("#form-title").textContent = "Редактирование студента";
    document.querySelector("#save-button").textContent = "Сохранить изменения";
    document.title = "Редактирование студента";
}

if (students === null) {
    form.hidden = true;
} else if (editingIsu !== null) {
    const student = students.find(student => student.isu === editingIsu);
    if (student) {
        fillForm(student);
    } else {
        form.hidden = true;
        showMessage("Студент не найден. Вернитесь к списку студентов.", true);
    }
}

fields.name.addEventListener("blur", function () {
    fields.name.value = normalizeName(fields.name.value);
    if (submitted) {
        checkField("name");
    }
});

for (const name of fieldNames) {
    fields[name].addEventListener("input", function () {
        if (name === "group") {
            fields.group.value = fields.group.value.toUpperCase();
        }
        if (submitted && students !== null) {
            checkField(name);
        }
    });
}

form.addEventListener("submit", function (event) {
    event.preventDefault();
    submitted = true;
    showMessage("");
    students = loadStudents();
    if (students === null) {
        return;
    }

    fields.name.value = normalizeName(fields.name.value);
    fields.group.value = fields.group.value.trim().toUpperCase();
    fields.isu.value = fields.isu.value.trim();
    fields.room.value = fields.room.value.trim();

    let firstInvalidField = null;
    for (const name of fieldNames) {
        if (!checkField(name) && firstInvalidField === null) {
            firstInvalidField = fields[name];
        }
    }
    if (firstInvalidField !== null) {
        firstInvalidField.focus();
        return;
    }

    const student = {
        name: fields.name.value,
        group: fields.group.value,
        isu: fields.isu.value,
        dorm: Number(fields.dorm.value),
        room: fields.room.value,
        term: fields.term.value,
        international: document.querySelector("#international").checked,
        note: fields.note.value
    };

    if (editingIsu === null) {
        students.push(student);
    } else {
        const index = students.findIndex(student => student.isu === editingIsu);
        if (index === -1) {
            showMessage("Студент уже удален. Вернитесь к списку студентов.", true);
            return;
        }
        students[index] = student;
    }

    if (saveStudents(students)) {
        window.location.href = "table.html";
    }
});
