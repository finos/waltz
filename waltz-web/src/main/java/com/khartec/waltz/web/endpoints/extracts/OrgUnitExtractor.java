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

package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class OrgUnitExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(OrgUnitExtractor.class);

    private OrganisationalUnitService orgUnitService;


    @Autowired
    public OrgUnitExtractor(DSLContext dsl,
                            OrganisationalUnitService orgUnitService) {
        super(dsl);
        checkNotNull(orgUnitService, "orgUnitService cannot be null");

        this.orgUnitService = orgUnitService;
    }


    @Override
    public void register() {
        get(mkPath("data-extract", "org-units"), (request, response) ->
                writeFile("organisational-units.csv", extract(), response));
    }


    private CSVSerializer extract() {
        return csvWriter -> {
            csvWriter.writeHeader(
                    "id",
                    "parentId",
                    "name",
                    "description");

            orgUnitService.findAll()
                    .forEach(ou -> {
                        try {
                            csvWriter.write(
                                    ou.id().orElse(null),
                                    ou.parentId().orElse(null),
                                    ou.name(),
                                    ou.description());
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write ou: " + ou, ioe);
                        }
                    });
        };
    }

}
