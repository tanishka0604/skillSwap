// SkillSwap — Phase 1 frontend JavaScript
// No backend calls yet. This file only handles page behavior:
// the mobile menu toggle and closing that menu after a link is clicked.

document.addEventListener("DOMContentLoaded", function () {
  var navbar = document.getElementById("navbar");
  var navToggle = document.getElementById("navToggle");

  if (navToggle && navbar) {
    navToggle.addEventListener("click", function () {
      navbar.classList.toggle("open");
    });

    // Close the mobile menu once a nav link is tapped
    var links = navbar.querySelectorAll(".nav-links a");
    links.forEach(function (link) {
      link.addEventListener("click", function () {
        navbar.classList.remove("open");
      });
    });
  }
});
