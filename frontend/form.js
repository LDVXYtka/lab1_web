const form = document.querySelector("#form");
const fieldNames = ["name", "group", "isu", "dormitory", "room", "term", "note"];
const fields = {};
for (const name of fieldNames) {
    fields[name] = document.querySelector("#" + name);
}
const international = document.querySelector("#international");

const editingId = new URLSearchParams(window.location.search).get("id");
const lastDate = new Date().getFullYear() + "-12-31";
fields.term.max = lastDate;

function normalizeName(name) {
    return name.trim().replace(/\s+/g, " ");
}

function getFieldError(name) {
    const value = fields[name].value;

    if (name === "name") {
        const namePattern = /^[A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*(?: [A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*)+$/;
        if (value.length > 100) {
            return "ФИО должно содержать не больше 100 символов.";
        }
        if (!namePattern.test(value)) {
            return "Введите минимум два слова: фамилию и имя. Допустимы русские и латинские буквы, дефисы и апострофы внутри слов.";
        }
    }

    if (name === "group" && !/^[A-Z][34][1-4][0-9]{2}$/.test(value)) {
        return "Укажите латинскую букву, затем 3 или 4 и еще три цифры, например M3301.";
    }

    if (name === "isu" && !/^[1-9][0-9]{5}$/.test(value)) {
        return "ИСУ должен состоять из шести цифр, первая цифра не 0.";
    }

    if (name === "dormitory" && !/^[1-8]$/.test(value)) {
        return "Введите целый номер общежития от 1 до 8.";
    }

    if (name === "room" && !/^(?:[1-9]|1[0-9]|20)(?:0[1-9]|[1-9][0-9])$/.test(value)) {
        return "Укажите этаж от 1 до 20 и две цифры комнаты от 01 до 99.";
    }

    if (name === "term") {
        if (!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(value) || value < "1970-01-01" || value > lastDate) {
            return "Дата должна быть от 01.01.1970 до " + formatDate(lastDate) + ".";
        }
    }

    if (name === "note" && value.length > 2000) {
        return "Заметки должны содержать не больше 2000 символов.";
    }

    return "";
}

function showFieldError(name, text) {
    const errorElement = document.querySelector("#" + name + "-error");
    errorElement.textContent = text;
    errorElement.hidden = text === "";
}

function checkField(name) {
    const error = getFieldError(name);
    showFieldError(name, error);
    return error === "";
}

function fillForm(student) {
    for (const name of fieldNames) {
        fields[name].value = student[name];
    }
    international.checked = student.international;
    document.querySelector("#form-title").textContent = "Редактирование студента";
    document.querySelector("#save-button").textContent = "Сохранить изменения";
    document.title = "Редактирование студента";
}

async function loadStudent() {
    try {
        const student = await getStudent(editingId);
        fillForm(student);
    } catch (error) {
        form.hidden = true;
        showMessage(error.message, true);
    }
}

if (editingId !== null) {
    loadStudent();
}

fields.group.addEventListener("input", function () {
    fields.group.value = fields.group.value.toUpperCase();
});

form.addEventListener("submit", async function (event) {
    event.preventDefault();
    showMessage("");

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
        dormitory: Number(fields.dormitory.value),
        room: fields.room.value,
        term: fields.term.value,
        international: international.checked,
        note: fields.note.value
    };

    try {
        if (editingId === null) {
            await createStudent(student);
        } else {
            await updateStudent(editingId, student);
        }
        window.location.href = "table.html";
    } catch (error) {
        showMessage(error.message, true);
        for (const name in error.fields) {
            if (fieldNames.includes(name)) {
                showFieldError(name, error.fields[name]);
            }
        }
    }
});
