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
 * Handles GET /skills — the protected skill-management page. Same
 * pattern as DashboardServlet: the real HTML lives under WEB-INF/pages/
 * (unreachable directly), and this servlet is the only door in.
 */
@WebServlet("/skills")
public class SkillsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SessionUtil.getLoggedInUserId(request) == null) {
            response.sendRedirect("login.html");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/skills.html");
        dispatcher.forward(request, response);
    }
}
