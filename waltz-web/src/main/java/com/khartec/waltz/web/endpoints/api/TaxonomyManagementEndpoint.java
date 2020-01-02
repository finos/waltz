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

import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import com.khartec.waltz.service.taxonomy_management.TaxonomyChangeService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class TaxonomyManagementEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "taxonomy-management");

    private final TaxonomyChangeService taxonomyChangeService;


    @Autowired
    public TaxonomyManagementEndpoint(TaxonomyChangeService taxonomyChangeService) {
        this.taxonomyChangeService = taxonomyChangeService;
    }


    @Override
    public void register() {
        registerPreview(mkPath(BASE_URL, "preview"));
        registerSubmitPendingChange(mkPath(BASE_URL, "pending-changes"));
        registerRemoveById(mkPath(BASE_URL, "pending-changes", "id", ":id"));
        registerPreviewById(mkPath(BASE_URL, "pending-changes", "id", ":id", "preview"));
        registerApplyPendingChange(mkPath(BASE_URL, "pending-changes", "id", ":id", "apply"));
        registerFindPendingChangesByDomain(mkPath(BASE_URL, "pending-changes", "by-domain", ":kind", ":id"));
    }


    private void registerApplyPendingChange(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.applyById(
                    getId(req),
                    getUsername(req));
        });
    }


    private void registerSubmitPendingChange(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.submitDraftChange(
                    readBody(req, TaxonomyChangeCommand.class),
                    getUsername(req));
        });
    }


    private void registerFindPendingChangesByDomain(String path) {
        getForList(path, (req, resp) -> {
            return taxonomyChangeService.findDraftChangesByDomain(getEntityReference(req));
        });
    }


    private void registerRemoveById(String path) {
        deleteForDatum(path, (req, resp) -> {
            return taxonomyChangeService.removeById(
                    getId(req),
                    getUsername(req));
        });
    }

    private void registerPreview(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.preview(readBody(req, TaxonomyChangeCommand.class));
        });
    }


    private void registerPreviewById(String path) {
        getForDatum(path, (req, resp) -> {
            return taxonomyChangeService.previewById(getId(req));
        });
    }

}
