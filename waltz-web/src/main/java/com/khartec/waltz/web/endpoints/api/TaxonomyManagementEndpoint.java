/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.taxonomy_management.ImmutableTaxonomyChangeCommand;
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
        registerListPendingChanges(mkPath(BASE_URL, "pending-changes"));
        registerApplyChange(mkPath(BASE_URL, "pending-changes", ":id"));
    }


    private void registerApplyChange(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.applyById(
                    getId(req),
                    getUsername(req));
        });
    }


    private void registerSubmitPendingChange(String path) {
        postForDatum(path, (req, resp) -> {
            TaxonomyChangeCommand cmd = ImmutableTaxonomyChangeCommand
                    .copyOf(readBody(req, TaxonomyChangeCommand.class))
                    .withCreatedAt(DateTimeUtilities.nowUtc())
                    .withCreatedBy(getUsername(req));
            return taxonomyChangeService.submitPendingChange(cmd);
        });
    }


    private void registerListPendingChanges(String path) {
        getForList(path, (req, resp) -> {
            return taxonomyChangeService.getPendingChanges();
        });
    }


    private void registerPreview(String path) {
        postForDatum(path, (req, resp) -> {
            return taxonomyChangeService.preview(readBody(req, TaxonomyChangeCommand.class));
        });
    }

}
