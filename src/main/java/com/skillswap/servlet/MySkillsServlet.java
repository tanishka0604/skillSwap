package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Skill;
import com.skillswap.service.SkillService;
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
 * Handles GET /skills/mine?type=TEACH (or type=LEARN).
 *
 * This is a JSON API endpoint rather than a page — skills.js calls it
 * with fetch() to fill in the "Skills I teach" / "Skills I want to
 * learn" lists without a full page reload. Same idea as register.js
 * and login.js talking to RegisterServlet/LoginServlet in Phase 2.
 */
@WebServlet("/skills/mine")
public class MySkillsServlet extends HttpServlet {

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

        String type = request.getParameter("type");

        try {
            List<Skill> skills = "LEARN".equalsIgnoreCase(type)
                    ? skillService.getSkillsToLearn(userId)
                    : skillService.getSkillsToTeach(userId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.skillsArray(skills));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
