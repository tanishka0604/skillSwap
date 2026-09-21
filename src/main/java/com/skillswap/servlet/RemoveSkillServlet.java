package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.service.SkillService;
import com.skillswap.util.JsonUtil;
import com.skillswap.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles POST /skills/remove. Expects form fields: skillId, type.
 */
@WebServlet("/skills/remove")
public class RemoveSkillServlet extends HttpServlet {

    private final SkillService skillService = new SkillService();

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

        String type = request.getParameter("type");
        String skillIdRaw = request.getParameter("skillId");

        try {
            int skillId = Integer.parseInt(skillIdRaw);
            skillService.removeSkill(userId, skillId, type);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.successMessage("Skill removed."));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage("Invalid skill id."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
