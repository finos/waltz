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


import com.khartec.waltz.common.exception.UpdateFailedException;
import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import com.khartec.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import org.jooq.lambda.tuple.Tuple2;
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
    private final MeasurableRatingService measurableRatingService;
    private final EntityReferenceNameResolver nameResolver;
    private final ChangeLogService changeLogService;

    @Autowired
    public MeasurableRatingReplacementService(MeasurableRatingReplacementDao measurableRatingReplacementDao,
                                              MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                              MeasurableRatingService measurableRatingService,
                                              EntityReferenceNameResolver nameResolver,
                                              ChangeLogService changeLogService){
        checkNotNull(measurableRatingReplacementDao, "measurableRatingReplacementDao cannot be null");
        checkNotNull(measurableRatingPlannedDecommissionDao, "measurableRatingPlannedDecommissionDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.measurableRatingService = measurableRatingService;
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

        Tuple2<Operation, Boolean> operation = measurableRatingReplacementDao.save(decommId, entityReference, commissionDate, username);

        MeasurableRatingReplacement measurableRatingReplacement = measurableRatingReplacementDao.fetchByDecommissionIdAndEntityRef(decommId, entityReference);
        MeasurableRatingPlannedDecommission plannedDecomm = measurableRatingPlannedDecommissionDao.getById(decommId);

        if(!operation.v2){
            throw new UpdateFailedException(
                    "REPLACEMENT_SAVE_FAILED",
                    format("Failed to store measurable rating replacement %s:%d for entity %s:%d and measurable %d",
                            measurableRatingReplacement.entityReference().kind(),
                            measurableRatingReplacement.entityReference().id(),
                            plannedDecomm.entityReference().kind(),
                            plannedDecomm.entityReference().id(),
                            plannedDecomm.measurableId()));
        } else {

            changeLogService.writeChangeLogEntries(
                    measurableRatingReplacement,
                    username,
                    format("%s with planned commission date: %s", (operation.v1.equals(Operation.ADD) ? "Added" : "Updated"), commissionDate),
                    operation.v1);

            return measurableRatingReplacementDao.fetchByDecommissionId(decommId);
        }
    }


    public Collection<MeasurableRatingReplacement> remove(long decommId, long replacementId, String username) {

        changeLogService.writeChangeLogEntries(
                mkRef(EntityKind.MEASURABLE_RATING_REPLACEMENT, replacementId),
                username,
                "Removed",
                Operation.REMOVE);

        boolean isRemoved = measurableRatingReplacementDao.remove(decommId, replacementId);

        return measurableRatingReplacementDao.fetchByDecommissionId(decommId);
    }

    public String getRequiredRatingEditRole(EntityReference ref) {
        return measurableRatingService.getRequiredRatingEditRole(ref);
    }
}
