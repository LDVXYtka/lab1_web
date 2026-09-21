const studentsBody = document.querySelector("#students-body");
const emptyState = document.querySelector("#empty-state");
const studentCount = document.querySelector("#student-count");
const deletePanel = document.querySelector("#delete-panel");
let isuToDelete = null;

function renderStudents(students) {
    studentsBody.replaceChildren();
    studentCount.textContent = students.length;
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
        const actions = document.createElement("div");
        actions.className = "row-actions";

        const detailsLink = document.createElement("a");
        detailsLink.textContent = "Подробнее";
        detailsLink.href = "person.html?isu=" + encodeURIComponent(student.isu);

        const editLink = document.createElement("a");
        editLink.textContent = "Изменить";
        editLink.href = "form.html?isu=" + encodeURIComponent(student.isu);

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

        actions.append(detailsLink, editLink, deleteButton);
        actionCell.append(actions);
        row.append(actionCell);
        studentsBody.append(row);
    }
}

document.querySelector("#cancel-delete").addEventListener("click", function () {
    deletePanel.hidden = true;
    isuToDelete = null;
    document.querySelector("#table-scroll").focus();
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
        showMessage("Студент удален.");
        document.querySelector("#table-scroll").focus();
    }
});

const students = loadStudents();
if (students !== null) {
    renderStudents(students);
} else {
    emptyState.hidden = true;
    studentCount.textContent = "-";
}
