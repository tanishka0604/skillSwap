package com.skillswap.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles GET /logout.
 *
 * We use doGet() here (not doPost()) because logging out doesn't change
 * any data in the database — it only clears server-side session memory —
 * and it's convenient to trigger from a plain link (<a href="logout">).
 *
 * session.invalidate() means: destroy this session's storage area on the
 * server completely and forget the userId/userName/userEmail we stored
 * in it. The browser's JSESSIONID cookie becomes meaningless afterwards —
 * even if it sends that same cookie again, there's no session left for
 * it to point to, so request.getSession(false) will return null and any
 * "am I logged in?" check will correctly say no.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // false = "don't create a new session just to invalidate it".
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect("login.html");
    }
}
