const tableBody = document.querySelector("#table-body");
const emptyState = document.querySelector("#empty-state");
const studentCount = document.querySelector("#student-count");
const deletePanel = document.querySelector("#delete-panel");
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

        const detailsLink = document.createElement("a");
        detailsLink.className = "button";
        detailsLink.textContent = "Подробнее";
        detailsLink.href = "person.html?id=" + student.id;

        const editLink = document.createElement("a");
        editLink.className = "button";
        editLink.textContent = "Изменить";
        editLink.href = "form.html?id=" + student.id;

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.textContent = "Удалить";
        deleteButton.addEventListener("click", function () {
            idToDelete = student.id;
            showMessage("");
            document.querySelector("#delete-question").textContent =
                "Удалить студента " + student.name + " (ИСУ " + student.isu + ")?";
            deletePanel.hidden = false;
            document.querySelector("#cancel-delete").focus();
        });

        actionCell.append(detailsLink, editLink, deleteButton);
        row.append(actionCell);
        tableBody.append(row);
    }
}

async function loadStudents() {
    try {
        const students = await getStudents(getFiltersFromUrl());
        renderStudents(students);
    } catch (error) {
        showMessage(error.message, true);
    }
}

document.querySelector("#cancel-delete").addEventListener("click", function () {
    deletePanel.hidden = true;
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
    deletePanel.hidden = true;
    idToDelete = null;
    loadStudents();
});

fillFiltersForm(getFiltersFromUrl());
loadStudents();
