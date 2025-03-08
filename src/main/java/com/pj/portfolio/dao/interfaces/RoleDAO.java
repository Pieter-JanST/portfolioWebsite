package com.pj.portfolio.dao.interfaces;

import com.pj.portfolio.entity.Role;

public interface RoleDAO {
    public Role findRoleByName(String roleName);
}
