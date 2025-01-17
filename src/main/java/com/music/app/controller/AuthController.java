package com.music.app.controller;

import com.music.app.entity.User;
import com.music.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        // Retrieve the error message from the session
        HttpSession session = request.getSession();
        String errorMessage = (String) session.getAttribute("error");

        // Add the error message to the model for Thymeleaf rendering
        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            // Remove the error message from the session
            session.removeAttribute("error");
        }

        return "login"; // Thymeleaf login page name
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("formData")) {
            model.addAttribute("formData", new HashMap<String, String>());
        }
        return "register"; // Thymeleaf template name (register.html)
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("email") String email,
            @RequestParam("displayName") String displayName,
            @RequestParam("role") String role,
            @RequestParam("profilePicture") MultipartFile profilePicture,
            Model model, RedirectAttributes redirectAttributes) {

        Map<String, String> formData = new HashMap<>();
        formData.put("username", username);
        formData.put("email", email);
        formData.put("displayName", displayName);
        formData.put("role", role);

        // Add formData back to the model to retain user inputs
        model.addAttribute("formData", formData);

        // Validations
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username is required.");
            return "register";
        }


        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required.");
            return "register";
        }

        if(userService.findByUsernameOrEmail(username, email) != null){
            model.addAttribute("error", "Email or username already used.");
            return "register";
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            model.addAttribute("error", "Display Name is required.");
            return "register";
        }
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }
        if (password.trim().length() < 4) {
            model.addAttribute("error", "Password must be at least 4 characters long.");
            return "register";
        }

        if (!profilePicture.isEmpty()) {
            String contentType = profilePicture.getContentType();
            long fileSize = profilePicture.getSize();

            // Check if file is an image
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("error", "Uploaded file must be an image.");
                return "register";
            }

            // Check if file size is between 10 KB and 15 MB
            if (fileSize < 10 * 1024 || fileSize > 15 * 1024 * 1024) {
                model.addAttribute("error", "Image size must be between 10 KB and 15 MB.");
                return "register";
            }
        }


        // TODO: Save the user details and handle the profile picture
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);
        System.out.println("Profile Picture: " + (profilePicture != null ? profilePicture.getOriginalFilename() : "No file uploaded"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setRole(User.Role.valueOf(role.toUpperCase()));

        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("success", "Account created successfully! Please login.");
        return "redirect:/login";
    }
}
