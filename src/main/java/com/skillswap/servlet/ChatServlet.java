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
 * Handles GET /chat?requestId=5 — the protected chat page. Only checks
 * that someone's logged in and that a requestId was actually supplied;
 * the REAL permission check (is this an accepted request, and are you
 * one of its two participants?) happens in ChatApiServlet/MessageService
 * once the page's own JavaScript asks for the messages. This servlet's
 * job is just "let a logged-in visitor reach the chat shell" — chat.js
 * shows the real error if they don't actually belong there.
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SessionUtil.getLoggedInUserId(request) == null) {
            response.sendRedirect("login.html");
            return;
        }

        if (request.getParameter("requestId") == null) {
            response.sendRedirect("requests");
            return;
        }

        // The forward preserves the original URL, including
        // "?requestId=5" — chat.js reads it back out with
        // window.location.search once the page loads.
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/chat.html");
        dispatcher.forward(request, response);
    }
}
