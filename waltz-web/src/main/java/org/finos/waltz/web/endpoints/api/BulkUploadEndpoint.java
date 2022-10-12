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

import org.finos.waltz.model.bulk_upload.BulkUploadCommand;
import org.finos.waltz.model.bulk_upload.ResolveBulkUploadRequestParameters;
import org.finos.waltz.model.bulk_upload.ResolveRowResponse;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.bulk_upload.BulkUploadService;
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
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class BulkUploadEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "bulk-upload");

    private final BulkUploadService service;
    private UserRoleService userRoleService;


    @Autowired
    public BulkUploadEndpoint(BulkUploadService service, UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {


        // create
        postForList(mkPath(BASE_URL, "resolve"), this::resolveRoute);
        postForDatum(mkPath(BASE_URL), this::uploadRoute);

    }

    private List<ResolveRowResponse> resolveRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        ResolveBulkUploadRequestParameters resolveParams = readBody(request, ResolveBulkUploadRequestParameters.class);
        String username = getUsername(request);
        LOG.info("User: {} resolving bulk upload: {}", username, resolveParams);

        return service.resolve(resolveParams);
    }


    private Integer uploadRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        BulkUploadCommand uploadCommand = readBody(request, BulkUploadCommand.class);
        String username = getUsername(request);
        LOG.info("User: {} requesting bulk upload: {}", username, uploadCommand);

        return service.upload(uploadCommand);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }

}
