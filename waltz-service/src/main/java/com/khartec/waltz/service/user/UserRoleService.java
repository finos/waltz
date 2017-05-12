/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.user;

import com.khartec.waltz.data.user.UserRoleDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.fromArray;
import static com.khartec.waltz.service.user.RoleUtilities.getRequiredRoleForEntityKind;

/**
 * Created by dwatkins on 30/03/2016.
 */
@Service
public class UserRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleService.class);

    private final UserRoleDao userRoleDao;


    @Autowired
    public UserRoleService(UserRoleDao userRoleDao) {
        checkNotNull(userRoleDao, "userRoleDao must not be null");

        this.userRoleDao = userRoleDao;
    }



    public boolean hasRole(String userName, Role... requiredRoles) {
        List<Role> userRoles = userRoleDao.getUserRoles(userName);
        return userRoles.containsAll(fromArray(requiredRoles));
    }


    public List<User> findAllUsers() {
        return userRoleDao.findAllUsers();
    }


    public User findForUserId(String userId) {
        return ImmutableUser.builder()
                .userName(userId)
                .addAllRoles(userRoleDao.getUserRoles(userId))
                .build();
    }


    public boolean updateRoles(String userName, List<Role> newRoles) {
        LOG.info("Updating roles for userName: "+userName + ", new roles: " + newRoles);
        return userRoleDao.updateRoles(userName, newRoles);
    }


    public List<Role> getUserRoles(String userName) {
        return userRoleDao.getUserRoles(userName);
    }

}
