const tableBody = document.querySelector("#table-body");
const emptyState = document.querySelector("#empty-state");
const studentCount = document.querySelector("#student-count");
const deletePanel = document.querySelector("#delete-panel");
const filtersForm = document.querySelector("#filters");
let idToDelete = null;

function getFiltersFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const filters = {};
    for (const [key, value] of params) {
        if (value.trim() !== "") {
            filters[key] = value.trim();
        }
    }
    return filters;
}

function fillFiltersForm(filters) {
    for (const key in filters) {
        const field = filtersForm.elements[key];
        if (field) {
            field.value = filters[key];
        }
    }
}

function renderStudents(students, hasFilters) {
    tableBody.replaceChildren();
    studentCount.textContent = "Студентов: " + students.length;
    emptyState.hidden = students.length !== 0;
    if (hasFilters) {
        emptyState.textContent = "По этим фильтрам никого не нашлось.";
    } else {
        emptyState.textContent = "Студентов пока нет. Добавьте первую запись.";
    }

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
    const filters = getFiltersFromUrl();
    try {
        const students = await getStudents(filters);
        renderStudents(students, Object.keys(filters).length > 0);
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
        showMessage("Студент удалён.");
    } catch (error) {
        showMessage(error.message, true);
    }
    deletePanel.hidden = true;
    idToDelete = null;
    loadStudents();
});

fillFiltersForm(getFiltersFromUrl());
loadStudents();
