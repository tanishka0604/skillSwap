package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Message;
import com.skillswap.model.User;
import com.skillswap.service.MessageService;
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
 * Handles GET and POST /api/chat?requestId=5 — same two-methods-one-URL
 * shape as ProfileApiServlet from Phase 6: GET reads the conversation,
 * POST adds a message to it.
 */
@WebServlet("/api/chat")
public class ChatApiServlet extends HttpServlet {

    private final MessageService messageService = new MessageService();

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
            int requestId = Integer.parseInt(request.getParameter("requestId"));

            User otherUser = messageService.getOtherParticipant(requestId, userId);
            List<Message> messages = messageService.getMessages(requestId, userId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.chatJson(otherUser, messages));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage("Invalid request id."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }

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

        try {
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            String content = request.getParameter("content");

            messageService.sendMessage(requestId, userId, content);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.successMessage("Message sent."));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage("Invalid request id."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
