package com.music.app.service;


import com.music.app.config.security.CustomUserDetails;
import com.music.app.entity.User;
import com.music.app.repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {


    @Autowired
    private UserRepo userRepo;

    public User findByUsernameOrEmail(String username, String email) {
        return userRepo.findByUsernameOrEmail(username, email);
    }

    @Transactional
    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User findByUsername(String username){
        return userRepo.findByUsername(username);
    }

    public User findByEmail(String email){
        return userRepo.findByEmail(email);
    }

    @Transactional
    public User saveUser(User user) {
        User save = userRepo.save(user);
        return save;
    }

    public User getLoggedInUser(){
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if(userDetails.getUser() != null){
            return userDetails.getUser();
        }else{
            return null;
        }
    }

    public String saveProfilePicture(MultipartFile profilePicture) throws IOException {
        String uploadDir = "src/main/resources/static/uploads/userImages/";
        String fileName = UUID.randomUUID() + "-" + profilePicture.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = profilePicture.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/userImages/" + fileName;
    }


    public List<User> searchUsers(String query) {
        return userRepo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
}
