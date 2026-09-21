package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.service.SkillRequestService;
import com.skillswap.util.JsonUtil;
import com.skillswap.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles POST /requests/send. Expects a "receiverId" form field —
 * matches.js calls this when someone clicks "Send swap request" on a
 * match card.
 */
@WebServlet("/requests/send")
public class SendRequestServlet extends HttpServlet {

    private final SkillRequestService requestService = new SkillRequestService();

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
            int receiverId = Integer.parseInt(request.getParameter("receiverId"));
            requestService.sendRequest(userId, receiverId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.successMessage("Request sent."));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage("Invalid user id."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
