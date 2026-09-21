// SkillSwap — login.html behavior

document.addEventListener("DOMContentLoaded", function () {
  wireUpPasswordToggle("togglePassword", "password");

  var form = document.getElementById("loginForm");
  var messageBox = document.getElementById("formMessage");
  var submitBtn = document.getElementById("submitBtn");

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    var email = document.getElementById("email").value.trim();
    var password = document.getElementById("password").value;

    var isValid = validate(email, password);
    if (!isValid) return;

    submitBtn.disabled = true;
    submitBtn.textContent = "Logging in...";

    try {
      var result = await postForm("login", { email: email, password: password });

      if (result.success) {
        showFormMessage(messageBox, result.message, "success");
        // The servlet has already created the session; the browser
        // just needs to navigate to the protected route next.
        setTimeout(function () {
          window.location.href = "dashboard";
        }, 600);
      } else {
        showFormMessage(messageBox, result.message, "error");
      }
    } catch (err) {
      showFormMessage(messageBox, "Could not reach the server. Please try again.", "error");
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = "Log in";
    }
  });

  function validate(email, password) {
    var ok = true;

    var emailPattern = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/;
    if (!email || !emailPattern.test(email)) {
      setFieldError("email", "emailError", "Please enter a valid email address.");
      ok = false;
    } else {
      setFieldError("email", "emailError", "");
    }

    if (!password) {
      setFieldError("password", "passwordError", "Please enter your password.");
      ok = false;
    } else {
      setFieldError("password", "passwordError", "");
    }

    return ok;
  }
});
