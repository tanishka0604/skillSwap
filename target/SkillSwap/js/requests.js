// SkillSwap — requests.html behavior
//
// Three tabs (received / sent / accepted), each backed by the same
// GET /requests/list?type=... endpoint. Only "received" + status
// PENDING shows Accept/Reject buttons; "sent" + PENDING shows Cancel;
// everything else is read-only history.

document.addEventListener("DOMContentLoaded", function () {
  var listEl = document.getElementById("requestList");
  var tabButtons = document.querySelectorAll(".tab-btn");
  var currentTab = "pending";

  tabButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      tabButtons.forEach(function (b) { b.classList.remove("active"); });
      btn.classList.add("active");
      currentTab = btn.getAttribute("data-tab");
      loadRequests();
    });
  });

  loadRequests();

  async function loadRequests() {
    listEl.innerHTML = "";

    try {
      var response = await fetch("requests/list?type=" + encodeURIComponent(currentTab));
      var result = await response.json();

      if (!result.success) {
        showEmpty(result.message);
        return;
      }

      if (result.requests.length === 0) {
        showEmpty("Nothing here yet.");
        return;
      }

      result.requests.forEach(function (req) {
        listEl.appendChild(buildRow(req));
      });

    } catch (err) {
      showEmpty("Could not load requests. Please try again.");
    }
  }

  function showEmpty(text) {
    var p = document.createElement("p");
    p.className = "empty";
    p.textContent = text;
    listEl.appendChild(p);
  }

  function buildRow(req) {
    var row = document.createElement("div");
    row.className = "request-row";

    var who = document.createElement("span");
    who.className = "who";
    who.textContent = req.otherUserName;
    row.appendChild(who);

    var badge = document.createElement("span");
    badge.className = "status-badge " + req.status.toLowerCase();
    badge.textContent = req.status.toLowerCase();
    row.appendChild(badge);

    var actions = document.createElement("div");
    actions.className = "actions";

    if (req.status === "PENDING" && req.direction === "RECEIVED") {
      actions.appendChild(buildActionButton("Accept", "action-accept", "requests/accept", req.id));
      actions.appendChild(buildActionButton("Reject", "action-reject", "requests/reject", req.id));
    } else if (req.status === "PENDING" && req.direction === "SENT") {
      actions.appendChild(buildActionButton("Cancel", "action-cancel", "requests/cancel", req.id));
    } else if (req.status === "ACCEPTED") {
      var chatLink = document.createElement("a");
      chatLink.className = "action-accept";
      chatLink.style.textDecoration = "none";
      chatLink.style.display = "inline-block";
      chatLink.href = "chat?requestId=" + encodeURIComponent(req.id);
      chatLink.textContent = "Chat";
      actions.appendChild(chatLink);
    }

    row.appendChild(actions);
    return row;
  }

  function buildActionButton(label, cssClass, url, requestId) {
    var button = document.createElement("button");
    button.className = cssClass;
    button.textContent = label;

    button.addEventListener("click", async function () {
      button.disabled = true;
      try {
        var result = await postForm(url, { requestId: requestId });
        if (result.success) {
          loadRequests(); // simplest way to reflect the new status: reload this tab
        } else {
          alert(result.message);
          button.disabled = false;
        }
      } catch (err) {
        alert("Could not reach the server. Please try again.");
        button.disabled = false;
      }
    });

    return button;
  }
});
