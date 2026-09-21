// SkillSwap — profile.html behavior
//
// Loads the current profile from GET /api/profile, fills in the form
// and the read-only summary card, and saves edits via POST /api/profile.
//
// We build DOM elements with createElement()/textContent instead of
// innerHTML wherever we're inserting data that came from a user (their
// name, a skill name) — textContent always treats its input as plain
// text, never as HTML to parse, so there's no way for someone's bio or
// skill name to accidentally (or deliberately) inject a tag into the
// page. innerHTML would parse whatever string we hand it as markup.

document.addEventListener("DOMContentLoaded", async function () {
  var form = document.getElementById("profileForm");
  var messageBox = document.getElementById("formMessage");
  var submitBtn = document.getElementById("submitBtn");

  await loadProfile();

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    var name = document.getElementById("name").value.trim();
    var bio = document.getElementById("bio").value;
    var profilePictureUrl = document.getElementById("profilePictureUrl").value.trim();

    if (!name) {
      showFormMessage(messageBox, "Name is required.", "error");
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Saving...";

    try {
      var result = await postForm("api/profile", { name: name, bio: bio, profilePictureUrl: profilePictureUrl });

      if (result.success) {
        showFormMessage(messageBox, result.message, "success");
        document.getElementById("displayName").textContent = name;
        renderAvatar(profilePictureUrl, name);
      } else {
        showFormMessage(messageBox, result.message, "error");
      }
    } catch (err) {
      showFormMessage(messageBox, "Could not reach the server. Please try again.", "error");
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = "Save changes";
    }
  });

  async function loadProfile() {
    try {
      var response = await fetch("api/profile");
      var result = await response.json();

      if (!result.success) {
        showFormMessage(messageBox, result.message, "error");
        return;
      }

      var profile = result.profile;

      document.getElementById("name").value = profile.name;
      document.getElementById("bio").value = profile.bio || "";
      document.getElementById("profilePictureUrl").value = profile.profilePictureUrl || "";

      document.getElementById("displayName").textContent = profile.name;
      document.getElementById("displayEmail").textContent = profile.email;

      document.getElementById("teachCount").textContent = profile.teachSkills.length;
      document.getElementById("learnCount").textContent = profile.learnSkills.length;

      renderAvatar(profile.profilePictureUrl, profile.name);
      renderSkillTags("teachTags", profile.teachSkills);
      renderSkillTags("learnTags", profile.learnSkills);

    } catch (err) {
      showFormMessage(messageBox, "Could not load your profile.", "error");
    }
  }

  function renderAvatar(profilePictureUrl, name) {
    var avatar = document.getElementById("avatar");
    avatar.innerHTML = ""; // clear whatever was there before re-rendering

    if (profilePictureUrl) {
      var img = document.createElement("img");
      img.src = profilePictureUrl; // attribute assignment, not HTML parsing
      img.alt = name;
      avatar.appendChild(img);
    } else {
      // Fall back to showing initials when there's no picture yet.
      avatar.textContent = name ? name.trim().charAt(0).toUpperCase() : "?";
    }
  }

  function renderSkillTags(containerId, skills) {
    var container = document.getElementById(containerId);
    container.innerHTML = "";

    if (skills.length === 0) {
      var empty = document.createElement("p");
      empty.className = "empty";
      empty.textContent = "None added yet.";
      container.appendChild(empty);
      return;
    }

    skills.forEach(function (skill) {
      var tag = document.createElement("span");
      tag.className = "skill-tag";
      tag.textContent = skill.name;
      container.appendChild(tag);
    });
  }
});
