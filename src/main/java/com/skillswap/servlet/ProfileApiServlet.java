package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Skill;
import com.skillswap.model.User;
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
 * Handles GET and POST /api/profile — the JSON API profile.js talks to.
 *
 * Notice this is one class handling TWO HTTP methods, unlike
 * RegisterServlet/LoginServlet which only ever needed doPost(). GET
 * here means "give me my current profile data" (safe, read-only —
 * a browser could even follow a link to it without side effects).
 * POST means "save these changes" (it modifies data, so it must not
 * be a GET). Same reasoning that picked doPost() for registration
 * back in Phase 2, just applied to a servlet that needs both.
 */
@WebServlet("/api/profile")
public class ProfileApiServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final SkillService skillService = new SkillService();

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

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.profileJson(user, teachSkills, learnSkills));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JsonUtil.errorMessage("You must be logged in."));
            return;
        }

        String name = request.getParameter("name");
        String bio = request.getParameter("bio");
        String profilePictureUrl = request.getParameter("profilePictureUrl");

        try {
            User updated = userService.updateProfile(userId, name, bio, profilePictureUrl);

            // The session still holds the OLD name from login (see
            // LoginServlet) — if the person just renamed themselves, we
            // update the session copy too, so a page like dashboard.html
            // that reads session attributes reflects the change right
            // away instead of showing stale data until the next login.
            request.getSession(false).setAttribute("userName", updated.getName());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.successMessage("Profile updated."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
