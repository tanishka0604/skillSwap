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

/** Handles POST /requests/cancel. Expects a "requestId" form field. */
@WebServlet("/requests/cancel")
public class CancelRequestServlet extends HttpServlet {

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
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            requestService.cancelRequest(requestId, userId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.successMessage("Request cancelled."));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage("Invalid request id."));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
