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

import org.finos.waltz.model.BulkChangeStatistics;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.BulkUploadLegalEntityRelationshipCommand;
import org.finos.waltz.model.bulk_upload.legal_entity_relationship.ResolveBulkUploadLegalEntityRelationshipResponse;
import org.finos.waltz.service.bulk_upload.BulkUploadLegalEntityRelationshipService;
import org.finos.waltz.service.permission.permission_checker.LegalEntityRelationshipPermissionChecker;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class BulkUploadLegalEntityRelationshipEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadLegalEntityRelationshipEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "bulk-upload-legal-entity-relationships");

    private final BulkUploadLegalEntityRelationshipService service;
    private final LegalEntityRelationshipPermissionChecker legalEntityRelationshipPermissionChecker;
    private final UserRoleService userRoleService;


    @Autowired
    public BulkUploadLegalEntityRelationshipEndpoint(BulkUploadLegalEntityRelationshipService service,
                                                     LegalEntityRelationshipPermissionChecker legalEntityRelationshipPermissionChecker,
                                                     UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(legalEntityRelationshipPermissionChecker, "legalEntityRelationshipPermissionChecker cannot be null");

        this.service = service;
        this.userRoleService = userRoleService;
        this.legalEntityRelationshipPermissionChecker = legalEntityRelationshipPermissionChecker;
    }


    @Override
    public void register() {

        postForDatum(mkPath(BASE_URL, "resolve"), this::resolveRoute);
        postForDatum(mkPath(BASE_URL, "save"), this::saveRoute);

    }

    private ResolveBulkUploadLegalEntityRelationshipResponse resolveRoute(Request request, Response response) throws IOException {
        BulkUploadLegalEntityRelationshipCommand uploadCmd = readBody(request, BulkUploadLegalEntityRelationshipCommand.class);
        String username = getUsername(request);

        LOG.info("User: {} resolving bulk upload: {}", username, uploadCmd);
        return service.resolve(uploadCmd);
    }


    private BulkChangeStatistics saveRoute(Request request, Response response) throws IOException {
        BulkUploadLegalEntityRelationshipCommand uploadCmd = readBody(request, BulkUploadLegalEntityRelationshipCommand.class);
        String username = getUsername(request);

        LOG.info("User: {} resolving bulk upload: {}", username, uploadCmd);
        return service.save(uploadCmd, username);
    }

    private void ensureUserHasAdminRights(long legalEntityRelationshipKindId, String username) {
        Set<Operation> perms = legalEntityRelationshipPermissionChecker.findLegalEntityRelationshipPermissionsForRelationshipKind(legalEntityRelationshipKindId, username);
    }

}
