/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.user;

import com.khartec.waltz.common.exception.DuplicateKeyException;
import com.khartec.waltz.data.user.UserDao;
import com.khartec.waltz.data.user.UserRoleDao;
import com.khartec.waltz.model.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final PasswordService passwordService;


    @Autowired
    public UserService(UserDao userDao, UserRoleDao userRoleDao, PasswordService passwordService) {
        checkNotNull(userDao, "userDao must not be null");
        checkNotNull(userRoleDao, "userRoleDao must not be null");
        checkNotNull(passwordService, "passwordService must not be null");

        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.passwordService = passwordService;
    }


    public int registerNewUser(UserRegistrationRequest request) {
        String existingPassword = userDao.getPassword(request.userName());
        if (existingPassword != null) {
            throw new DuplicateKeyException("Cannot register "+request.userName()+" as already exists");
        }

        LOG.info("Registering new user: "+ request.userName());
        String passwordHash = passwordService.hashPassword(request.password());
        return userDao.create(request.userName(), passwordHash);
    }

    public boolean authenticate(LoginRequest request) {
        String existingHashedPassword = userDao.getPassword(request.userName());
        if (existingHashedPassword == null) {
            LOG.warn("Could not find " + request.userName() + " in database");
        }

        return passwordService.verifyPassword(request.password(), existingHashedPassword);
    }


    public boolean hasRole(String userName, Role... requiredRoles) {
        List<Role> userRoles = userRoleDao.getUserRoles(userName);
        return Stream
                .of(requiredRoles)
                .allMatch(requiredRole -> userRoles.contains(requiredRole));
    }


    public List<String> findAllUserNames() {
        return userDao.findAllUserNames();
    }

    public List<User> findAllUsers() {
        return userRoleDao.findAllUsers();
    }


    public User findByUserName(String userName) {
        return ImmutableUser.builder()
                .userName(userName)
                .addAllRoles(userRoleDao.getUserRoles(userName))
                .build();
    }


    public boolean updateRoles(String userName, List<Role> newRoles) {
        LOG.info("Updating roles for userName: "+userName + ", new roles: " + newRoles);
        return userRoleDao.updateRoles(userName, newRoles);
    }


    public boolean deleteUser(String userName) {
        LOG.info("Deleting user: " + userName);
        userRoleDao.deleteUser(userName);
        userDao.deleteUser(userName);
        LOG.info("Deleted user: " + userName);
        return true;
    }


    public boolean resetPassword(String userName, String password) {
        LOG.info("Resetting password for: " + userName );
        String hashedPassword = passwordService.hashPassword(password);
        return userDao.resetPassword(userName, hashedPassword) == 1;
    }


    public List<Role> getUserRoles(String userName) {
        return userRoleDao.getUserRoles(userName);
    }
}
