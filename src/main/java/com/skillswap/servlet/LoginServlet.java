package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.User;
import com.skillswap.service.UserService;
import com.skillswap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles POST /login.
 *
 * HttpSession, in plain terms: HTTP itself has no memory — every request
 * is a fresh, disconnected conversation, so by default the server has no
 * way to know "this next request is from the same person who just logged
 * in". A session fixes that. The first time we call request.getSession(),
 * Tomcat creates a small storage area on the SERVER for this one visitor
 * and sends the browser a cookie (JSESSIONID) containing just an ID that
 * points at it. Every later request from that browser automatically
 * carries the cookie, so request.getSession() finds the SAME storage
 * area again — that's how the server "remembers" who's logged in.
 *
 * We store the User's id and name in the session. Any other servlet
 * (like DashboardServlet) can then check session.getAttribute("userId")
 * to know whether — and who — is currently logged in.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = userService.login(email, password);

            // true = "create a new session if one doesn't already exist".
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.successMessage("Welcome back, " + user.getName() + "."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
