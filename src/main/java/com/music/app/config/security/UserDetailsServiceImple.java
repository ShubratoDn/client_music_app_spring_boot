package com.music.app.config.security;

import com.music.app.entity.User;
import com.music.app.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class UserDetailsServiceImple implements UserDetailsService{

	@Autowired
	private UserRepo userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepo.findByUsernameOrEmail(username, username);
		if(user == null) {
			throw new UsernameNotFoundException("user Not Founds");
		}

		return new CustomUserDetails(user);
	}

}
