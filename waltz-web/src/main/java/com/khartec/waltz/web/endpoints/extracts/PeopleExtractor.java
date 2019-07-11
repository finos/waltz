/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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


import com.khartec.waltz.model.EntityReference;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.SelectSeekStep1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.post;


@Service
public class PeopleExtractor extends BaseDataExtractor{


    @Autowired
    public PeopleExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        registerExtractForApp( mkPath("data-extract", "people", "entity", ":kind", ":id"));
    }


    private void registerExtractForApp(String path) {
        post(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);

            SelectSeekStep1<Record4<String, String, String, String>, String> qry = dsl
                    .select(PERSON.DISPLAY_NAME.as("Name"),
                            PERSON.TITLE.as("Title"),
                            PERSON.OFFICE_PHONE.as("Telephone"),
                            INVOLVEMENT_KIND.NAME.as("Role"))
                    .from(PERSON)
                    .innerJoin(INVOLVEMENT).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                    .innerJoin(INVOLVEMENT_KIND).on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                    .where(INVOLVEMENT.ENTITY_ID.eq(entityRef.id())
                            .and(INVOLVEMENT.ENTITY_KIND.eq(entityRef.kind().name())))
                    .orderBy(PERSON.DISPLAY_NAME);

            return writeExtract(
                    "Involved_people",
                    qry,
                    request,
                    response);
        });
    }
}
