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

package com.khartec.waltz.service.measurable_rating_planned_decommission;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.exception.UpdateFailedException;
import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import com.khartec.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.DateFieldChange;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.lang.String.format;

@Service
public class MeasurableRatingPlannedDecommissionService {

    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final MeasurableRatingReplacementDao measurableRatingReplacementDao;
    private final EntityReferenceNameResolver nameResolver;
    private final ChangeLogService changeLogService;

    @Autowired
    public MeasurableRatingPlannedDecommissionService(MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                                      MeasurableRatingReplacementDao measurableRatingReplacementDao,
                                                      EntityReferenceNameResolver nameResolver,
                                                      ChangeLogService changeLogService){
        checkNotNull(measurableRatingPlannedDecommissionDao, "MeasurableRatingPlannedDecommissionDao cannot be null");
        checkNotNull(measurableRatingReplacementDao, "MeasurableRatingReplacementDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(changeLogService, "ChangeLogService cannot be null");
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.measurableRatingReplacementDao = measurableRatingReplacementDao;
        this.nameResolver = nameResolver;
        this.changeLogService = changeLogService;
    }


    public Collection<MeasurableRatingPlannedDecommission> findForEntityRef(EntityReference ref){
        return measurableRatingPlannedDecommissionDao.findByEntityRef(ref);
    }


    public Collection<MeasurableRatingPlannedDecommission> findForReplacingEntityRef(EntityReference ref) {
        return measurableRatingPlannedDecommissionDao.findByReplacingEntityRef(ref);
    }


    public MeasurableRatingPlannedDecommission save(EntityReference entityReference, long measurableId, DateFieldChange dateChange, String userName) {
        boolean success = measurableRatingPlannedDecommissionDao.save(
                entityReference,
                measurableId,
                dateChange,
                userName);

        if (! success) {
            throw new UpdateFailedException(
                    "DECOM_DATE_SAVE_FAILED",
                    format("Failed to store date change for entity %s:%d and measurable %d",
                            entityReference.kind(),
                            entityReference.id(),
                            measurableId));
        }

        mkChangeLogEntry(entityReference,
                format("Saved planned decommission date [%s] for measurable: %s [%d]",
                        toSqlDate(dateChange.newVal()),
                        resolveMeasurableName(measurableId),
                        measurableId),
                userName,
                Operation.ADD);

        return measurableRatingPlannedDecommissionDao.getByEntityAndMeasurable(entityReference, measurableId);
    }


    public Boolean remove(Long id, String username){

        MeasurableRatingPlannedDecommission decommissionToRemove = measurableRatingPlannedDecommissionDao.getById(id);
        Set<String> replacementApps = map(measurableRatingReplacementDao.fetchByDecommissionId(id), r -> r.entityReference().name().get());

        boolean is_removed = measurableRatingPlannedDecommissionDao.remove(id);

        mkChangeLogEntry(decommissionToRemove.entityReference(),
                format("Removed planned decommission date for measurable: %s [%d]. Removed associated replacement applications: %s",
                        resolveMeasurableName(decommissionToRemove.measurableId()),
                        decommissionToRemove.measurableId(),
                        replacementApps),
                username,
                Operation.REMOVE);

        return is_removed;
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
}
