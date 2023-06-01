/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.user;

import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.common.Checks;
import org.finos.waltz.data.user.UserDao;
import org.finos.waltz.data.user.UserRoleDao;
import org.finos.waltz.model.settings.Setting;
import org.finos.waltz.model.user.ImmutableLoginRequest;
import org.finos.waltz.model.user.LoginRequest;
import org.finos.waltz.model.user.PasswordResetRequest;
import org.finos.waltz.model.user.UserRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.common.StringUtilities.tokenise;


@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final PasswordService passwordService;
    private final UserRoleDao userRoleDao;
    private final SettingsService settingsService;


    @Autowired
    public UserService(UserDao userDao,
                       UserRoleDao userRoleDao,
                       PasswordService passwordService,
                       SettingsService settingsService) {
        checkNotNull(userDao, "userDao must not be null");
        checkNotNull(userRoleDao, "userRoleDao cannot be null");
        checkNotNull(passwordService, "passwordService must not be null");
        checkNotNull(settingsService, "settingsService cannot be null");

        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.passwordService = passwordService;
        this.settingsService = settingsService;
    }


    public int registerNewUser(UserRegistrationRequest request) {
        LOG.info("Registering new user: " + request.userName());
        String passwordHash = passwordService.hashPassword(request.password());
        int rc = userDao.create(request.userName(), passwordHash);
        assignDefaultRoles(request.userName());
        return rc;
    }


    public boolean authenticate(LoginRequest request) {
        String existingHashedPassword = userDao.getPassword(request.userName());
        if (existingHashedPassword == null) {
            LOG.warn("Could not find " + request.userName() + " in database");
        }

        return passwordService.verifyPassword(request.password(), existingHashedPassword);
    }


    public boolean deleteUser(String userName) {
        LOG.info("Deleting user: " + userName);
        userDao.deleteUser(userName);
        LOG.info("Deleted user: " + userName);
        return true;
    }


    public List<String> findAllUserNames() {
        return userDao.findAllUserNames();
    }


    public boolean resetPassword(PasswordResetRequest resetRequest, boolean validate) {
        LOG.info("Resetting password for: " + resetRequest.userName() );
        if (validate) {
            LoginRequest loginRequest = ImmutableLoginRequest.builder()
                    .password(resetRequest.currentPassword())
                    .userName(resetRequest.userName())
                    .build();
            if (! authenticate(loginRequest)) {
                return false;
            }
        }
        String hashedPassword = passwordService.hashPassword(resetRequest.newPassword());
        return userDao.resetPassword(resetRequest.userName(), hashedPassword) == 1;
    }


    /**
     * Returns true if user is new, false if already existed
     * @param username
     * @return
     */
    public boolean ensureExists(String username) {
        Checks.checkNotEmpty(username, "Cannot ensure an empty username exists");
        int rc = userDao.create(username, passwordService.hashPassword("temp4321"));
        boolean isNewUser = (rc == 1);

        if (isNewUser) {
            assignDefaultRoles(username);
        }

        return isNewUser;
    }


    private void assignDefaultRoles(String username) {
        Checks.checkNotEmpty(username, "username cannot be empty");
        Setting setting = settingsService.getByName(SettingsService.DEFAULT_ROLES_KEY);
        if (setting != null ) {
            setting.value()
                    .map(s -> tokenise(s, ","))
                    .ifPresent(roles -> userRoleDao.updateRoles(username, fromCollection(roles)));

        }
    }

}
