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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.service.measurable_rating_replacement.MeasurableRatingReplacementService;
import org.finos.waltz.service.permission.permission_checker.MeasurableRatingPermissionChecker;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class MeasurableRatingReplacementEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-rating-replacement");


    private final MeasurableRatingReplacementService measurableRatingReplacementService;
    private final MeasurableRatingPermissionChecker measurableRatingPermissionChecker;


    @Autowired
    public MeasurableRatingReplacementEndpoint(MeasurableRatingReplacementService measurableRatingReplacementService,
                                               MeasurableRatingPermissionChecker measurableRatingPermissionChecker) {
        checkNotNull(measurableRatingReplacementService, "measurableRatingReplacementService cannot be null");
        checkNotNull(measurableRatingPermissionChecker, "measurableRatingPermissionChecker cannot be null");

        this.measurableRatingReplacementService = measurableRatingReplacementService;
        this.measurableRatingPermissionChecker = measurableRatingPermissionChecker;
    }


    @Override
    public void register() {

        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String removePath = mkPath(BASE_URL, "decomm-id", ":decommId", "replacement-id", ":replacementId");
        String savePath = mkPath(BASE_URL, "decomm-id", ":decommId", "entity", ":kind", ":id");

        ListRoute<MeasurableRatingReplacement> findForEntityRoute = (request, response)
                -> measurableRatingReplacementService.findForEntityRef(getEntityReference(request));

        ListRoute<MeasurableRatingReplacement> saveRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            long decommId = getLong(request, "decommId");
            Date commissionDate = readBody(request, Date.class);
            String username = getUsername(request);

            checkHasPermissionForThisOperation(decommId, asSet(Operation.ADD, Operation.UPDATE), username);

            return measurableRatingReplacementService.save(decommId, entityReference, commissionDate, username);
        };


        ListRoute<MeasurableRatingReplacement> removeRoute = (request, response) -> {
            String username = getUsername(request);
            long decommId = getLong(request, "decommId");
            long replacementId = getLong(request, "replacementId");

            checkHasPermissionForThisOperation(decommId, asSet(Operation.REMOVE), username);

            return measurableRatingReplacementService.remove(decommId, replacementId, username);
        };


        getForList(findForEntityPath, findForEntityRoute);
        deleteForList(removePath, removeRoute);
        postForList(savePath, saveRoute);

    }


    private void checkHasPermissionForThisOperation(long decommId,
                                                    Set<Operation> operations,
                                                    String username) throws InsufficientPrivelegeException {

        Set<Operation> perms = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, username);
        measurableRatingPermissionChecker.verifyAnyPerms(operations, perms, EntityKind.MEASURABLE_RATING_REPLACEMENT, username);
    }
}
