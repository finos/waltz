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

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidationResult;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyChangeService;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser.InputFormat;
import org.finos.waltz.service.taxonomy_management.TaxonomyChangeService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.getEntityReference;
import static org.finos.waltz.web.WebUtilities.getUsername;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readEnum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.deleteForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class TaxonomyManagementEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "taxonomy-management");

    private final TaxonomyChangeService taxonomyChangeService;
    private final BulkTaxonomyChangeService bulkTaxonomyChangeService;


    @Autowired
    public TaxonomyManagementEndpoint(TaxonomyChangeService taxonomyChangeService,
                                      BulkTaxonomyChangeService bulkTaxonomyChangeService) {
        this.taxonomyChangeService = taxonomyChangeService;
        this.bulkTaxonomyChangeService = bulkTaxonomyChangeService;
    }


    @Override
    public void register() {
        registerPreview(mkPath(BASE_URL, "preview"));
        registerSubmitPendingChange(mkPath(BASE_URL, "pending-changes"));
        registerRemoveById(mkPath(BASE_URL, "pending-changes", "id", ":id"));
        registerPreviewById(mkPath(BASE_URL, "pending-changes", "id", ":id", "preview"));
        registerApplyPendingChange(mkPath(BASE_URL, "pending-changes", "id", ":id", "apply"));
        registerFindPendingChangesByDomain(mkPath(BASE_URL, "pending-changes", "by-domain", ":kind", ":id"));
        registerFindAllChangesByDomain(mkPath(BASE_URL, "all", "by-domain", ":kind", ":id"));
        registerPreviewBulkTaxonomyChanges(mkPath(BASE_URL, "bulk", "preview", ":kind", ":id"));
        registerApplyBulkTaxonomyChanges(mkPath(BASE_URL, "bulk", "apply", ":kind", ":id"));
    }


    private void registerApplyBulkTaxonomyChanges(String path) {
        postForDatum(path, (req, resp) -> {
            String userId = getUsername(req);
            EntityReference taxonomyRef = getEntityReference(req);
            InputFormat format = readEnum(req, "format", InputFormat.class, s -> InputFormat.TSV);
            BulkUpdateMode mode = readEnum(req, "mode", BulkUpdateMode.class, s -> BulkUpdateMode.ADD_ONLY);
            String body = req.body();
            BulkTaxonomyValidationResult validationResult = bulkTaxonomyChangeService.previewBulk(taxonomyRef, body, format, mode);
            return bulkTaxonomyChangeService.applyBulk(taxonomyRef, validationResult, userId);
        });
    }

    private void registerPreviewBulkTaxonomyChanges(String path) {
        postForDatum(path, (req, resp) -> {
            EntityReference taxonomyRef = getEntityReference(req);
            InputFormat format = readEnum(req, "format", InputFormat.class, s -> InputFormat.TSV);
            BulkUpdateMode mode = readEnum(req, "mode", BulkUpdateMode.class, s -> BulkUpdateMode.ADD_ONLY);
            String body = req.body();
            return bulkTaxonomyChangeService.previewBulk(taxonomyRef, body, format, mode);
        });
    }


    private void registerFindAllChangesByDomain(String path) {
        getForList(path, (req, resp) -> {
            return taxonomyChangeService.findAllChangesByDomain(WebUtilities.getEntityReference(req));
        });
    }


    private void registerApplyPendingChange(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.applyById(
                    WebUtilities.getId(req),
                    WebUtilities.getUsername(req));
        });
    }


    private void registerSubmitPendingChange(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.submitDraftChange(
                    WebUtilities.readBody(req, TaxonomyChangeCommand.class),
                    WebUtilities.getUsername(req));
        });
    }


    private void registerFindPendingChangesByDomain(String path) {
        getForList(path, (req, resp) -> {
            return taxonomyChangeService.findDraftChangesByDomain(WebUtilities.getEntityReference(req));
        });
    }


    private void registerRemoveById(String path) {
        deleteForDatum(path, (req, resp) -> {
            return taxonomyChangeService.removeById(
                    WebUtilities.getId(req),
                    WebUtilities.getUsername(req));
        });
    }

    private void registerPreview(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.preview(WebUtilities.readBody(req, TaxonomyChangeCommand.class));
        });
    }


    private void registerPreviewById(String path) {
        EndpointUtilities.getForDatum(path, (req, resp) -> {
            return taxonomyChangeService.previewById(WebUtilities.getId(req));
        });
    }

}
