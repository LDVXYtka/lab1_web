const storageKey = "lab1Students";

function showMessage(text, isError = false) {
    const message = document.querySelector("#message");
    message.textContent = text;
    message.hidden = text === "";
    message.className = isError ? "message error" : "message success";
}

function loadStudents() {
    try {
        const saved = localStorage.getItem(storageKey);
        if (saved === null) {
            return [];
        }

        const students = JSON.parse(saved);
        if (!Array.isArray(students) || students.some(student =>
            !student || typeof student.isu !== "string" || typeof student.name !== "string"
        )) {
            throw new Error("Invalid student list");
        }
        return students;
    } catch (error) {
        showMessage("Не удалось прочитать сохраненные данные. Они не были изменены.", true);
        return null;
    }
}

function saveStudents(students) {
    try {
        localStorage.setItem(storageKey, JSON.stringify(students));
        return true;
    } catch (error) {
        showMessage("Не удалось сохранить изменения. Проверьте, разрешено ли сохранение данных в браузере.", true);
        return false;
    }
}

function formatDate(date) {
    return date.split("-").reverse().join(".");
}
