package com.skillswap.servlet;

import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.SkillRequest;
import com.skillswap.service.SkillRequestService;
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
 * Handles GET /requests/list?type=pending|sent|accepted.
 */
@WebServlet("/requests/list")
public class RequestsListServlet extends HttpServlet {

    private final SkillRequestService requestService = new SkillRequestService();

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

        String type = request.getParameter("type");

        try {
            List<SkillRequest> requests;
            if ("sent".equalsIgnoreCase(type)) {
                requests = requestService.getSent(userId);
            } else if ("accepted".equalsIgnoreCase(type)) {
                requests = requestService.getAcceptedSwaps(userId);
            } else {
                requests = requestService.getPendingReceived(userId);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.requestsJson(requests, userId));

        } catch (SkillSwapException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
