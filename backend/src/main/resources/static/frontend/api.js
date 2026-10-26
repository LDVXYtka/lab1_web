function showMessage(text, isError = false) {
    const message = document.querySelector("#message");
    message.textContent = text;
    message.hidden = text === "";
    message.className = isError ? "message error" : "message success";
}

function showFieldError(name, text) {
    const errorElement = document.querySelector("#" + name + "-error");
    errorElement.textContent = text;
    errorElement.hidden = text === "";
}

function formatDate(date) {
    return date.split("-").reverse().join(".");
}

function saveTokens(tokens) {
    localStorage.setItem("accessToken", tokens.accessToken);
    localStorage.setItem("refreshToken", tokens.refreshToken);
}

function goToLogin() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    window.location.replace("login.html");
}

function isAdmin() {
    return localStorage.getItem("role") === "ADMIN";
}

async function callServer(method, url, data, token) {
    const options = { method: method, headers: {} };
    if (token) {
        options.headers["Authorization"] = "Bearer " + token;
    }
    if (data !== undefined) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(data);
    }

    try {
        return await fetch(url, options);
    } catch (error) {
        throw new Error("Сервер недоступен. Проверьте, что он запущен");
    }
}

async function readResponse(response) {
    if (response.status === 204) {
        return null;
    }

    const body = await response.json();
    if (!response.ok) {
        const error = new Error(body.message);
        error.fields = body.fields;
        throw error;
    }
    return body;
}

async function refreshTokens() {
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken === null) {
        return false;
    }

    const response = await callServer("POST", "/api/auth/refresh", { refreshToken: refreshToken });
    if (!response.ok) {
        return false;
    }
    saveTokens(await response.json());
    return true;
}

async function sendRequest(method, url, data) {
    let response = await callServer(method, url, data, localStorage.getItem("accessToken"));
    if (response.status === 401 && await refreshTokens()) {
        response = await callServer(method, url, data, localStorage.getItem("accessToken"));
    }
    if (response.status === 401) {
        goToLogin();
    }
    return readResponse(response);
}

async function login(username, password) {
    const response = await callServer("POST", "/api/auth/login", { username: username, password: password });
    const tokens = await readResponse(response);
    saveTokens(tokens);

    const user = await readResponse(await callServer("GET", "/api/auth/me", undefined, tokens.accessToken));
    localStorage.setItem("username", user.username);
    localStorage.setItem("role", user.role);
}

async function register(user) {
    const response = await callServer("POST", "/api/auth/register", user);
    return readResponse(response);
}

async function logout() {
    try {
        await callServer("POST", "/api/auth/logout", { refreshToken: localStorage.getItem("refreshToken") });
    } finally {
        goToLogin();
    }
}

function showUserPanel() {
    document.querySelector("#current-user").textContent =
        localStorage.getItem("username") + " (" + localStorage.getItem("role") + ")";
    document.querySelector("#logout-button").addEventListener("click", logout);
}

function getStudents(params) {
    const filterCount = Object.keys(params).filter(function (name) {
        return !["page", "size", "sort", "order"].includes(name);
    }).length;
    if (filterCount > 3) {
        return sendRequest("QUERY", "/api/requests", params);
    }
    return sendRequest("GET", "/api/requests?" + new URLSearchParams(params));
}

function getStudent(id) {
    return sendRequest("GET", "/api/requests/" + id);
}

function createStudent(student) {
    return sendRequest("POST", "/api/requests", student);
}

function replaceStudent(id, student) {
    return sendRequest("PUT", "/api/requests/" + id, student);
}

function deleteStudent(id) {
    return sendRequest("DELETE", "/api/requests/" + id);
}
