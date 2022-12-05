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

package org.finos.waltz.service.allocation;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.service.allocation.AllocationUtilities.ValidationResult;
import org.finos.waltz.service.allocation_schemes.AllocationSchemeService;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.allocation.AllocationDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation.MeasurablePercentageChange;
import org.finos.waltz.service.permission.permission_checker.AllocationPermissionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.finos.waltz.model.EntityKind.*;
import static org.finos.waltz.service.allocation.AllocationUtilities.mkBasicLogEntry;
import static org.finos.waltz.service.allocation.AllocationUtilities.validateAllocationChanges;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class AllocationService {

    private static final Logger LOG = LoggerFactory.getLogger(AllocationService.class);

    private final AllocationDao allocationDao;
    private final ChangeLogService changeLogService;
    private final EntityReferenceNameResolver nameResolver;

    private final AllocationPermissionChecker allocationPermissionChecker;
    private final AllocationSchemeService allocationSchemeService;


    @Autowired
    public AllocationService(AllocationDao allocationDao,
                             EntityReferenceNameResolver nameResolver,
                             ChangeLogService changeLogService,
                             AllocationPermissionChecker allocationPermissionChecker,
                             AllocationSchemeService allocationSchemeService) {

        checkNotNull(allocationDao, "allocationDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(allocationPermissionChecker, "allocationPermissionChecker cannot be null");
        checkNotNull(allocationSchemeService, "allocationSchemeService cannot be null");

        this.allocationDao = allocationDao;
        this.changeLogService = changeLogService;
        this.allocationPermissionChecker = allocationPermissionChecker;
        this.nameResolver = nameResolver;
        this.allocationSchemeService = allocationSchemeService;
    }


    public Collection<Allocation> findByEntity(EntityReference ref) {
        return allocationDao.findByEntity(ref);
    }


    public List<Allocation> findByEntityAndScheme(EntityReference ref,
                                                  long schemeId) {
        return allocationDao.findByEntityAndScheme(ref, schemeId);
    }


    public List<Allocation> findByMeasurableAndScheme(long measurableId,
                                                      long schemeId){
        return allocationDao.findByMeasurableIdAndScheme(measurableId, schemeId);
    }


    public Boolean updateAllocations(EntityReference ref,
                                     long schemeId,
                                     Collection<MeasurablePercentageChange> changes,
                                     String username){
        validateChanges(
                ref,
                schemeId,
                changes,
                username);

        Boolean success = allocationDao.updateAllocations(ref, schemeId, changes, username);

        if (success) {
            writeChangeLogEntries(ref, schemeId, changes, username);
        }

        return success;
    }


    // -- HELPERS ---

    private void validateChanges(EntityReference ref,
                                 long schemeId,
                                 Collection<MeasurablePercentageChange> changes,
                                 String username) {
        List<Allocation> currentAllocations = findByEntityAndScheme(ref, schemeId);

        ValidationResult validationResult = validateAllocationChanges(currentAllocations, changes);
        if (validationResult.failed()) {
            String reason = String.format("Cannot update allocations because: %s", validationResult.message());
            LOG.error("Cannot update allocations for entity: {}, scheme: {}, changes:{}, for user: {}, because: {}",
                    ref,
                    schemeId,
                    changes,
                    username,
                    reason);

            throw new IllegalArgumentException(reason);
        }
    }

    private void writeChangeLogEntries(EntityReference ref, long schemeId, Collection<MeasurablePercentageChange> changes, String username) {
        List<EntityReference> refs = ListUtilities.map(
                changes,
                c -> mkRef(MEASURABLE, c.measurablePercentage().measurableId()));

        Map<Long, Optional<String>> measurableIdToName = nameResolver.resolve(refs)
                .stream()
                .collect(Collectors.toMap(EntityReference::id, EntityReference::name));

        Optional<EntityReference> schemeRef = nameResolver.resolve(mkRef(ALLOCATION_SCHEME, schemeId));

        String msgPrefix = String.format(
                "Modifying %s allocations",
                schemeRef
                    .flatMap(EntityReference::name)
                    .orElse("Unknown"));

        String msgBody = changes.stream()
                .map(c -> describeChange(measurableIdToName, c))
                .collect(Collectors.joining(", "));

        String msg = String.format(
                "%s: %s",
                msgPrefix,
                msgBody);

        changeLogService.write(mkBasicLogEntry(ref, msg, username));
    }


    private String describeChange(Map<Long, Optional<String>> measurableIdToName,
                                  MeasurablePercentageChange c) {
        switch (c.operation()) {
            case UPDATE:
            case ADD:
                return  String.format(
                        "Set allocation for measurable '%s' to %d%% from %d%% ",
                        measurableIdToName.get(c.measurablePercentage().measurableId()).orElse("Unknown"),
                        c.measurablePercentage().percentage(),
                        c.previousPercentage().orElse(0));
            case REMOVE:
                return String.format(
                        "Unallocated measurable '%s'",
                        measurableIdToName.get(c.measurablePercentage().measurableId()).orElse("Unknown"));
            default:
                return "";
        }
    }


    public void checkHasEditPermission(EntityReference parentRef,
                                       Long schemeId,
                                       String username) throws InsufficientPrivelegeException {

        AllocationScheme allocScheme = allocationSchemeService.getById(schemeId);

        Set<Operation> perms = allocationPermissionChecker.findAllocationPermissions(parentRef, allocScheme.measurableCategoryId(), username);
        allocationPermissionChecker.verifyEditPerms(perms, MEASURABLE_RATING, username);
    }

}
