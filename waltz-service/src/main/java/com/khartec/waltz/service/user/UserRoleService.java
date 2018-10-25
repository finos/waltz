/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.user.UserRoleDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.user.ImmutableUser;
import com.khartec.waltz.model.user.Role;
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



    public boolean hasRole(String userName, Role... requiredRoles) {
        Set<Role> userRoles = userRoleDao.getUserRoles(userName);
        return userRoles.containsAll(SetUtilities.fromArray(requiredRoles));
    }


    public boolean hasAnyRole(String userName, Role... requiredRoles) {
        Set<Role> userRoles = userRoleDao.getUserRoles(userName);
        Set<Role> requiredRolesSet = SetUtilities.fromArray(requiredRoles);

        return ! SetUtilities.intersection(userRoles, requiredRolesSet)
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


    public boolean updateRoles(String userName, String targetUserName, List<Role> newRoles) {
        LOG.info("Updating roles for userName: " + targetUserName + ", new roles: " + newRoles);

        Person person = personService.getPersonByUserId(targetUserName);
        if(person == null) {
            LOG.warn(targetUserName + " does not exist, cannot update roles");
            return false;
        }

        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(EntityReference.mkRef(EntityKind.PERSON, person.id().get()))
                .severity(Severity.INFORMATION)
                .userId(userName)
                .message(String.format(
                        "Roles for %s updated to %s",
                        targetUserName,
                        newRoles
                        ))
                .childKind(Optional.empty())
                .operation(Operation.UPDATE)
                .build();
        changeLogService.write(logEntry);

        return userRoleDao.updateRoles(targetUserName, newRoles);
    }


    public Set<Role> getUserRoles(String userName) {
        return userRoleDao.getUserRoles(userName);
    }

}
