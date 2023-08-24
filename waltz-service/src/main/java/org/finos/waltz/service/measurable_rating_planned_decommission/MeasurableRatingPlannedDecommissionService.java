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

package org.finos.waltz.service.measurable_rating_planned_decommission;

import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.common.exception.UpdateFailedException;
import org.finos.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import org.finos.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.command.DateFieldChange;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class MeasurableRatingPlannedDecommissionService {

    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final MeasurableRatingReplacementDao measurableRatingReplacementDao;
    private final MeasurableRatingService measurableRatingService;
    private final ChangeLogService changeLogService;

    @Autowired
    public MeasurableRatingPlannedDecommissionService(MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                                      MeasurableRatingReplacementDao measurableRatingReplacementDao,
                                                      MeasurableRatingService measurableRatingService,
                                                      ChangeLogService changeLogService) {
        checkNotNull(measurableRatingPlannedDecommissionDao, "MeasurableRatingPlannedDecommissionDao cannot be null");
        checkNotNull(measurableRatingReplacementDao, "MeasurableRatingReplacementDao cannot be null");
        checkNotNull(measurableRatingService, "MeasurableRatingService cannot be null");
        checkNotNull(changeLogService, "ChangeLogService cannot be null");
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.measurableRatingReplacementDao = measurableRatingReplacementDao;
        this.measurableRatingService = measurableRatingService;
        this.changeLogService = changeLogService;
    }


    public Collection<MeasurableRatingPlannedDecommission> findForEntityRef(EntityReference ref) {
        return measurableRatingPlannedDecommissionDao.findByEntityRef(ref);
    }


    public Collection<MeasurableRatingPlannedDecommission> findForReplacingEntityRef(EntityReference ref) {
        return measurableRatingPlannedDecommissionDao.findByReplacingEntityRef(ref);
    }


    public MeasurableRatingPlannedDecommission save(long measurableRatingId,
                                                    DateFieldChange dateChange,
                                                    String userName) {

        MeasurableRating rating = measurableRatingService.getById(measurableRatingId);

        Tuple2<Operation, Boolean> operation = measurableRatingPlannedDecommissionDao.save(
                measurableRatingId,
                dateChange,
                userName);

        if (!operation.v2) {
            throw new UpdateFailedException(
                    "DECOM_DATE_SAVE_FAILED",
                    format("Failed to store date change for entity %s:%d and measurable %d",
                            rating.entityReference().kind(),
                            rating.entityReference().id(),
                            rating.measurableId()));
        } else {
            MeasurableRatingPlannedDecommission plannedDecommission = measurableRatingPlannedDecommissionDao.getByEntityAndMeasurable(rating.entityReference(),rating.measurableId());
            String logMessage = operation.v1.equals(Operation.UPDATE)
                    ? String.format("Updated planned decommission date: from %s to %s",
                    dateChange.oldVal(),
                    plannedDecommission.plannedDecommissionDate())
                    : String.format("Added planned decommission date:%s", plannedDecommission.plannedDecommissionDate());

            changeLogService.writeChangeLogEntries(
                    plannedDecommission,
                    userName,
                    logMessage,
                    operation.v1);

            return plannedDecommission;
        }

    }


    public Boolean remove(Long id, String username) {
        Set<String> replacementApps = map(measurableRatingReplacementDao.fetchByDecommissionId(id), r -> r.entityReference().name().get());

        String msg = (replacementApps.size() > 0) ?
                format("Removed planned decommission date and the associated replacement application/s: %s", replacementApps)
                : "Removed planned decommission date";

        changeLogService.writeChangeLogEntries(
                mkRef(EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION, id),
                username,
                msg,
                Operation.REMOVE);

        return measurableRatingPlannedDecommissionDao.remove(id);
    }


    public String getRequiredRatingEditRole(EntityReference entityRef) {
        return measurableRatingService.getRequiredRatingEditRole(entityRef);
    }


    public MeasurableRatingPlannedDecommission getById(Long decommId) {
        return measurableRatingPlannedDecommissionDao.getById(decommId);
    }

}