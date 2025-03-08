package com.pj.portfolio.dao.interfaces;

import com.pj.portfolio.entity.User;

public interface UserDAO {
    User findByUserName(String userName);
}
