package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.MatchResult;
import com.skillswap.service.MatchService;
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
 * Handles GET /api/matches — matches.js calls this to fill the matches
 * page. All the real work happens in MatchService; this class is, once
 * again, purely "translate HTTP in, HTTP out."
 */
@WebServlet("/api/matches")
public class MatchesApiServlet extends HttpServlet {

    private final MatchService matchService = new MatchService();

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
            List<MatchResult> matches = matchService.findMatchesForUser(userId);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.matchesJson(matches));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
