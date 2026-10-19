const loginForm = document.querySelector("#login-form");

loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    showMessage("");

    const username = document.querySelector("#username").value.trim();
    const password = document.querySelector("#password").value;
    if (username === "" || password === "") {
        showMessage("Введите имя пользователя и пароль", true);
        return;
    }

    try {
        await login(username, password);
        window.location.href = "table.html";
    } catch (error) {
        showMessage(error.message, true);
    }
});
