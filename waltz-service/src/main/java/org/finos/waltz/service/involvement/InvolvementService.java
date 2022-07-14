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

package org.finos.waltz.service.involvement;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.involvement.EntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.involvement_kind.InvolvementKind;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.involvement_kind.InvolvementKindService;
import org.finos.waltz.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.*;
import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.common.ListUtilities.applyToFirst;
import static org.finos.waltz.common.ListUtilities.newArrayList;


@Service
public class InvolvementService {


    private final ChangeLogService changeLogService;
    private final InvolvementDao involvementDao;
    private final EntityReferenceNameResolver entityReferenceNameResolver;
    private final InvolvementKindService involvementKindService;
    private final PersonDao personDao;
    private final UserRoleService userRoleService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    private Map<Long, String> involvementKindIdToNameMap;


    @Autowired
    public InvolvementService(ChangeLogService changeLogService,
                              InvolvementDao dao,
                              EntityReferenceNameResolver entityReferenceNameResolver,
                              InvolvementKindService involvementKindService,
                              PersonDao personDao,
                              UserRoleService userRoleService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dao, "involvementDao must not be null");
        checkNotNull(entityReferenceNameResolver, "entityReferenceNameResolver cannot be null");
        checkNotNull(involvementKindService, "involvementKindService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");

        this.changeLogService = changeLogService;
        this.involvementDao = dao;
        this.entityReferenceNameResolver = entityReferenceNameResolver;
        this.involvementKindService = involvementKindService;
        this.userRoleService = userRoleService;
        this.personDao = personDao;
    }


    public List<Involvement> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return time("IS.findByEntityReference", () -> involvementDao.findByEntityReference(ref));
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "employeeId cannot be empty");
        return involvementDao.findByEmployeeId(employeeId);
    }


    public Set<Long> findExistingInvolvementKindIdsForUser(EntityReference entityReference, String username) {
        checkNotEmpty(username, "username cannot be empty");
        return involvementDao.findExistingInvolvementKindIdsForUser(entityReference, username);
    }


    public List<Involvement> findAllByEmployeeId(String employeeId) {
        return involvementDao.findAllByEmployeeId(employeeId);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return time("IS.findPeopleByEntityReference", () -> involvementDao.findPeopleByEntityReference(ref));
    }


    public List<Person> findPeopleByGenericEntitySelector(IdSelectionOptions selectionOptions) {
        checkNotNull(selectionOptions, "selectionOptions cannot be null");
        GenericSelector genericSelector = genericSelectorFactory.apply(selectionOptions);
        return involvementDao.findPeopleByGenericEntitySelector(genericSelector);
    }


    public boolean addEntityInvolvement(String userId,
                                        EntityReference entityReference,
                                        EntityInvolvementChangeCommand command) {

        checkInvolvementKindIsUserSelectable(command);

        Involvement involvement = mkInvolvement(entityReference, command);
        boolean result = involvementDao.save(involvement) == 1;
        if (result) {
            logChange(entityReference, userId, command);
        }
        return result;
    }


    public boolean removeEntityInvolvement(String userId,
                                           EntityReference entityReference,
                                           EntityInvolvementChangeCommand command) {
        Involvement involvement = mkInvolvement(entityReference, command);
        boolean result = involvementDao.remove(involvement) > 0;
        if (result) {
            logChange(entityReference, userId, command);
        }
        return result;
    }


    public Collection<Involvement> findByGenericEntitySelector(IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.apply(selectionOptions);
        return involvementDao.findByGenericEntitySelector(genericSelector);
    }


    public int deleteByGenericEntitySelector(IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory
                .apply(selectionOptions);
        return involvementDao
                .deleteByGenericEntitySelector(genericSelector);
    }


    public int countOrphanInvolvementsForKind(EntityKind entityKind) {
        return involvementDao.countOrphanInvolvementsForKind(entityKind);
    }


    public int cleanupInvolvementsForKind(String userName, EntityKind entityKind) {
        boolean isAdmin = userRoleService.hasRole(userName, SystemRole.ADMIN);
        Checks.checkTrue(isAdmin, "Must be an admin to bulk remove involvements");
        return involvementDao.cleanupInvolvementsForKind(entityKind);
    }


    private Involvement mkInvolvement(EntityReference entityReference,
                                      EntityInvolvementChangeCommand command) {
        checkNotNull(entityReference, "entityReference cannot be null");
        checkNotNull(command, "command cannot be null");

        Person person = personDao.getById(command.personEntityRef().id());

        return Involvement.mkInvolvement(
                entityReference,
                person.employeeId(),
                command.involvementKindId(),
                "waltz",
                false);
    }


    private void logChange(EntityReference entityReference, String userId, EntityInvolvementChangeCommand command) {
        String message = String.format("Involvement kind (%s) %s for person: %s",
                resolvePrettyInvolvementKind(command.involvementKindId()),
                command.operation().name().toLowerCase(),
                resolveName(command.personEntityRef()));

        ImmutableChangeLog changeLog = ImmutableChangeLog.builder()
                .parentReference(entityReference)
                .message(message)
                .userId(userId)
                .childKind(command.personEntityRef().kind())
                .operation(command.operation())
                .build();
        changeLogService.write(changeLog);
    }


    private String resolvePrettyInvolvementKind(long id) {
        if(involvementKindIdToNameMap == null) {
            this.involvementKindIdToNameMap = loadInvolvementKindIdToNameMap();
        }

        return String.format("%s / %s", this.involvementKindIdToNameMap.get(id), id);
    }


    private String resolveName(EntityReference ref) {
        return applyToFirst(
                    entityReferenceNameResolver.resolve(newArrayList(ref)),
                    EntityReferenceUtilities::pretty)
                .orElseGet(ref::toString);
    }


    private Map<Long, String> loadInvolvementKindIdToNameMap() {
        return involvementKindService
                .findAll()
                .stream()
                .collect(Collectors.toMap(ik -> ik.id().get(), NameProvider::name));
    }


    private void checkInvolvementKindIsUserSelectable(EntityInvolvementChangeCommand command) {
        InvolvementKind involvementKind = involvementKindService.getById(command.involvementKindId());
        checkTrue(involvementKind.userSelectable(), "Involvement kind '%s' is not user selectable", involvementKind.name());
    }

}
