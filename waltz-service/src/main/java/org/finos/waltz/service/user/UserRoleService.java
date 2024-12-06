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

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.role.RoleDao;
import org.finos.waltz.data.user.UserRoleDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.role.Role;
import org.finos.waltz.model.user.*;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.person.PersonService;
import org.finos.waltz.service.settings.SettingsService;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.CollectionUtilities.sort;
import static org.finos.waltz.common.ListUtilities.getOrDefault;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.tokenise;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * The user role service provides a number of methods for managing user / roles combinations.
 * The service is responsible for:
 * <ul>
 *     <li>Verify users have certain roles</li>
 *     <li>Listing users and their roles</li>
 *     <li>Updating user roles</li>
 * </ul>
 */
@Service
public class UserRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleService.class);
    private static final String FOUR_EYE_CHECK_SETTINGS_KEY = "feature.user-roles.four-eye-check";

    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;
    private final PersonDao personDao;
    private final ChangeLogService changeLogService;
    private final PersonService personService;
    private final SettingsService settingsService;


    @Autowired
    public UserRoleService(UserRoleDao userRoleDao,
                           RoleDao roleDao,
                           PersonDao personDao, ChangeLogService changeLogService,
                           PersonService personService,
                           SettingsService settingsService) {
        checkNotNull(personDao, "personDao must not be null");
        checkNotNull(userRoleDao, "userRoleDao must not be null");
        checkNotNull(roleDao, "roleDao must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(personService, "personService must not be null");
        checkNotNull(settingsService, "settingsService must not be null");

        this.userRoleDao = userRoleDao;
        this.roleDao = roleDao;
        this.changeLogService = changeLogService;
        this.personService = personService;
        this.personDao = personDao;
        this.settingsService = settingsService;
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


    public boolean hasAnyRole(String userName, Set<String> requiredRoles) {
        Set<String> userRoles = userRoleDao.getUserRoles(userName);
        return ! SetUtilities.intersection(userRoles, requiredRoles)
                    .isEmpty();
    }


    public Set<User> findAllUsers() {
        return userRoleDao.findAllUsers();
    }


    public Set<User> findUsersForRole(Long roleId) {
        return userRoleDao.findUsersForRole(roleId);
    }

    public User getByUserId(String userId) {
        return ImmutableUser.builder()
                .userName(userId)
                .addAllRoles(userRoleDao.getUserRoles(userId))
                .build();
    }


    public int updateRoles(String userName, String targetUserName, UpdateRolesCommand command) throws IllegalArgumentException{
        LOG.info("Updating roles for userName: {}, new roles: {}", targetUserName, command.roles());

        Person person = personService.getPersonByUserId(targetUserName);
        if(person == null) {
            LOG.warn("{} does not exist, cannot create audit log for role updates", targetUserName);
        } else {
            Boolean hasSetting = settingsService.getByName(FOUR_EYE_CHECK_SETTINGS_KEY) != null;
            Boolean hasFourEyeCheck = false; // by default administrators can modify their own roles

            if(hasSetting) {
                hasFourEyeCheck = Boolean.valueOf(settingsService.getByName(FOUR_EYE_CHECK_SETTINGS_KEY)
                        .value()
                        .orElse("false"));
            }

            Boolean currentUserIsTargetUser = StringUtilities.safeEq(userName, targetUserName);

            if(hasFourEyeCheck && currentUserIsTargetUser) {
                throw new IllegalArgumentException("Cannot modify own roles.");
            }

            ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                    .parentReference(mkRef(EntityKind.PERSON, person.id().get()))
                    .severity(Severity.INFORMATION)
                    .userId(userName)
                    .message(format(
                            "Roles for %s updated to %s.  Comment: %s",
                            targetUserName,
                            sort(command.roles()),
                            StringUtilities.ifEmpty(command.comment(), "none")))
                    .childKind(Optional.empty())
                    .operation(Operation.UPDATE)
                    .build();
            changeLogService.write(logEntry);
        }

        return userRoleDao.updateRoles(targetUserName, command.roles());
    }


    public Set<String> getUserRoles(String userName) {
        return userRoleDao.getUserRoles(userName);
    }


    public List<BulkUserOperationRowPreview> bulkUploadPreview(BulkUploadMode mode,
                                                               List<String> lines,
                                                               String username) {
        if (isEmpty(lines)) {
            return Collections.emptyList();
        }

        return mkBulkOperationPreviews(lines);
    }



    public int bulkUpload(BulkUploadMode mode,
                          List<String> lines,
                          String username) {
        if (isEmpty(lines)) {
            return 0;
        }

        Set<Tuple3<String, String,String>> usersAndRolesToUpdate = mkBulkOperationPreviews(lines)
                .stream()
                .filter(p -> p.status() == BulkUserOperationRowPreview.ResolutionStatus.OK)
                .map(d -> tuple(d.resolvedUser(), d.resolvedRole(),d.resolvedComment()))
                .collect(toSet());

        handleBulkChangelog(usersAndRolesToUpdate,username);
        
        Set<Tuple2<String, String>> usernamesAndRoles = usersAndRolesToUpdate.stream().map(t -> tuple(t.v1, t.v2)).collect(toSet());

        switch (mode) {
            case ADD_ONLY:
                return userRoleDao.addRoles(usernamesAndRoles);
            case REMOVE_ONLY:
                return userRoleDao.removeRoles(usernamesAndRoles);
            case REPLACE:
                return userRoleDao.replaceRoles(usernamesAndRoles);
            default:
                throw new UnsupportedOperationException("Unsupported mode: " + mode);
        }
    }

    private void handleBulkChangelog(Set<Tuple3<String, String, String>> xs, String username) {
        Set<ChangeLog> changeLog = xs.stream().map(t -> {
           Person person = personService.getPersonByUserId(t.v1);
                return ImmutableChangeLog.builder()
                        .parentReference(mkRef(EntityKind.PERSON,person.id().get() ))
                        .severity(Severity.INFORMATION)
                        .userId(username)
                        .message(format(
                                "Role for %s updated to %s.  Comment: %s",
                                t.v1,
                                t.v2,
                                StringUtilities.ifEmpty(t.v3, "none")))
                        .childKind(Optional.empty())
                        .operation(Operation.UPDATE)
                        .build();
            }).collect(toSet());

        changeLogService.write(changeLog);

    }






    private List<BulkUserOperationRowPreview> mkBulkOperationPreviews(List<String> lines) {
        List<Tuple3<String, String, String>> parsed = parseBulkOperationLines(lines);
        Set<String> distinctPeople = SetUtilities.map(parsed, Tuple3::v1);
        Set<String> distinctRoles = SetUtilities.map(parsed, Tuple3::v2);

        //Checks for person.emails
        Set<String> unknownPeople = minus(
                distinctPeople,
                fromCollection(personDao.findAllEmails()));

        Set<String> unknownRoles = minus(
                distinctRoles,
                SetUtilities.map(
                        roleDao.findAllRoles(),
                        Role::key));

        return parsed
                .stream()
                .map(t -> ImmutableBulkUserOperationRowPreview
                        .builder()
                        .givenUser(t.v1)
                        .givenRole(t.v2)
                        .givenComment(t.v3)
                        .resolvedUser(unknownPeople.contains(t.v1) ? null : t.v1)
                        .resolvedRole(unknownRoles.contains(t.v2) ? null : t.v2)
                        .resolvedComment(t.v3)
                        .build())
                .collect(toList());
    }


    /**
     * Expects a list of lines, each one containing a username, role and comment.
     * The cells may be delimited by comma or by tab.
     *
     * @param lines
     * @return a list of tuples, each one containing a username, role and comment
     */
    private static List<Tuple3<String, String, String>> parseBulkOperationLines(List<String> lines) {
        return lines
                .stream()
                .map(cmd -> tokenise(cmd, "(,|\\t)"))
                .map(parts -> tuple(
                        getOrDefault(parts, 0, null),
                        getOrDefault(parts, 1, null),
                        getOrDefault(parts, 2, null)))
                .filter(t -> ! ("username".equalsIgnoreCase(t.v1) && "role".equalsIgnoreCase(t.v2) && "comment".equalsIgnoreCase(t.v3))) // remove header
                .filter(t -> ! (StringUtilities.isEmpty(t.v1) && StringUtilities.isEmpty(t.v2) && StringUtilities.isEmpty(t.v3))) // remove empty lines
                .collect(toList());
    }

}
