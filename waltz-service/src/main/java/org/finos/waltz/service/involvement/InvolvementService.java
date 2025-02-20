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
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.physical_flow.PhysicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.EntityReferenceUtilities;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.involvement.EntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.involvement_kind.InvolvementKind;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
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

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.ListUtilities.applyToFirst;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.isEmpty;


@Service
public class InvolvementService {


    private final ChangeLogService changeLogService;
    private final InvolvementDao involvementDao;
    private final LogicalFlowDao logicalFlowDao;
    private final PhysicalFlowDao physicalFlowDao;
    private final EntityReferenceNameResolver entityReferenceNameResolver;
    private final InvolvementKindService involvementKindService;
    private final PersonDao personDao;
    private final UserRoleService userRoleService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    private Map<Long, String> involvementKindIdToNameMap;


    @Autowired
    public InvolvementService(ChangeLogService changeLogService,
                              InvolvementDao dao,
                              LogicalFlowDao logicalFlowDao,
                              PhysicalFlowDao physicalFlowDao,
                              EntityReferenceNameResolver entityReferenceNameResolver,
                              InvolvementKindService involvementKindService,
                              PersonDao personDao,
                              UserRoleService userRoleService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dao, "involvementDao must not be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao must not be null");
        checkNotNull(entityReferenceNameResolver, "entityReferenceNameResolver cannot be null");
        checkNotNull(involvementKindService, "involvementKindService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");

        this.changeLogService = changeLogService;
        this.involvementDao = dao;
        this.logicalFlowDao = logicalFlowDao;
        this.physicalFlowDao = physicalFlowDao;
        this.entityReferenceNameResolver = entityReferenceNameResolver;
        this.involvementKindService = involvementKindService;
        this.userRoleService = userRoleService;
        this.personDao = personDao;
    }


    public List<Involvement> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return involvementDao.findByEntityReference(ref);
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "employeeId cannot be empty");
        return involvementDao.findByEmployeeId(employeeId);
    }


    public Set<Long> findExistingInvolvementKindIdsForUser(EntityReference entityReference, String username) {
        checkNotNull(entityReference, "entityReference cannot be empty");
        checkNotEmpty(username, "username cannot be empty");
        if(entityReference.kind().equals(EntityKind.LOGICAL_DATA_FLOW)) {
            return findSourceAndTargetInvolvementKinds(entityReference.id(), username);
        } else if(entityReference.kind().equals(EntityKind.PHYSICAL_FLOW)) {
            PhysicalFlow physicalFlow = physicalFlowDao.getById(entityReference.id());
            return findSourceAndTargetInvolvementKinds(physicalFlow.logicalFlowId(), username);
        }
        return involvementDao.findExistingInvolvementKindIdsForUser(entityReference, username);
    }

    private Set<Long> findSourceAndTargetInvolvementKinds(long logicalFlowId, String username) {
        LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(logicalFlowId);
        Set<Long> sourceInvolvements = involvementDao.findExistingInvolvementKindIdsForUser(logicalFlow.source(), username);
        Set<Long> targetInvolvements = involvementDao.findExistingInvolvementKindIdsForUser(logicalFlow.target(), username);
        sourceInvolvements.addAll(targetInvolvements);
        return sourceInvolvements;
    }


    public List<Involvement> findAllByEmployeeId(String employeeId) {
        return involvementDao.findAllByEmployeeId(employeeId);
    }


    public Set<Involvement> findInvolvementsByKindAndEntityKind(Long invKindId, EntityKind entityKind) {
        return involvementDao.findInvolvementsByKindAndEntityKind(invKindId, entityKind);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return involvementDao.findPeopleByEntityReference(ref);
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
        checkHasEditPermissionForKind(Long.valueOf(command.involvementKindId()), userId);

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
        checkHasEditPermissionForKind(Long.valueOf(command.involvementKindId()), userId);

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

    public int bulkStoreInvolvements(Set<Involvement> involvements, String username) {

        int insertedRecords = involvementDao.bulkStoreInvolvements(involvements);

        Map<Long, String> involvementKindNameByIdMap = loadInvolvementKindIdToNameMap();

        Set<ChangeLog> changelogs = map(involvements, i -> {
            String message = format(
                    "Added involvement: %s for employee: %s",
                    involvementKindNameByIdMap.getOrDefault(i.kindId(), "Unknown"),
                    i.employeeId());
            return mkChangeLog(i.entityReference(), username, EntityKind.INVOLVEMENT, Operation.ADD, message);
        });

        changeLogService.write(changelogs);

        return insertedRecords;
    }

    public int bulkDeleteInvolvements(Set<Involvement> involvements, String username) {

        int removedRecords = involvementDao.bulkDeleteInvolvements(involvements);

        Map<Long, String> involvementKindNameByIdMap = loadInvolvementKindIdToNameMap();

        Set<ChangeLog> changelogs = map(involvements, i -> {
            String message = format(
                    "Removed involvement: %s for employee: %s",
                    involvementKindNameByIdMap.getOrDefault(i.kindId(), "Unknown"),
                    i.employeeId());
            return mkChangeLog(i.entityReference(), username, EntityKind.INVOLVEMENT, Operation.REMOVE, message);
        });

        changeLogService.write(changelogs);

        return removedRecords;
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
        String message = format("Involvement kind (%s) %s for person: %s",
                resolvePrettyInvolvementKind(command.involvementKindId()),
                command.operation().name().toLowerCase(),
                resolveName(command.personEntityRef()));

        ImmutableChangeLog changeLog = mkChangeLog(entityReference, userId, command.personEntityRef().kind(), command.operation(), message);
        changeLogService.write(changeLog);
    }


    private ImmutableChangeLog mkChangeLog(EntityReference entityReference,
                                           String userId,
                                           EntityKind childKind,
                                           Operation operation,
                                           String message) {
        return ImmutableChangeLog.builder()
                .parentReference(entityReference)
                .message(message)
                .userId(userId)
                .childKind(childKind)
                .operation(operation)
                .build();
    }


    private String resolvePrettyInvolvementKind(long id) {
        if(involvementKindIdToNameMap == null) {
            this.involvementKindIdToNameMap = loadInvolvementKindIdToNameMap();
        }

        return format("%s / %s", this.involvementKindIdToNameMap.get(id), id);
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

    public Set<Involvement> findByKindIdAndEntityKind(long id, EntityKind kind) {
        return involvementDao.findByKindIdAndEntityKind(id, kind);
    }


    private void checkHasEditPermissionForKind(Long kindId, String username) {
        InvolvementKind kind = involvementKindService.getById(kindId);
        if (!isEmpty(kind.permittedRole())) {
            checkTrue(
                    userRoleService.hasAnyRole(username, SetUtilities.asSet(kind.permittedRole(), SystemRole.ADMIN.name())),
                    format("User does not have the required permissions to edit involvement kind: '%s'", kind.name()));
        }
    }
}
