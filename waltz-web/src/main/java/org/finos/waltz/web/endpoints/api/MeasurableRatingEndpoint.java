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
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.application.MeasurableRatingsView;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.measurable_rating.ImmutableRemoveMeasurableRatingCommand;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating.MeasurableRatingStatParams;
import org.finos.waltz.model.measurable_rating.MeasurableRatingView;
import org.finos.waltz.model.measurable_rating.RemoveMeasurableRatingCommand;
import org.finos.waltz.model.tally.MeasurableRatingTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.service.measurable_rating.BulkMeasurableItemParser;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingViewService;
import org.finos.waltz.service.permission.permission_checker.MeasurableRatingPermissionChecker;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityKind.MEASURABLE_RATING;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.WebUtilities.readEnum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.deleteForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class MeasurableRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-rating");


    private final MeasurableRatingService measurableRatingService;
    private final MeasurableRatingViewService measurableRatingViewService;
    private final MeasurableRatingPermissionChecker measurableRatingPermissionChecker;
    private final UserRoleService userRoleService;


    @Autowired
    public MeasurableRatingEndpoint(MeasurableRatingService measurableRatingService,
                                    MeasurableRatingViewService measurableRatingViewService,
                                    MeasurableRatingPermissionChecker measurableRatingPermissionChecker,
                                    UserRoleService userRoleService) {

        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(measurableRatingViewService, "measurableRatingViewService cannot be null");
        checkNotNull(measurableRatingPermissionChecker, "measurableRatingPermissionChecker cannot be null");

        this.measurableRatingService = measurableRatingService;
        this.measurableRatingPermissionChecker = measurableRatingPermissionChecker;
        this.measurableRatingViewService = measurableRatingViewService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String getViewByIdPath = mkPath(BASE_URL, "id", ":id", "view");
        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String getViewForCategoryAndAppSelectorPath = mkPath(BASE_URL, "category", ":id", "view");
        String getPrimaryRatingsViewForAppSelectorPath = mkPath(BASE_URL, "primary-ratings", "view");
        String modifyMeasurableForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id", "measurable", ":measurableId");
        String modifyCategoryForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id", "category", ":categoryId");
        String findByMeasurableSelectorPath = mkPath(BASE_URL, "measurable-selector");
        String findByAppSelectorPath = mkPath(BASE_URL, "app-selector");
        String findByCategoryPath = mkPath(BASE_URL, "category", ":id");
        String countByMeasurableCategoryPath = mkPath(BASE_URL, "count-by", "measurable", "category", ":id");
        String statsByAppSelectorPath = mkPath(BASE_URL, "stats-by", "app-selector");
        String hasMeasurableRatingsPath = mkPath(BASE_URL, "has-measurable-ratings");

        String saveRatingItemPath = mkPath(BASE_URL, "entity", ":kind", ":id", "measurable", ":measurableId", "rating");
        String saveRatingDescriptionPath = mkPath(BASE_URL, "entity", ":kind", ":id", "measurable", ":measurableId", "description");
        String saveRatingIsPrimaryPath = mkPath(BASE_URL, "entity", ":kind", ":id", "measurable", ":measurableId", "is-primary");

        registerPreviewBulkMeasurableRatingChanges(mkPath(BASE_URL, "bulk", "preview", ":kind", ":id"));
        DatumRoute<MeasurableRating> getByIdRoute = (request, response)
                -> measurableRatingService.getById(getId(request));

        DatumRoute<MeasurableRatingView> getViewByIdRoute = (request, response)
                -> measurableRatingViewService.getViewById(getId(request));

        ListRoute<MeasurableRating> findForEntityRoute = (request, response)
                -> measurableRatingService.findForEntity(getEntityReference(request));

        ListRoute<MeasurableRating> findByMeasurableSelectorRoute = (request, response)
                -> measurableRatingService.findByMeasurableIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<MeasurableRating> findByAppSelectorRoute = (request, response)
                -> measurableRatingService.findByAppIdSelector(readIdSelectionOptionsFromBody(request));

        DatumRoute<MeasurableRatingCategoryView> getViewByCategoryAndAppSelectorRoute = (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            long categoryId = getId(request);
            return time("viewForCatAndSelector", () -> measurableRatingViewService.getViewForCategoryAndSelector(idSelectionOptions, categoryId));
        };

        DatumRoute<MeasurableRatingsView> getPrimaryRatingsViewForAppSelectorRoute = (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            return measurableRatingViewService.getPrimaryRatingsView(idSelectionOptions);
        };

        ListRoute<MeasurableRating> findByCategoryRoute = (request, response)
                -> measurableRatingService.findByCategory(getId(request));

        ListRoute<Tally<Long>> countByMeasurableCategoryRoute = (request, response)
                -> measurableRatingService.tallyByMeasurableCategoryId(getId(request));

        ListRoute<MeasurableRatingTally> statsByAppSelectorRoute = (request, response)
                -> measurableRatingService.statsByAppSelector(readBody(request, MeasurableRatingStatParams.class));

        DatumRoute<Boolean> hasMeasurableRatingsRoute = (request, response)
                -> measurableRatingService.hasMeasurableRatings(readIdSelectionOptionsFromBody(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getViewByIdPath, getViewByIdRoute);
        getForList(findForEntityPath, findForEntityRoute);
        postForList(findByMeasurableSelectorPath, findByMeasurableSelectorRoute);
        postForList(findByAppSelectorPath, findByAppSelectorRoute);
        getForList(findByCategoryPath, findByCategoryRoute);
        deleteForList(modifyMeasurableForEntityPath, this::removeRoute);
        deleteForList(modifyCategoryForEntityPath, this::removeCategoryRoute);
        getForList(countByMeasurableCategoryPath, countByMeasurableCategoryRoute);
        postForDatum(hasMeasurableRatingsPath, hasMeasurableRatingsRoute);
        postForList(statsByAppSelectorPath, statsByAppSelectorRoute);

        postForList(saveRatingItemPath, this::saveRatingItemRoute);
        postForList(saveRatingDescriptionPath, this::saveRatingDescriptionRoute);
        postForList(saveRatingIsPrimaryPath, this::saveRatingIsPrimaryRoute);
        postForDatum(getViewForCategoryAndAppSelectorPath, getViewByCategoryAndAppSelectorRoute);
        postForDatum(getPrimaryRatingsViewForAppSelectorPath, getPrimaryRatingsViewForAppSelectorRoute);
    }


    private Collection<MeasurableRating> saveRatingDescriptionRoute(Request request, Response response) throws InsufficientPrivelegeException {
        EntityReference entityRef = getEntityReference(request);
        long measurableId = getLong(request, "measurableId");
        String description = request.body();
        String username = getUsername(request);
        checkHasPermissionForThisOperation(entityRef, measurableId, asSet(Operation.UPDATE), username);

        measurableRatingService.saveRatingDescription(entityRef, measurableId, description, username);
        return measurableRatingService.findForEntity(entityRef);
    }


    private Collection<MeasurableRating> saveRatingIsPrimaryRoute(Request request, Response response) throws InsufficientPrivelegeException {
        EntityReference entityRef = getEntityReference(request);
        long measurableId = getLong(request, "measurableId");
        boolean isPrimary = Boolean.parseBoolean(request.body());
        String username = getUsername(request);
        checkHasPermissionForThisOperation(entityRef, measurableId, asSet(Operation.UPDATE), username);

        measurableRatingService.saveRatingIsPrimary(entityRef, measurableId, isPrimary, username);
        return measurableRatingService.findForEntity(entityRef);
    }


    private Collection<MeasurableRating> saveRatingItemRoute(Request request, Response response) throws InsufficientPrivelegeException {
        EntityReference entityRef = getEntityReference(request);
        long measurableId = getLong(request, "measurableId");
        String ratingCode = request.body();
        String username = getUsername(request);
        checkHasPermissionForThisOperation(entityRef, measurableId, asSet(Operation.UPDATE), username);

        measurableRatingService.saveRatingItem(
                entityRef,
                measurableId,
                ratingCode,
                username);

        return measurableRatingService.findForEntity(entityRef);
    }


    private Collection<MeasurableRating> removeCategoryRoute(Request request, Response z) {

        EntityReference ref = getEntityReference(request);
        long categoryId = getLong(request, "categoryId");

        requireRole(userRoleService, request, measurableRatingService.getRequiredRatingEditRole(mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId)));

        return measurableRatingService.removeForCategory(ref, categoryId, getUsername(request));
    }


    private Collection<MeasurableRating> removeRoute(Request request, Response z) throws IOException, InsufficientPrivelegeException {
        long measurableId = getLong(request, "measurableId");
        String username = getUsername(request);
        EntityReference parentReference = getEntityReference(request);

        checkHasPermissionForThisOperation(parentReference, measurableId, asSet(Operation.REMOVE), username);

        RemoveMeasurableRatingCommand command = ImmutableRemoveMeasurableRatingCommand.builder()
                .entityReference(parentReference)
                .measurableId(measurableId)
                .lastUpdate(UserTimestamp.mkForUser(username))
                .build();

        return measurableRatingService.remove(command);
    }


    private void checkHasPermissionForThisOperation(EntityReference parentRef,
                                                    Long measurableId,
                                                    Set<Operation> operations,
                                                    String username) throws InsufficientPrivelegeException {

        Set<Operation> perms = measurableRatingPermissionChecker.findMeasurableRatingPermissions(parentRef, measurableId, username);
        measurableRatingPermissionChecker.verifyAnyPerms(operations, perms, MEASURABLE_RATING, username);
    }

    private void registerPreviewBulkMeasurableRatingChanges(String path) {
        postForDatum(path, (req, resp) -> {
            EntityReference measurableRatingRef = getEntityReference(req);
            BulkMeasurableItemParser.InputFormat format = readEnum(req, "format", BulkMeasurableItemParser.InputFormat.class, s -> BulkMeasurableItemParser.InputFormat.TSV);
            BulkUpdateMode mode = readEnum(req, "mode", BulkUpdateMode.class, s -> BulkUpdateMode.ADD_ONLY);
            String body = req.body();
            return measurableRatingService.bulkPreview(measurableRatingRef, body, format, mode);
        });
    }
}
