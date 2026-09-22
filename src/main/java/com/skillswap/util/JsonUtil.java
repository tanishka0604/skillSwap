package com.skillswap.util;

import com.skillswap.model.MatchResult;
import com.skillswap.model.Message;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;

import java.util.List;

/**
 * A tiny helper for writing simple JSON responses without pulling in an
 * external JSON library like Gson or Jackson.
 *
 * We're keeping this deliberately narrow — flat success/error messages,
 * plus (as of Phase 5) a JSON array of skills — because that's the only
 * shape our Servlets need so far. A real project would use a proper
 * JSON library once the responses get more complex; this is a fine
 * bridge while we're still keeping dependencies to a minimum.
 */
public class JsonUtil {

    private JsonUtil() {
    }

    public static String successMessage(String message) {
        return "{\"success\": true, \"message\": \"" + escape(message) + "\"}";
    }

    public static String errorMessage(String message) {
        return "{\"success\": false, \"message\": \"" + escape(message) + "\"}";
    }

    public static String skillsArray(List<Skill> skills) {
        return "{\"success\": true, \"skills\": " + skillsArrayFragment(skills) + "}";
    }

    /**
     * Builds a full profile response:
     * {"success": true, "profile": {"id":.., "name":"..", "email":"..",
     *  "bio":"..", "profilePictureUrl":"..", "teachSkills":[...], "learnSkills":[...]}}
     *
     * Reuses skillsArrayFragment() (extracted below) instead of repeating
     * that loop a third time — the same reasoning as UserDAO's mapRow()
     * helper: one block of logic, one place it's written.
     */
    public static String profileJson(User user, List<Skill> teachSkills, List<Skill> learnSkills) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\": true, \"profile\": {");
        sb.append("\"id\": ").append(user.getId());
        sb.append(", \"name\": \"").append(escape(user.getName())).append("\"");
        sb.append(", \"email\": \"").append(escape(user.getEmail())).append("\"");
        sb.append(", \"bio\": \"").append(escape(user.getBio())).append("\"");
        sb.append(", \"profilePictureUrl\": \"").append(escape(user.getProfilePictureUrl())).append("\"");
        sb.append(", \"teachSkills\": ").append(skillsArrayFragment(teachSkills));
        sb.append(", \"learnSkills\": ").append(skillsArrayFragment(learnSkills));
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Builds: {"success": true, "matches": [{"userId":.., "name":..,
     *  "bio":.., "theyTeach":[...], "theyWant":[...]}]}
     */
    public static String matchesJson(List<MatchResult> matches) {
        return "{\"success\": true, \"matches\": " + matchesArrayFragment(matches) + "}";
    }

    /**
     * Builds {"success": true, "requests": [{"id":.., "otherUserId":..,
     *  "otherUserName":.., "status":.., "direction":"SENT"|"RECEIVED"}]}
     *
     * One method serves all three request lists (pending received, sent,
     * accepted swaps) because "who is the other person" is always the
     * same question relative to whoever is VIEWING the list: if I'm the
     * sender, the other person is the receiver, and vice versa. Passing
     * in viewerUserId lets this method work that out once, instead of
     * needing a slightly different JSON shape for each of the three pages.
     */
    public static String requestsJson(List<SkillRequest> requests, int viewerUserId) {
        return "{\"success\": true, \"requests\": " + requestsArrayFragment(requests, viewerUserId) + "}";
    }

    /**
     * Builds: {"success": true, "otherUserId":.., "otherUserName":..,
     *  "messages": [{"senderId":.., "senderName":.., "content":.., "sentAt":..}]}
     */
    public static String chatJson(User otherUser, List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\": true");
        sb.append(", \"otherUserId\": ").append(otherUser.getId());
        sb.append(", \"otherUserName\": \"").append(escape(otherUser.getName())).append("\"");
        sb.append(", \"messages\": [");

        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("{\"senderId\": ").append(m.getSenderId());
            sb.append(", \"senderName\": \"").append(escape(m.getSenderName())).append("\"");
            sb.append(", \"content\": \"").append(escape(m.getContent())).append("\"");
            sb.append(", \"sentAt\": \"").append(m.getSentAt()).append("\"");
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    // Builds just the "[{...}, {...}]" part, with no surrounding wrapper
    // object — pulled out of skillsArray() so profileJson() can reuse it
    // for two different lists in the same response.
    private static String skillsArrayFragment(List<Skill> skills) {
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < skills.size(); i++) {
            Skill skill = skills.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("{\"id\": ").append(skill.getId())
              .append(", \"name\": \"").append(escape(skill.getName())).append("\"")
              .append(", \"category\": \"").append(skill.getCategory().name()).append("\"}");
        }

        sb.append("]");
        return sb.toString();
    }

    // Builds just the "[{...}, {...}]" part for a list of matches — used
    // by both matchesJson() (the full Matches page) and dashboardJson()
    // (a short "suggested matches" preview), same DRY reasoning as
    // skillsArrayFragment.
    private static String matchesArrayFragment(List<MatchResult> matches) {
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < matches.size(); i++) {
            MatchResult match = matches.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("{\"userId\": ").append(match.getMatchedUser().getId());
            sb.append(", \"name\": \"").append(escape(match.getMatchedUser().getName())).append("\"");
            sb.append(", \"bio\": \"").append(escape(match.getMatchedUser().getBio())).append("\"");
            sb.append(", \"theyTeach\": ").append(skillsArrayFragment(match.getTheyTeachThatIWant()));
            sb.append(", \"theyWant\": ").append(skillsArrayFragment(match.getTheyWantThatITeach()));
            sb.append("}");
        }

        sb.append("]");
        return sb.toString();
    }

    // Builds just the "[{...}, {...}]" part for a list of requests —
    // used by both requestsJson() (the full Requests page) and
    // dashboardJson() (short previews of pending/accepted).
    private static String requestsArrayFragment(List<SkillRequest> requests, int viewerUserId) {
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < requests.size(); i++) {
            SkillRequest r = requests.get(i);
            if (i > 0) {
                sb.append(", ");
            }

            boolean viewerIsSender = r.getSenderId() == viewerUserId;
            int otherUserId = viewerIsSender ? r.getReceiverId() : r.getSenderId();
            String otherUserName = viewerIsSender ? r.getReceiverName() : r.getSenderName();

            sb.append("{\"id\": ").append(r.getId());
            sb.append(", \"otherUserId\": ").append(otherUserId);
            sb.append(", \"otherUserName\": \"").append(escape(otherUserName)).append("\"");
            sb.append(", \"status\": \"").append(r.getStatus().name()).append("\"");
            sb.append(", \"direction\": \"").append(viewerIsSender ? "SENT" : "RECEIVED").append("\"");
            sb.append("}");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Builds the dashboard's one big summary response. Every piece here
     * is something Phases 6-9 already know how to produce — this method
     * (and DashboardApiServlet, which calls it) don't compute anything
     * new, they just COMPOSE existing results into one JSON object so
     * the dashboard page can render everything with a single request.
     */
    public static String dashboardJson(String name, List<Skill> teachSkills, List<Skill> learnSkills,
                                        List<MatchResult> suggestedMatches, int totalMatches,
                                        List<SkillRequest> pendingRequests, int totalPending,
                                        List<SkillRequest> acceptedSwaps, int totalAccepted,
                                        List<Message> recentMessages, int viewerUserId) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\": true");
        sb.append(", \"name\": \"").append(escape(name)).append("\"");
        sb.append(", \"teachSkills\": ").append(skillsArrayFragment(teachSkills));
        sb.append(", \"learnSkills\": ").append(skillsArrayFragment(learnSkills));
        sb.append(", \"suggestedMatches\": ").append(matchesArrayFragment(suggestedMatches));
        sb.append(", \"totalMatches\": ").append(totalMatches);
        sb.append(", \"pendingRequests\": ").append(requestsArrayFragment(pendingRequests, viewerUserId));
        sb.append(", \"totalPending\": ").append(totalPending);
        sb.append(", \"acceptedSwaps\": ").append(requestsArrayFragment(acceptedSwaps, viewerUserId));
        sb.append(", \"totalAccepted\": ").append(totalAccepted);

        sb.append(", \"recentMessages\": [");
        for (int i = 0; i < recentMessages.size(); i++) {
            Message m = recentMessages.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("{\"requestId\": ").append(m.getRequestId());
            sb.append(", \"senderName\": \"").append(escape(m.getSenderName())).append("\"");
            sb.append(", \"content\": \"").append(escape(m.getContent())).append("\"");
            sb.append(", \"sentAt\": \"").append(m.getSentAt()).append("\"");
            sb.append("}");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // Escapes characters that would otherwise break the JSON string,
    // e.g. if a message itself contained a double quote, or a newline
    // typed into the chat box or bio textarea (raw control characters
    // are not allowed inside a JSON string).
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
