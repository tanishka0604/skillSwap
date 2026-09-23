// SkillSwap — matches.html behavior
//
// Fetches GET /api/matches and renders one card per reciprocal match.
// As in profile.js, anything built from another user's data (their
// name, bio, skill names) is inserted with textContent/createElement,
// never innerHTML — so a stray "<" in someone's bio can't be parsed
// as markup.

document.addEventListener("DOMContentLoaded", async function () {
  var listEl = document.getElementById("matchList");
  var emptyState = document.getElementById("emptyState");

  try {
    var response = await fetch("api/matches");
    var result = await response.json();

    if (!result.success) {
      listEl.textContent = result.message;
      return;
    }

    if (result.matches.length === 0) {
      emptyState.style.display = "block";
      return;
    }

    result.matches.forEach(function (match) {
      listEl.appendChild(buildMatchCard(match));
    });

  } catch (err) {
    listEl.textContent = "Could not load your matches. Please try again.";
  }

  function buildMatchCard(match) {
    var card = document.createElement("div");
    card.className = "match-card";

    // ---- Who ----
    var who = document.createElement("div");
    who.className = "who";

    var avatar = document.createElement("div");
    avatar.className = "match-avatar";
    avatar.textContent = match.name ? match.name.trim().charAt(0).toUpperCase() : "?";
    who.appendChild(avatar);

    var name = document.createElement("h3");
    name.textContent = match.name;
    who.appendChild(name);

    card.appendChild(who);

    // ---- What they teach that you want ----
    card.appendChild(buildSkillColumn("teach", "They can teach you", match.theyTeach));

    // ---- What they want that you teach ----
    card.appendChild(buildSkillColumn("learn", "They want to learn from you", match.theyWant));

    // ---- Send a swap request ----
    card.appendChild(buildRequestButton(match.userId));

    return card;
  }

  function buildRequestButton(receiverId) {
    var wrapper = document.createElement("div");
    wrapper.style.gridColumn = "1 / -1";

    var button = document.createElement("button");
    button.className = "btn btn-primary";
    button.textContent = "Send swap request";

    button.addEventListener("click", async function () {
      button.disabled = true;
      button.textContent = "Sending...";

      try {
        var result = await postForm("requests/send", { receiverId: receiverId });
        if (result.success) {
          button.textContent = "Request sent";
        } else {
          alert(result.message);
          button.disabled = false;
          button.textContent = "Send swap request";
        }
      } catch (err) {
        alert("Could not reach the server. Please try again.");
        button.disabled = false;
        button.textContent = "Send swap request";
      }
    });

    wrapper.appendChild(button);
    return wrapper;
  }

  function buildSkillColumn(cssClass, heading, skills) {
    var column = document.createElement("div");
    column.className = "match-column " + cssClass;

    var h4 = document.createElement("h4");
    h4.textContent = heading;
    column.appendChild(h4);

    var tags = document.createElement("div");
    tags.className = "skill-tags";

    skills.forEach(function (skill) {
      var tag = document.createElement("span");
      tag.className = "skill-tag";
      tag.textContent = skill.name;
      tags.appendChild(tag);
    });

    column.appendChild(tags);
    return column;
  }
});
