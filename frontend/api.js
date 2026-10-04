function showMessage(text, isError = false) {
    const message = document.querySelector("#message");
    message.textContent = text;
    message.hidden = text === "";
    message.className = isError ? "message error" : "message success";
}

function formatDate(date) {
    return date.split("-").reverse().join(".");
}

async function sendRequest(method, url, data) {
    const options = { method: method, headers: {} };
    if (data !== undefined) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(data);
    }

    let response;
    try {
        response = await fetch(url, options);
    } catch (error) {
        throw { message: "Сервер недоступен. Проверьте, что он запущен" };
    }

    if (response.status === 204) {
        return null;
    }

    const body = await response.json();
    if (!response.ok) {
        throw body;
    }
    return body;
}

function getStudents(filters) {
    if (Object.keys(filters).length > 3) {
        return sendRequest("QUERY", "/api/requests", filters);
    }
    const params = new URLSearchParams(filters);
    return sendRequest("GET", "/api/requests?" + params);
}

function getStudent(id) {
    return sendRequest("GET", "/api/requests/" + id);
}

function createStudent(student) {
    return sendRequest("POST", "/api/requests", student);
}

function updateStudent(id, student) {
    return sendRequest("PATCH", "/api/requests/" + id, student);
}

function deleteStudent(id) {
    return sendRequest("DELETE", "/api/requests/" + id);
}
