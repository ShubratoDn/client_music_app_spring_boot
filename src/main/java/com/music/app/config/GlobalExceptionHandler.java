package com.music.app.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

//    // Handle NoResourceFoundException
//    @ExceptionHandler(NoResourceFoundException.class)
//    public String handleNoResourceFoundException(NoResourceFoundException ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("status", HttpStatus.NOT_FOUND.value());
//        response.put("error", "Resource Not Found");
//        response.put("message", ex.getMessage());
//        response.put("path", "/error");
//
//        ex.printStackTrace();
//
//        return "404";
//    }
//
//    // Generic Exception Handler
//    @ExceptionHandler(Exception.class)
//    public String handleGenericException(Exception ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//        response.put("error", "Internal Server Error");
//        response.put("message", ex.getMessage());
//
//        ex.printStackTrace();
//
//        return "500";
//    }


    // Generic Exception Handler
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "403 Forbidden");
        response.put("message", ex.getMessage());

        ex.printStackTrace();

        return "page_403";
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex, Model model) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "404 Not Found");
        response.put("message", ex.getMessage());

        model.addAttribute("exception", response);

        ex.printStackTrace();

        return "page_404";
    }
}
