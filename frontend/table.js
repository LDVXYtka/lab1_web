const tableBody = document.querySelector("#table-body");
const emptyState = document.querySelector("#empty-state");
const studentCount = document.querySelector("#student-count");
const deletePanel = document.querySelector("#delete-panel");
let isuToDelete = null;

function renderStudents(students) {
    tableBody.replaceChildren();
    studentCount.textContent = "Всего студентов: " + students.length;
    emptyState.hidden = students.length !== 0;

    for (const student of students) {
        const row = document.createElement("tr");
        const values = [student.name, student.group, student.isu, student.dorm,
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
            window.location.href = "person.html?isu=" + encodeURIComponent(student.isu);
        });

        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.textContent = "Изменить";
        editButton.addEventListener("click", function () {
            window.location.href = "form.html?isu=" + encodeURIComponent(student.isu);
        })

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.textContent = "Удалить";
        deleteButton.addEventListener("click", function () {
            isuToDelete = student.isu;
            showMessage("");
            document.querySelector("#delete-question").textContent =
                "Удалить студента " + student.name + " (ИСУ " + student.isu + ")?";
            deletePanel.hidden = false;
            document.querySelector("#cancel-delete").focus();
        });

        actionCell.append(detailsButton, editButton, deleteButton);
        row.append(actionCell);
        tableBody.append(row);
    }
}

document.querySelector("#cancel-delete").addEventListener("click", function () {
    deletePanel.hidden = true;
    isuToDelete = null;
    document.querySelector("#table").focus();
});

document.querySelector("#confirm-delete").addEventListener("click", function () {
    if (isuToDelete === null) {
        return;
    }
    const students = loadStudents();
    if (students === null) {
        return;
    }
    const remainingStudents = students.filter(student => student.isu !== isuToDelete);
    if (saveStudents(remainingStudents)) {
        renderStudents(remainingStudents);
        deletePanel.hidden = true;
        isuToDelete = null;
        document.querySelector("#table").focus();
    }
});

const students = loadStudents();
renderStudents(students);
