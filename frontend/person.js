const isu = new URLSearchParams(window.location.search).get("isu");
const students = loadStudents();
const student = students === null ? null : students.find(student => student.isu === isu);

if (student) {
    document.querySelector("#name").textContent = student.name;
    document.querySelector("#group").textContent = student.group;
    document.querySelector("#number").textContent = student.isu;
    document.querySelector("#dorm").textContent = student.dorm;
    document.querySelector("#room").textContent = student.room;
    document.querySelector("#date").textContent = formatDate(student.term);
    document.querySelector("#international").textContent = student.international ? "Да" : "Нет";
    document.querySelector("#notes").textContent = student.note || "Заметок нет.";
    document.querySelector("#edit-button").href = "form.html?isu=" + encodeURIComponent(student.isu);
    document.title = student.name + " - Досье студента";
} else {
    document.querySelector("#student-details").hidden = true;
    document.querySelector("#edit-link").hidden = true;
    if (students !== null) {
        showMessage("Студент не найден. Вернитесь к списку студентов.", true);
    }
}
