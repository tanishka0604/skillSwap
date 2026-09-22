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
 * Handles GET /dashboard — this is the "protected page" mechanism.
 *
 * SESSION-BASED AUTHENTICATION, explained simply:
 * A logged-in user's browser holds a cookie pointing at their session.
 * "Protecting" a page means: before showing it, check whether THIS
 * request's session has a userId stored in it (which LoginServlet put
 * there). No userId in the session → we never even ran the login logic
 * for this visitor → redirect them to the login page instead.
 *
 * Note the actual dashboard.html file lives under WEB-INF/pages/, NOT
 * directly under webapp/. Anything under WEB-INF is private — Tomcat
 * will refuse a direct browser request for /WEB-INF/pages/dashboard.html.
 * The ONLY way to reach that file is through this servlet, which means
 * the check below can never be bypassed by guessing the file's URL.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

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

        // Forward = "let another resource on the SERVER handle the rest
        // of this same request", without the browser knowing or the URL
        // changing. This is different from sendRedirect(), which tells
        // the BROWSER to make a brand-new request to a new URL.
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/dashboard.html");
        dispatcher.forward(request, response);
    }
}
