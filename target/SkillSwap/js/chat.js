// SkillSwap — chat.html behavior
//
// Reads requestId from the URL's query string (window.location.search),
// loads the conversation from GET /api/chat, and posts new messages to
// the same URL. The session on the SERVER is what actually decides
// whether you're allowed to see this conversation — this file just
// reacts to whatever the server says (including "you're not part of
// this conversation" if someone edits the URL to a requestId that
// isn't theirs).

document.addEventListener("DOMContentLoaded", function () {
  var params = new URLSearchParams(window.location.search);
  var requestId = params.get("requestId");

  var titleEl = document.getElementById("chatTitle");
  var threadEl = document.getElementById("chatThread");
  var form = document.getElementById("chatForm");
  var input = document.getElementById("messageInput");

  loadChat();

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    var content = input.value.trim();
    if (!content) return;

    input.disabled = true;

    try {
      var result = await postForm("api/chat?requestId=" + encodeURIComponent(requestId), { content: content });

      if (result.success) {
        input.value = "";
        await loadChat(); // simplest way to show the new message: reload the thread
      } else {
        alert(result.message);
      }
    } catch (err) {
      alert("Could not reach the server. Please try again.");
    } finally {
      input.disabled = false;
      input.focus();
    }
  });

  async function loadChat() {
    try {
      var response = await fetch("api/chat?requestId=" + encodeURIComponent(requestId));
      var result = await response.json();

      if (!result.success) {
        showError(result.message);
        return;
      }

      titleEl.textContent = "Chatting with " + result.otherUserName;
      renderThread(result.messages);

    } catch (err) {
      showError("Could not load this conversation.");
    }
  }

  function renderThread(messages) {
    threadEl.innerHTML = "";

    if (messages.length === 0) {
      var empty = document.createElement("p");
      empty.className = "chat-empty";
      empty.textContent = "No messages yet — say hello!";
      threadEl.appendChild(empty);
      return;
    }

    messages.forEach(function (message) {
      threadEl.appendChild(buildBubble(message));
    });

    threadEl.scrollTop = threadEl.scrollHeight;
  }

  function buildBubble(message) {
    // "mine" vs "theirs" is decided by comparing the message's sender
    // name to the chat header's other-participant name — this page
    // never separately fetched "my own name" to compare against.
    var otherName = titleEl.textContent.replace("Chatting with ", "");
    var isTheirs = message.senderName === otherName;

    var bubble = document.createElement("div");
    bubble.className = "chat-bubble " + (isTheirs ? "theirs" : "mine");
    bubble.textContent = message.content; // textContent: never parsed as HTML

    var meta = document.createElement("span");
    meta.className = "meta";
    meta.textContent = message.senderName + " • " + formatTime(message.sentAt);
    bubble.appendChild(meta);

    return bubble;
  }

  function formatTime(isoLikeString) {
    var date = new Date(isoLikeString);
    if (isNaN(date.getTime())) return "";
    return date.toLocaleString();
  }

  function showError(message) {
    // Replaces the whole page body with a single message — used for
    // fatal cases like "you're not part of this conversation", where
    // there's nothing useful left to show alongside an empty thread.
    var chatPage = document.getElementById("chatPage");
    chatPage.innerHTML = "";

    var errorEl = document.createElement("p");
    errorEl.className = "chat-error";
    errorEl.textContent = message;
    chatPage.appendChild(errorEl);
  }
});
