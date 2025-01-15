package com.music.app.service;


import com.music.app.config.security.CustomUserDetails;
import com.music.app.entity.User;
import com.music.app.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepo;

    public User findByUsernameOrEmail(String username, String email) {
        return userRepo.findByUsernameOrEmail(username, email);
    }
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

}
