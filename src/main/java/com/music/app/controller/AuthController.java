package com.music.app.controller;

import com.music.app.entity.User;
import com.music.app.service.PlaylistService;
import com.music.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PlaylistService playlistService;


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
//            @RequestParam("role") String role,
            @RequestParam("profilePicture") MultipartFile profilePicture,
            Model model, RedirectAttributes redirectAttributes) {

        Map<String, String> formData = new HashMap<>();
        formData.put("username", username);
        formData.put("email", email);
        formData.put("displayName", displayName);
//        formData.put("role", role);

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
//        System.out.println("Role: " + role);
        System.out.println("Profile Picture: " + (profilePicture != null ? profilePicture.getOriginalFilename() : "No file uploaded"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setDisplayName(displayName);
//        user.setRole(User.Role.valueOf(role.toUpperCase()));
        user.setRole(User.Role.USER);
        // If validation passes, proceed with saving the album
        if(profilePicture != null){
            try {
                String userImageUrl = userService.saveProfilePicture(profilePicture);
                user.setProfilePictureUrl(userImageUrl);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload profile picture: " + e.getMessage());
                return "redirect:/login";
            }
        }


        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("success", "Account created successfully! Please login.");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User user = userService.getLoggedInUser();
        user.setPlaylists(playlistService.getPlaylistsByUser(user));
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/users/edit-profile/{userId}")
    public String showEditProfileX(@PathVariable("userId") Long userId, RedirectAttributes redirectAttributes) {
        User user = userService.findById(userId);
        System.out.println("User fetched by ID (" + userId + "): " + user);
        redirectAttributes.addFlashAttribute("user", user); // Pass user as flash attribute
        return "redirect:/edit-profile";
    }

    @GetMapping("/edit-profile")
    public String showEditProfile(Model model, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        System.out.println("User from request attribute: " + user);
        if (user == null) {
            user = (User) model.getAttribute("user"); // Retrieve flash attribute
            System.out.println("User from model attribute: " + user);
            if (user == null) {
                user = userService.getLoggedInUser(); // Fallback to logged-in user
                System.out.println("Logged-in user: " + user);
            }
        }
        model.addAttribute("user", user);
        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(
            @RequestParam("id") Long id,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("displayName") String displayName,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            Model model, RedirectAttributes redirectAttributes) {

        Map<String, String> formData = new HashMap<>();
        formData.put("username", username);
        formData.put("email", email);
        formData.put("displayName", displayName);
        formData.put("role", role);

        // Add formData back to the model to retain user inputs
        model.addAttribute("user", formData);


//        User user = userService.getLoggedInUser();

        User user = userService.findById(id);

        User dbUsername = userService.findByUsername(username);
        User dbEmail = userService.findByEmail(email);

        if ( dbUsername != null && !user.getUsername().equalsIgnoreCase(dbUsername.getUsername())) {
            model.addAttribute("error", "Username already used, try another username");
            return "edit-profile";
        }

        if(dbEmail != null && !user.getEmail().equalsIgnoreCase(dbEmail.getEmail())){
            model.addAttribute("error", "Email already used, try another email");
            return "edit-profile";
        }


        // Update user details
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName);
        if(role != null){
            user.setRole(User.Role.valueOf(role.toUpperCase()));
        }


        // Validate and update password
        if (password != null && !password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("user", user);
                return "edit-profile";
            }
        }

        // Validate and update profile picture
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String contentType = profilePicture.getContentType();
            long fileSize = profilePicture.getSize();

            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("error", "Uploaded file must be an image.");
                model.addAttribute("user", user);
                return "edit-profile";
            }

            if (fileSize < 10 * 1024 || fileSize > 15 * 1024 * 1024) {
                model.addAttribute("error", "Image size must be between 10 KB and 15 MB.");
                model.addAttribute("user", user);
                return "edit-profile";
            }

            try {
                String profilePictureUrl = userService.saveProfilePicture(profilePicture);
                user.setProfilePictureUrl(profilePictureUrl);
            } catch (Exception e) {
                model.addAttribute("error", "Failed to upload profile picture: " + e.getMessage());
                model.addAttribute("user", user);
                return "edit-profile";
            }
        }


        if(password != null && !password.isEmpty() ){
            user.setPassword(passwordEncoder.encode(password));
        }

        // Save updated user details
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }


    // Controller
    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String listUsers(Model model) {
        return "users";
    }

    @GetMapping("/users/search")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> searchUsers(@RequestParam String query) {
        return userService.searchUsers(query);
    }


}
