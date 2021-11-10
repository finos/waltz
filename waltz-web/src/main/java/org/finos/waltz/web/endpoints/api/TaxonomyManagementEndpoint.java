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

import org.finos.waltz.service.taxonomy_management.TaxonomyChangeService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TaxonomyManagementEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "taxonomy-management");

    private final TaxonomyChangeService taxonomyChangeService;


    @Autowired
    public TaxonomyManagementEndpoint(TaxonomyChangeService taxonomyChangeService) {
        this.taxonomyChangeService = taxonomyChangeService;
    }


    @Override
    public void register() {
        registerPreview(WebUtilities.mkPath(BASE_URL, "preview"));
        registerSubmitPendingChange(WebUtilities.mkPath(BASE_URL, "pending-changes"));
        registerRemoveById(WebUtilities.mkPath(BASE_URL, "pending-changes", "id", ":id"));
        registerPreviewById(WebUtilities.mkPath(BASE_URL, "pending-changes", "id", ":id", "preview"));
        registerApplyPendingChange(WebUtilities.mkPath(BASE_URL, "pending-changes", "id", ":id", "apply"));
        registerFindPendingChangesByDomain(WebUtilities.mkPath(BASE_URL, "pending-changes", "by-domain", ":kind", ":id"));
    }


    private void registerApplyPendingChange(String path) {
        EndpointUtilities.postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.applyById(
                    WebUtilities.getId(req),
                    WebUtilities.getUsername(req));
        });
    }


    private void registerSubmitPendingChange(String path) {
        EndpointUtilities.postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.submitDraftChange(
                    WebUtilities.readBody(req, TaxonomyChangeCommand.class),
                    WebUtilities.getUsername(req));
        });
    }


    private void registerFindPendingChangesByDomain(String path) {
        EndpointUtilities.getForList(path, (req, resp) -> {
            return taxonomyChangeService.findDraftChangesByDomain(WebUtilities.getEntityReference(req));
        });
    }


    private void registerRemoveById(String path) {
        EndpointUtilities.deleteForDatum(path, (req, resp) -> {
            return taxonomyChangeService.removeById(
                    WebUtilities.getId(req),
                    WebUtilities.getUsername(req));
        });
    }

    private void registerPreview(String path) {
        EndpointUtilities.postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.preview(WebUtilities.readBody(req, TaxonomyChangeCommand.class));
        });
    }


    private void registerPreviewById(String path) {
        EndpointUtilities.getForDatum(path, (req, resp) -> {
            return taxonomyChangeService.previewById(WebUtilities.getId(req));
        });
    }

}
