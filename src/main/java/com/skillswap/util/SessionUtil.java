package com.skillswap.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A small METHOD-only class (all static, like PasswordUtil and JsonUtil)
 * that centralizes one question every protected servlet needs to ask:
 * "does this request belong to a logged-in user, and if so, which one?"
 *
 * Before this class existed, that check — request.getSession(false),
 * then session.getAttribute("userId") — was written out by hand inside
 * DashboardServlet. Copy-pasting it into every new servlet (SkillsServlet,
 * AddSkillServlet, RemoveSkillServlet, ...) would mean five places to
 * update if the session's attribute name ever changed. Pulling it into
 * one method is the same idea as UserDAO holding all the SQL: one
 * source of truth for one specific job.
 */
public class SessionUtil {

    private SessionUtil() {
    }

    /**
     * Returns the logged-in user's id, or null if nobody is logged in.
     * Returning null (instead of throwing an exception) puts the "what
     * should happen if not logged in?" decision back in each servlet's
     * hands — a page might redirect, an API endpoint might return a
     * JSON error. Neither behavior belongs baked into this helper.
     */
    public static Integer getLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute("userId");
    }
}
