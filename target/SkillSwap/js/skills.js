// SkillSwap — skills.html behavior
//
// Loads the user's current TEACH and LEARN lists from MySkillsServlet,
// renders them, and wires up the add-skill form and each remove button.

document.addEventListener("DOMContentLoaded", function () {
  var form = document.getElementById("addSkillForm");
  var messageBox = document.getElementById("formMessage");

  loadSkills("TEACH", "teachList");
  loadSkills("LEARN", "learnList");

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    var name = document.getElementById("skillName").value.trim();
    var category = document.getElementById("skillCategory").value;
    var type = document.getElementById("skillType").value;

    if (!name) {
      showFormMessage(messageBox, "Please enter a skill name.", "error");
      return;
    }

    try {
      var result = await postForm("skills/add", { name: name, category: category, type: type });

      if (result.success) {
        showFormMessage(messageBox, result.message, "success");
        document.getElementById("skillName").value = "";
        // Refresh whichever list this skill just went into.
        loadSkills(type, type === "TEACH" ? "teachList" : "learnList");
      } else {
        showFormMessage(messageBox, result.message, "error");
      }
    } catch (err) {
      showFormMessage(messageBox, "Could not reach the server. Please try again.", "error");
    }
  });

  async function loadSkills(type, listElementId) {
    var listEl = document.getElementById(listElementId);

    try {
      var response = await fetch("skills/mine?type=" + type);
      var result = await response.json();

      listEl.innerHTML = "";

      if (!result.success || result.skills.length === 0) {
        var emptyItem = document.createElement("li");
        emptyItem.className = "empty";
        emptyItem.textContent = "Nothing added yet.";
        listEl.appendChild(emptyItem);
        return;
      }

      result.skills.forEach(function (skill) {
        listEl.appendChild(buildSkillItem(skill, type));
      });

    } catch (err) {
      listEl.innerHTML = "<li class=\"empty\">Could not load skills.</li>";
    }
  }

  function buildSkillItem(skill, type) {
    var li = document.createElement("li");

    var label = document.createElement("span");
    label.textContent = skill.name;

    var categorySpan = document.createElement("span");
    categorySpan.className = "skill-category";
    categorySpan.textContent = skill.category;
    label.appendChild(categorySpan);

    var removeBtn = document.createElement("button");
    removeBtn.className = "remove-btn";
    removeBtn.textContent = "Remove";
    removeBtn.addEventListener("click", async function () {
      try {
        var result = await postForm("skills/remove", { skillId: skill.id, type: type });
        if (result.success) {
          loadSkills(type, type === "TEACH" ? "teachList" : "learnList");
        }
      } catch (err) {
        // Silently ignored here — the list simply won't update;
        // a production version would show a small inline error.
      }
    });

    li.appendChild(label);
    li.appendChild(removeBtn);
    return li;
  }
});
