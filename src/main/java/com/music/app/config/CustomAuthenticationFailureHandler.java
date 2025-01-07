package com.music.app.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Invalid username or password.";

        if (exception.getMessage().contains("User account is locked")) {
            errorMessage = "Your account has been locked. Please contact support.";
        } else if (exception.getMessage().contains("User account has expired")) {
            errorMessage = "Your account has expired. Please contact support.";
        } else if (exception.getMessage().contains("User is disabled")) {
            errorMessage = "Your account is not verified. Please check your associate email to verify your account.";
        }

        request.getSession().setAttribute("error", errorMessage);
        System.out.println("\n\n\nEERR  : " + errorMessage);
        response.sendRedirect("/login?error=true");
    }
}
