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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.AppRegistrationRequest;
import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.AssetCodeRelationshipKind;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.tag.Tag;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.tag.TagService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.action.AppChangeAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.lang.Long.parseLong;


@Service
public class ApplicationEndpoint implements Endpoint {


    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEndpoint.class);

    private static final String BASE_URL = mkPath("api", "app");

    private final ApplicationService appService;
    private final ChangeLogService changeLogService;


    @Autowired
    public ApplicationEndpoint(ApplicationService appService,
                               ChangeLogService changeLogService,
                               TagService tagService) {
        checkNotNull(appService, "appService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(tagService, "appTagService cannot be null");

        this.appService = appService;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {
        registerQueries();
        registerAppRegistration();
        registerAppUpdate();
    }


    private void registerAppUpdate() {
        DatumRoute<Boolean> handleAppUpdateRequest = (req, res) -> {
            res.type(WebUtilities.TYPE_JSON);
            AppChangeAction appChange = readBody(req, AppChangeAction.class);
            LOG.info("Updating application: " + appChange);
            String username = getUsername(req);

            Long appId = appChange.app().id().get();

            EntityReference ref = ImmutableEntityReference.builder()
                    .kind(EntityKind.APPLICATION)
                    .id(appId)
                    .build();

            appChange.changes()
                    .forEach(c -> {
                        changeLogService.write(
                                ImmutableChangeLog.builder()
                                        .message(c.toDescription())
                                        .severity(Severity.INFORMATION)
                                        .userId(username)
                                        .parentReference(ref)
                                        .operation(Operation.UPDATE)
                                        .build());
                    });

            appService.update(appChange.app());
            return true;
        };

        postForDatum(mkPath(BASE_URL, ":id"), handleAppUpdateRequest);

    }


    private void registerAppRegistration() {
        DatumRoute<AppRegistrationResponse> handleRegistrationRequest = (req, res) -> {
            res.type(WebUtilities.TYPE_JSON);
            AppRegistrationRequest registrationRequest = readBody(req, AppRegistrationRequest.class);

            LOG.info("Registering new application:" + registrationRequest);

            AppRegistrationResponse registrationResponse = appService
                    .registerApp(registrationRequest, getUsername(req));

            if (registrationResponse.registered()) {
                ImmutableChangeLog changeLogEntry = ImmutableChangeLog.builder()
                        .message("Registered new application: " + registrationRequest.name())
                        .severity(Severity.INFORMATION)
                        .userId(getUsername(req))
                        .parentReference(ImmutableEntityReference.builder()
                                .kind(EntityKind.APPLICATION)
                                .id(registrationResponse.id().get())
                                .build())
                        .operation(Operation.ADD)
                        .build();
                changeLogService.write(changeLogEntry);
            }

            return registrationResponse;
        };

        postForDatum(BASE_URL, handleRegistrationRequest);
    }


    private void registerQueries() {
        ListRoute<Application> searchRoute = (request, response) -> appService.search(request.params("query"));

        ListRoute<Tally<Long>> tallyByOrgUnitRoute = (request, response) -> appService.countByOrganisationalUnit();


        DatumRoute<Map<AssetCodeRelationshipKind, List<Application>>> findRelatedRoute
                = (req, res) -> appService.findRelated(getId(req));

        ListRoute<Application> findByIdsRoute = (req, res) -> {
            List<Long> ids = readIdsFromBody(req);
            if (ListUtilities.isEmpty(ids)) {
                return Collections.emptyList();
            }
            return appService
                    .findByIds(ids);
        };

        ListRoute<Application> findAllRoute = (req, res) -> appService.findAll();

        DatumRoute<Application> getByIdRoute = (req, res) -> {
            String id = req.params("id");
            return appService
                    .getById(parseLong(id));
        };

        ListRoute<Application> findBySelectorRoute = ((request, response)
                -> appService.findByAppIdSelector(readIdSelectionOptionsFromBody(request)));

        ListRoute<Application> findByAssetCodeRoute = ((request, response)
                -> appService.findByAssetCode(request.splat()[0]));


        getForList(mkPath(BASE_URL, "search", ":query"), searchRoute);
        getForList(mkPath(BASE_URL, "count-by", "org-unit"), tallyByOrgUnitRoute);

        getForDatum(mkPath(BASE_URL, "id", ":id"), getByIdRoute);
        getForDatum(mkPath(BASE_URL, "id", ":id", "related"), findRelatedRoute);
        postForList(mkPath(BASE_URL, "by-ids"), findByIdsRoute);
        getForList(mkPath(BASE_URL, "all"), findAllRoute);
        postForList(mkPath(BASE_URL, "selector"), findBySelectorRoute);
        getForList(mkPath(BASE_URL, "asset-code", "*"), findByAssetCodeRoute);
        getForList(mkPath(BASE_URL, "external-id", "*"), findByAssetCodeRoute);
    }

}
