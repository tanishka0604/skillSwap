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
import java.io.IOException;

/**
 * A Servlet is a Java class that handles one HTTP endpoint. Tomcat keeps
 * a single instance of this class alive and calls its doPost() method
 * every time a POST request arrives at "/register" (see @WebServlet below,
 * which is an alternative to declaring the mapping in web.xml).
 *
 *   HttpServletRequest  = everything about the incoming request: form
 *                         fields, headers, the URL, etc.
 *   HttpServletResponse = the object we write our reply into — status
 *                         code, headers, and body.
 *
 * doPost() specifically handles POST requests. We use POST (not doGet())
 * for registration because we're changing server state (creating a row)
 * and because form data shouldn't sit in a URL or browser history.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // getParameter() reads a single form field sent by the browser,
        // whether it came from a normal <form> submit or (as we're using
        // it here) a fetch() call sending "application/x-www-form-urlencoded" data.
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User newUser = userService.register(name, email, password);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                    JsonUtil.successMessage("Account created for " + newUser.getName() + ". You can now log in."));

        } catch (SkillSwapException e) {
            // Anything UserService flagged (bad input, duplicate email,
            // database trouble) — the message is already safe to show.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.errorMessage(e.getMessage()));
        }
    }
}
