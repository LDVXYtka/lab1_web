const tableBody = document.querySelector("#table-body");
const emptyState = document.querySelector("#empty-state");
const studentCount = document.querySelector("#student-count");
const deleteDialog = document.querySelector("#delete-dialog");
const pageInfo = document.querySelector("#page-info");
const prevButton = document.querySelector("#prev-page");
const nextButton = document.querySelector("#next-page");
const paramNames = ["name", "group", "isu", "dormitory", "room", "term_from", "term_to", "international", "sort", "order"];
const pageSize = 10;
let idToDelete = null;

function getParamsFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    const params = {};
    for (const name of paramNames) {
        const value = urlParams.get(name);
        if (value !== null && value.trim() !== "") {
            params[name] = value.trim();
        }
    }
    return params;
}

function getPageFromUrl() {
    const page = Number(new URLSearchParams(window.location.search).get("page"));
    if (Number.isInteger(page) && page >= 1) {
        return page;
    }
    return 1;
}

function goToPage(page) {
    const params = getParamsFromUrl();
    params.page = page;
    window.location.href = "table.html?" + new URLSearchParams(params);
}

function fillFiltersForm(params) {
    for (const name in params) {
        document.querySelector("#" + name).value = params[name];
    }
}

function renderStudents(students) {
    tableBody.replaceChildren();
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
        actionCell.append(detailsButton);

        if (isAdmin()) {
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

            actionCell.append(editButton, deleteButton);
        }

        row.append(actionCell);
        tableBody.append(row);
    }
}

function renderPagination(page, pages) {
    pageInfo.textContent = "Страница " + page + " из " + pages;
    prevButton.disabled = page <= 1;
    nextButton.disabled = page >= pages;
}

async function loadStudents() {
    const page = getPageFromUrl();
    const params = getParamsFromUrl();
    params.page = page;
    params.size = pageSize;

    try {
        const result = await getStudents(params);
        const pages = Math.max(1, Math.ceil(result.total / result.size));
        if (page > pages) {
            goToPage(pages);
            return;
        }
        studentCount.textContent = "Студентов: " + result.total;
        renderStudents(result.items);
        renderPagination(page, pages);
    } catch (error) {
        showMessage(error.message, true);
    }
}

prevButton.addEventListener("click", function () {
    goToPage(getPageFromUrl() - 1);
});

nextButton.addEventListener("click", function () {
    goToPage(getPageFromUrl() + 1);
});

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

showUserPanel();
document.querySelector("#add-button").hidden = !isAdmin();
fillFiltersForm(getParamsFromUrl());
loadStudents();
