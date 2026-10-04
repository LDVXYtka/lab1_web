const id = new URLSearchParams(window.location.search).get("id");

async function loadPerson() {
    try {
        const student = await getStudent(id);
        document.querySelector("#name").textContent = student.name;
        document.querySelector("#group").textContent = student.group;
        document.querySelector("#isu").textContent = student.isu;
        document.querySelector("#dormitory").textContent = student.dormitory;
        document.querySelector("#room").textContent = student.room;
        document.querySelector("#term").textContent = formatDate(student.term);
        document.querySelector("#international").textContent = student.international ? "Да" : "Нет";
        document.querySelector("#note").textContent = student.note || "Заметок нет.";
        document.querySelector("#edit-link").href = "form.html?id=" + student.id;
        document.title = student.name + " - Досье студента";
    } catch (error) {
        document.querySelector("#student-details").hidden = true;
        document.querySelector("#edit-link").hidden = true;
        showMessage(error.message, true);
    }
}

loadPerson();
