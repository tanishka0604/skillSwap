// SkillSwap — shared helpers for register.html and login.html

// Shows a message box under the form. type is "success" or "error".
function showFormMessage(el, text, type) {
  el.textContent = text;
  el.className = "form-message show " + type;
}

// Wires up a "Show/Hide" toggle button next to a password input.
function wireUpPasswordToggle(toggleButtonId, inputId) {
  var toggleBtn = document.getElementById(toggleButtonId);
  var input = document.getElementById(inputId);

  if (!toggleBtn || !input) return;

  toggleBtn.addEventListener("click", function () {
    var isPassword = input.getAttribute("type") === "password";
    input.setAttribute("type", isPassword ? "text" : "password");
    toggleBtn.textContent = isPassword ? "Hide" : "Show";
  });
}

// Sends form-urlencoded data to a servlet endpoint and returns the
// parsed JSON response ({ success, message }).
async function postForm(url, fields) {
  var body = new URLSearchParams();
  for (var key in fields) {
    body.append(key, fields[key]);
  }

  var response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: body.toString()
  });

  return response.json();
}

// Shows a small inline error under one field, and marks the input.
function setFieldError(inputId, errorId, message) {
  var input = document.getElementById(inputId);
  var errorEl = document.getElementById(errorId);
  if (input) input.classList.toggle("invalid", Boolean(message));
  if (errorEl) errorEl.textContent = message || "";
}
