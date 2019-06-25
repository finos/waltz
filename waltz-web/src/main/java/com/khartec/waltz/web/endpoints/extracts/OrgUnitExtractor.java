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


import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class OrgUnitExtractor extends BaseDataExtractor {


    @Autowired
    public OrgUnitExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        get(mkPath("data-extract", "org-units"), (request, response) ->
                writeExtract(
                        "organisational-units",
                        prepareExtract(),
                        request,
                        response));
    }


    private SelectJoinStep<Record6<Long, Long, String, String, String, String>> prepareExtract() {
        return dsl
                .select(
                    ORGANISATIONAL_UNIT.ID.as("id"),
                    ORGANISATIONAL_UNIT.PARENT_ID.as("parentId"),
                    ORGANISATIONAL_UNIT.NAME.as("name"),
                    ORGANISATIONAL_UNIT.DESCRIPTION.as("description"),
                    ORGANISATIONAL_UNIT.EXTERNAL_ID.as("externalId"),
                    ORGANISATIONAL_UNIT.PROVENANCE.as("provenance"))
                .from(ORGANISATIONAL_UNIT);
    }

}
