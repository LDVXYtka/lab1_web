const tableBody = document.querySelector("#table-body");
const emptyState = document.querySelector("#empty-state");
const studentCount = document.querySelector("#student-count");
const deleteDialog = document.querySelector("#delete-dialog");
const filterNames = ["name", "group", "isu", "dormitory", "room", "term_from", "term_to", "international"];
let idToDelete = null;

function getFiltersFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const filters = {};
    for (const name of filterNames) {
        const value = params.get(name);
        if (value !== null && value.trim() !== "") {
            filters[name] = value.trim();
        }
    }
    return filters;
}

function fillFiltersForm(filters) {
    for (const name in filters) {
        document.querySelector("#" + name).value = filters[name];
    }
}

function renderStudents(students) {
    tableBody.replaceChildren();
    studentCount.textContent = "Студентов: " + students.length;
    emptyState.hidden = students.length !== 0;

    for (const student of students) {
        const row = document.createElement("tr");
        const values = [student.name, student.group, student.isu, student.dormitory,
            student.room, formatDate(student.term), student.international ? "Да" : "Нет"];

        for (const value of values) {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.append(cell);
        }

        const actionCell = document.createElement("td");

        const detailsButton = document.createElement("button");
        detailsButton.type = "button";
        detailsButton.textContent = "Подробнее";
        detailsButton.addEventListener("click", function () {
            window.location.href = "person.html?id=" + student.id;
        });

        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.textContent = "Изменить";
        editButton.addEventListener("click", function () {
            window.location.href = "form.html?id=" + student.id;
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.textContent = "Удалить";
        deleteButton.addEventListener("click", function () {
            idToDelete = student.id;
            showMessage("");
            document.querySelector("#delete-question").textContent =
                "Удалить студента " + student.name + " (ИСУ " + student.isu + ")?";
            deleteDialog.showModal();
            document.querySelector("#cancel-delete").focus();
        });

        actionCell.append(detailsButton, editButton, deleteButton);
        row.append(actionCell);
        tableBody.append(row);
    }
}

async function loadStudents() {
    try {
        const students = await getStudents(getFiltersFromUrl());
        if (!Array.isArray(students)) {
            throw new Error("Сервер прислал неверные данные");
        }
        renderStudents(students);
    } catch (error) {
        showMessage(error.message, true);
    }
}

document.querySelector("#cancel-delete").addEventListener("click", function () {
    deleteDialog.close();
    idToDelete = null;
});

document.querySelector("#confirm-delete").addEventListener("click", async function () {
    if (idToDelete === null) {
        return;
    }
    try {
        await deleteStudent(idToDelete);
        showMessage("Студент удален");
    } catch (error) {
        showMessage(error.message, true);
    }
    deleteDialog.close();
    idToDelete = null;
    loadStudents();
});

fillFiltersForm(getFiltersFromUrl());
loadStudents();
