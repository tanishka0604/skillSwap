// SkillSwap — register.html behavior
//
// This file does two things:
//   1. Basic client-side validation (fast feedback, no round trip).
//   2. Sends the data to RegisterServlet and shows its response.
//
// IMPORTANT: client-side checks are a convenience for the user, not
// security. RegisterServlet -> UserService validates everything again
// on the server, because JavaScript in the browser can always be
// bypassed or edited by whoever is using the page.

document.addEventListener("DOMContentLoaded", function () {
  wireUpPasswordToggle("togglePassword", "password");
  wireUpPasswordToggle("toggleConfirmPassword", "confirmPassword");

  var form = document.getElementById("registerForm");
  var messageBox = document.getElementById("formMessage");
  var submitBtn = document.getElementById("submitBtn");

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    var name = document.getElementById("name").value.trim();
    var email = document.getElementById("email").value.trim();
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;

    var isValid = validate(name, email, password, confirmPassword);
    if (!isValid) return;

    submitBtn.disabled = true;
    submitBtn.textContent = "Creating account...";

    try {
      var result = await postForm("register", { name: name, email: email, password: password });

      if (result.success) {
        showFormMessage(messageBox, result.message + " Redirecting to login...", "success");
        setTimeout(function () {
          window.location.href = "login.html";
        }, 1200);
      } else {
        showFormMessage(messageBox, result.message, "error");
      }
    } catch (err) {
      showFormMessage(messageBox, "Could not reach the server. Please try again.", "error");
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = "Create account";
    }
  });

  function validate(name, email, password, confirmPassword) {
    var ok = true;

    if (!name) {
      setFieldError("name", "nameError", "Please enter your name.");
      ok = false;
    } else {
      setFieldError("name", "nameError", "");
    }

    var emailPattern = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/;
    if (!email || !emailPattern.test(email)) {
      setFieldError("email", "emailError", "Please enter a valid email address.");
      ok = false;
    } else {
      setFieldError("email", "emailError", "");
    }

    if (!password || password.length < 6) {
      setFieldError("password", "passwordError", "Password must be at least 6 characters.");
      ok = false;
    } else {
      setFieldError("password", "passwordError", "");
    }

    if (password !== confirmPassword) {
      setFieldError("confirmPassword", "confirmPasswordError", "Passwords do not match.");
      ok = false;
    } else {
      setFieldError("confirmPassword", "confirmPasswordError", "");
    }

    return ok;
  }
});
