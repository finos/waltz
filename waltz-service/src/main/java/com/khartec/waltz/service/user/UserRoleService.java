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

package com.khartec.waltz.service.user;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.user.UserRoleDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.model.user.User;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.person.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.sort;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.lang.String.format;

/**
 * Created by dwatkins on 30/03/2016.
 */
@Service
public class UserRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleService.class);

    private final UserRoleDao userRoleDao;

    private final ChangeLogService changeLogService;

    private final PersonService personService;


    @Autowired
    public UserRoleService(UserRoleDao userRoleDao,
                           ChangeLogService changeLogService, PersonService personService) {
        this.personService = personService;
        checkNotNull(userRoleDao, "userRoleDao must not be null");

        this.userRoleDao = userRoleDao;
        this.changeLogService = changeLogService;
    }



    public boolean hasRole(String userName, SystemRole... requiredRoles) {
        return hasRole(userName, SetUtilities.map(asSet(requiredRoles), Enum::name));
    }


    public boolean hasRole(String userName, String... requiredRoles) {
        return hasRole(userName, SetUtilities.fromArray(requiredRoles));
    }


    public boolean hasRole(String userName, Set<String> requiredRoles) {
        Set<String> userRoles = userRoleDao.getUserRoles(userName);
        return userRoles.containsAll(requiredRoles);
    }


    public boolean hasAnyRole(String userName, SystemRole... requiredRoles) {
        return hasAnyRole(userName, SetUtilities.map(asSet(requiredRoles), Enum::name));
    }


    public boolean hasAnyRole(String userName, String... requiredRoles) {
        return hasAnyRole(userName, SetUtilities.fromArray(requiredRoles));
    }

    public boolean hasAnyRole(String userName, Set<String> requiredRoles) {
        Set<String> userRoles = userRoleDao.getUserRoles(userName);
        return ! SetUtilities.intersection(userRoles, requiredRoles)
                    .isEmpty();
    }


    public List<User> findAllUsers() {
        return userRoleDao.findAllUsers();
    }


    public User getByUserId(String userId) {
        return ImmutableUser.builder()
                .userName(userId)
                .addAllRoles(userRoleDao.getUserRoles(userId))
                .build();
    }


    public boolean updateRoles(String userName, String targetUserName, Set<String> newRoles) {
        LOG.info("Updating roles for userName: {}, new roles: {}", targetUserName, newRoles);

        Person person = personService.getPersonByUserId(targetUserName);
        if(person == null) {
            LOG.warn("{} does not exist, cannot create audit log for role updates", targetUserName);
        } else {
            ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                    .parentReference(mkRef(EntityKind.PERSON, person.id().get()))
                    .severity(Severity.INFORMATION)
                    .userId(userName)
                    .message(format(
                            "Roles for %s updated to %s",
                            targetUserName,
                            sort(newRoles)
                    ))
                    .childKind(Optional.empty())
                    .operation(Operation.UPDATE)
                    .build();
            changeLogService.write(logEntry);
        }

        return userRoleDao.updateRoles(targetUserName, newRoles);
    }


    public Set<String> getUserRoles(String userName) {
        return userRoleDao.getUserRoles(userName);
    }

}
