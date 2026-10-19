const registerForm = document.querySelector("#register-form");
const fieldNames = ["username", "email", "password", "password2", "role"];

registerForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    showMessage("");
    for (const name of fieldNames) {
        showFieldError(name, "");
    }

    const user = {
        username: document.querySelector("#username").value.trim(),
        email: document.querySelector("#email").value.trim(),
        password: document.querySelector("#password").value,
        role: document.querySelector("#role").value
    };
    if (user.password !== document.querySelector("#password2").value) {
        showFieldError("password2", "Пароли не совпадают");
        return;
    }

    try {
        await register(user);
        await login(user.username, user.password);
        window.location.href = "table.html";
    } catch (error) {
        showMessage(error.message, true);
        for (const name in error.fields) {
            if (fieldNames.includes(name)) {
                showFieldError(name, error.fields[name]);
            }
        }
    }
});
