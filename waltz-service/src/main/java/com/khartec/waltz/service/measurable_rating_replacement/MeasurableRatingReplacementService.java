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

package com.khartec.waltz.service.measurable_rating_replacement;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import com.khartec.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.lang.String.format;

@Service
public class MeasurableRatingReplacementService {

    private final MeasurableRatingReplacementDao measurableRatingReplacementDao;
    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final EntityReferenceNameResolver nameResolver;
    private final ChangeLogService changeLogService;

    @Autowired
    public MeasurableRatingReplacementService(MeasurableRatingReplacementDao measurableRatingReplacementDao,
                                              MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                              EntityReferenceNameResolver nameResolver,
                                              ChangeLogService changeLogService){
        checkNotNull(measurableRatingReplacementDao, "measurableRatingReplacementDao cannot be null");
        checkNotNull(measurableRatingPlannedDecommissionDao, "measurableRatingPlannedDecommissionDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.measurableRatingReplacementDao = measurableRatingReplacementDao;
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.nameResolver = nameResolver;
        this.changeLogService = changeLogService;
    }


    public Collection<MeasurableRatingReplacement> findForEntityRef(EntityReference ref){
        return measurableRatingReplacementDao.fetchByEntityRef(ref);
    }


    public Set<MeasurableRatingReplacement> save(long decommId,
                        EntityReference entityReference,
                        LocalDate commissionDate,
                        String username) {

        measurableRatingReplacementDao.save(decommId, entityReference, commissionDate, username);

        MeasurableRatingPlannedDecommission plannedDecomm = measurableRatingPlannedDecommissionDao.getById(decommId);

        mkChangeLogEntry(entityReference,
                format("Added as a replacement application for measurable: %s [%d] from %s [%d] on %s",
                        resolveMeasurableName(plannedDecomm.id()),
                        plannedDecomm.measurableId(),
                        resolveApplicationName(entityReference.id()),
                        entityReference.id(),
                        commissionDate),
                username,
                Operation.ADD);
        mkChangeLogEntry(plannedDecomm.entityReference(),
                format("Added replacement application %s [%d] for measurable: %s [%d] with planned commission date: %s",
                        resolveApplicationName(entityReference.id()),
                        entityReference.id(),
                        resolveMeasurableName(plannedDecomm.measurableId()),
                        plannedDecomm.measurableId(),
                        commissionDate),
                username,
                Operation.ADD);

        return measurableRatingReplacementDao.fetchByDecommissionId(decommId);
    }


    public Collection<MeasurableRatingReplacement> remove(long decommId, long replacementId, String username) {

        MeasurableRatingReplacement replacementRemoved = measurableRatingReplacementDao.getById(replacementId);
        MeasurableRatingPlannedDecommission plannedDecomm = measurableRatingPlannedDecommissionDao.getById(decommId);

        measurableRatingReplacementDao.remove(decommId, replacementId);

        mkChangeLogEntry(plannedDecomm.entityReference(),
                format("Removed replacement application %s [%d] from measurable: %s [%d]",
                        resolveApplicationName(replacementRemoved.entityReference().id()),
                        replacementRemoved.entityReference().id(),
                        resolveMeasurableName(plannedDecomm.measurableId()),
                        plannedDecomm.measurableId()),
                username,
                Operation.REMOVE);
        mkChangeLogEntry(replacementRemoved.entityReference(),
                format("Removed this application as a replacement for measurable: %s [%d] from %s [%d]",
                        resolveMeasurableName(plannedDecomm.measurableId()),
                        plannedDecomm.measurableId(),
                        resolveApplicationName(plannedDecomm.entityReference().id()),
                        plannedDecomm.entityReference().id()),
                username,
                Operation.REMOVE);
        return measurableRatingReplacementDao.fetchByDecommissionId(decommId);
    }


    private void mkChangeLogEntry(EntityReference entityReference, String message, String userName, Operation operation) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(message)
                .parentReference(entityReference)
                .userId(userName)
                .createdAt(DateTimeUtilities.nowUtc())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.MEASURABLE_RATING)
                .operation(operation)
                .build());
    }


    private String resolveMeasurableName(long id) {
        return nameResolver
                .resolve(mkRef(EntityKind.MEASURABLE, id))
                .flatMap(EntityReference::name)
                .orElse("UNKNOWN");
    }


    private String resolveApplicationName(long id) {
        return nameResolver
                .resolve(mkRef(EntityKind.APPLICATION, id))
                .flatMap(EntityReference::name)
                .orElse("UNKNOWN");
    }
}
