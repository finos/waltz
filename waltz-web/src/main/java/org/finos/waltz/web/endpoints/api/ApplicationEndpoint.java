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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.application.ApplicationsView;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.application.ApplicationViewService;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.action.AppChangeAction;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.application.AppRegistrationRequest;
import org.finos.waltz.model.application.AppRegistrationResponse;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.AssetCodeRelationshipKind;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.tally.Tally;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.getUsername;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static org.finos.waltz.web.WebUtilities.readIdsFromBody;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class ApplicationEndpoint implements Endpoint {


    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEndpoint.class);

    private static final String BASE_URL = mkPath("api", "app");

    private final ApplicationService appService;
    private final ApplicationViewService appViewService;
    private final ChangeLogService changeLogService;


    @Autowired
    public ApplicationEndpoint(ApplicationService appService,
                               ChangeLogService changeLogService,
                               ApplicationViewService appViewService) {
        this.appViewService = appViewService;
        checkNotNull(appService, "appService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");

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

        ListRoute<Application> findBySelectorRoute = (request, response)
                -> appService.findByAppIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<Application> findByAssetCodeRoute = (request, response)
                -> appService.findByAssetCode(ExternalIdValue.of(request.splat()[0]));

        DatumRoute<ApplicationsView> getViewBySelectorRoute = (request, response)
                -> appViewService.getViewBySelector(readIdSelectionOptionsFromBody(request));

        getForList(mkPath(BASE_URL, "search", ":query"), searchRoute);
        getForList(mkPath(BASE_URL, "count-by", "org-unit"), tallyByOrgUnitRoute);

        getForDatum(mkPath(BASE_URL, "id", ":id"), getByIdRoute);
        getForDatum(mkPath(BASE_URL, "id", ":id", "related"), findRelatedRoute);
        postForList(mkPath(BASE_URL, "by-ids"), findByIdsRoute);
        getForList(mkPath(BASE_URL, "all"), findAllRoute);
        postForList(mkPath(BASE_URL, "selector"), findBySelectorRoute);
        getForList(mkPath(BASE_URL, "asset-code", "*"), findByAssetCodeRoute);
        getForList(mkPath(BASE_URL, "external-id", "*"), findByAssetCodeRoute);
        postForDatum(mkPath(BASE_URL, "view", "selector"), getViewBySelectorRoute);

    }

}
