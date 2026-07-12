/**
 * Verantwortungsbereich: Gemeinsame Frontend-Entwicklung,
 * Benutzerinteraktion und Echtzeitkommunikation im Chat.
 *
 * @author Zeynep Ünver
 * @author Nilüfer Civelek
 */
const loginForm = document.getElementById("loginForm");
const errorMessage = document.getElementById("errorMessage");
const formTitle = document.getElementById("formTitle");
const formSubtitle = document.getElementById("formSubtitle");
const submitBtn = document.getElementById("submitBtn");
const toggleLink = document.getElementById("toggleLink");
const toggleMsg = document.getElementById("toggleMsg");

let isLoginMode = true;

toggleLink.addEventListener("click", function (event) {
    event.preventDefault();
    isLoginMode = !isLoginMode;

    if (isLoginMode) {
        if (formTitle) formTitle.textContent = "Willkommen zurück";
        formSubtitle.textContent = "Mit Net Passport anmelden";
        submitBtn.textContent = "Anmelden";
        toggleMsg.textContent = "Kein Net Passport vorhanden?";
        toggleLink.textContent = "Registrieren";
    } else {
        if (formTitle) formTitle.textContent = "Konto erstellen";
        formSubtitle.textContent = "Konto erstellen, um zu chatten";
        submitBtn.textContent = "Registrieren";
        toggleMsg.textContent = "Bereits ein Konto vorhanden?";
        toggleLink.textContent = "Anmelden";
    }
    errorMessage.textContent = "";
});

loginForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    errorMessage.textContent = "";

    const endpoint = isLoginMode ? "/login" : "/register";

    fetch(endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password })
    })
    .then(async response => {
        const text = await response.text();
        if (response.ok) {
            if (isLoginMode) {
                localStorage.setItem("username", text);
                window.location.href = "chat.html";
            } else {
                // After registering, switch to login and show success message
                isLoginMode = true;
                if (formTitle) formTitle.textContent = "Willkommen zurück";
                formSubtitle.textContent = "Mit Net Passport anmelden";
                submitBtn.textContent = "Anmelden";
                toggleMsg.textContent = "Kein Net Passport vorhanden?";
                toggleLink.textContent = "Registrieren";
                errorMessage.style.color = "green";
                errorMessage.textContent = "Konto erstellt! Du kannst dich jetzt anmelden.";
                document.getElementById("password").value = "";
            }
        } else {
            errorMessage.style.color = "#dc2626";
            let msg = text;
            if (text === "Username and password cannot be empty.") {
                msg = "Benutzername und Passwort dürfen nicht leer sein.";
            } else if (text === "Username is already taken.") {
                msg = "Benutzername ist bereits vergeben.";
            } else if (text === "Invalid username or password.") {
                msg = "Ungültiger Benutzername oder Passwort.";
            }
            errorMessage.textContent = msg;
        }
    })
    .catch(error => {
        errorMessage.style.color = "#dc2626";
        errorMessage.textContent = "Verbindungsfehler. Bitte versuche es erneut.";
        console.error(error);
    });
});