package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.MatchResult;
import com.skillswap.model.Message;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;
import com.skillswap.service.MatchService;
import com.skillswap.service.MessageService;
import com.skillswap.service.SkillRequestService;
import com.skillswap.service.SkillService;
import com.skillswap.service.UserService;
import com.skillswap.util.JsonUtil;
import com.skillswap.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Handles GET /api/dashboard.
 *
 * Notice this class doesn't do anything NEW — every list it builds
 * comes from a Service method Phases 6 through 9 already wrote and
 * already tested on their own pages. This servlet's only job is
 * COMPOSITION: call several services, trim each result down to a
 * short preview, and hand it all back as one response. That's a
 * useful shape to recognize — a "summary" or "overview" screen in a
 * real application is almost always this pattern, not new business
 * logic of its own.
 */
@WebServlet("/api/dashboard")
public class DashboardApiServlet extends HttpServlet {

    private static final int PREVIEW_LIMIT = 3;

    private final UserService userService = new UserService();
    private final SkillService skillService = new SkillService();
    private final MatchService matchService = new MatchService();
    private final SkillRequestService requestService = new SkillRequestService();
    private final MessageService messageService = new MessageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JsonUtil.errorMessage("You must be logged in."));
            return;
        }

        try {
            User user = userService.getById(userId);

            List<Skill> teachSkills = skillService.getSkillsToTeach(userId);
            List<Skill> learnSkills = skillService.getSkillsToLearn(userId);

            List<MatchResult> matches = matchService.findMatchesForUser(userId);
            List<SkillRequest> pending = requestService.getPendingReceived(userId);
            List<SkillRequest> accepted = requestService.getAcceptedSwaps(userId);
            List<Message> recentMessages = messageService.getRecentMessagesForUser(userId, PREVIEW_LIMIT);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.dashboardJson(
                    user.getName(),
                    teachSkills,
                    learnSkills,
                    firstN(matches, PREVIEW_LIMIT), matches.size(),
                    firstN(pending, PREVIEW_LIMIT), pending.size(),
                    firstN(accepted, PREVIEW_LIMIT), accepted.size(),
                    recentMessages,
                    userId
            ));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }

    // Trims a list down to at most `limit` items, without ever asking
    // for more index than the list actually has — List.subList(0, n)
    // throws IndexOutOfBoundsException if n is bigger than the list's
    // real size, so Math.min protects against that.
    private <T> List<T> firstN(List<T> list, int limit) {
        return list.subList(0, Math.min(limit, list.size()));
    }
}
