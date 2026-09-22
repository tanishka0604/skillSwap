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
 * Handles GET /profile — the protected profile page. Same pattern as
 * DashboardServlet and SkillsServlet: the real HTML lives under
 * WEB-INF/pages/ where a browser can never fetch it directly, and this
 * servlet is the only door in, guarded by a session check.
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

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

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/profile.html");
        dispatcher.forward(request, response);
    }
}
