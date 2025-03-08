package com.pj.portfolio.service.interfaces;

import com.pj.portfolio.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User findByUserName(String userName);
}
