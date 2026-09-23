package com.skillswap.servlet;

import com.skillswap.util.SessionUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles GET /matches — the protected matches page. Same door-guard
 * pattern as DashboardServlet, SkillsServlet, and ProfileServlet.
 */
@WebServlet("/matches")
public class MatchesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SessionUtil.getLoggedInUserId(request) == null) {
            response.sendRedirect("login.html");
            return;
        }

        // This page is only for logged-in users, so the browser must not cache
        // it — otherwise Back / revisiting after logout shows a stale copy
        // instead of going through the login check above.
        response.setHeader("Cache-Control", "no-store");

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/matches.html");
        dispatcher.forward(request, response);
    }
}
