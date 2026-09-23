// SkillSwap — dashboard.html behavior
//
// One fetch to GET /api/dashboard fills in every panel on the page.
// As with the other pages, anything built from data (names, skill
// names, message content) is inserted with textContent/createElement,
// never innerHTML, so nothing a user typed can be parsed as markup.

document.addEventListener("DOMContentLoaded", async function () {
  try {
    var response = await fetch("api/dashboard");
    var data = await response.json();

    if (!data.success) {
      document.getElementById("greeting").textContent = data.message;
      return;
    }

    document.getElementById("greeting").textContent = "Welcome back, " + data.name;

    document.getElementById("teachCount").textContent = data.teachSkills.length;
    document.getElementById("learnCount").textContent = data.learnSkills.length;
    document.getElementById("matchCount").textContent = data.totalMatches;
    document.getElementById("pendingCount").textContent = data.totalPending;

    renderMatches(data.suggestedMatches);
    renderRequests("pendingList", data.pendingRequests, "No pending requests.");
    renderRequests("acceptedList", data.acceptedSwaps, "No accepted swaps yet.");
    renderMessages(data.recentMessages);

  } catch (err) {
    document.getElementById("greeting").textContent = "Could not load your dashboard.";
  }

  function renderMatches(matches) {
    var container = document.getElementById("matchesList");
    container.innerHTML = "";

    if (matches.length === 0) {
      container.appendChild(emptyRow("No suggested matches yet — add more skills to find one."));
      return;
    }

    matches.forEach(function (match) {
      var row = document.createElement("div");
      row.className = "dash-row";

      var name = document.createElement("span");
      name.className = "name";
      name.textContent = match.name;
      row.appendChild(name);

      var sub = document.createElement("span");
      sub.className = "sub";
      sub.textContent = match.theyTeach.length + " skill(s) they can teach you";
      row.appendChild(sub);

      container.appendChild(row);
    });
  }

  function renderRequests(containerId, requests, emptyText) {
    var container = document.getElementById(containerId);
    container.innerHTML = "";

    if (requests.length === 0) {
      container.appendChild(emptyRow(emptyText));
      return;
    }

    requests.forEach(function (req) {
      var row = document.createElement("div");
      row.className = "dash-row";

      var name = document.createElement("span");
      name.className = "name";
      name.textContent = req.otherUserName;
      row.appendChild(name);

      var sub = document.createElement("span");
      sub.className = "sub";
      sub.textContent = req.status.toLowerCase();
      row.appendChild(sub);

      container.appendChild(row);
    });
  }

  function renderMessages(messages) {
    var container = document.getElementById("messagesList");
    container.innerHTML = "";

    if (messages.length === 0) {
      container.appendChild(emptyRow("No messages yet."));
      return;
    }

    messages.forEach(function (message) {
      var row = document.createElement("div");
      row.className = "dash-row dash-message";
      row.style.flexDirection = "column";
      row.style.alignItems = "flex-start";

      var top = document.createElement("div");
      top.style.display = "flex";
      top.style.justifyContent = "space-between";
      top.style.width = "100%";

      var name = document.createElement("span");
      name.className = "name";
      name.textContent = message.senderName;
      top.appendChild(name);

      var link = document.createElement("a");
      link.href = "chat?requestId=" + encodeURIComponent(message.requestId);
      link.textContent = "Reply";
      link.style.fontSize = "0.8rem";
      link.style.fontWeight = "600";
      link.style.color = "var(--amber-dark)";
      top.appendChild(link);

      row.appendChild(top);

      var content = document.createElement("span");
      content.className = "content";
      content.textContent = message.content;
      row.appendChild(content);

      container.appendChild(row);
    });
  }

  function emptyRow(text) {
    var p = document.createElement("p");
    p.className = "dash-empty";
    p.textContent = text;
    return p;
  }
});
