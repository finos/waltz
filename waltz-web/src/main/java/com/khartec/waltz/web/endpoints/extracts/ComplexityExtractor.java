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


import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.APPLICATION;
import static com.khartec.waltz.schema.Tables.COMPLEXITY_SCORE;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readAppIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class ComplexityExtractor extends BaseDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ComplexityExtractor(DSLContext dsl) {
        super(dsl);
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
    }


    @Override
    public void register() {

        String path = mkPath("data-extract", "complexity", "all");
        post(path, (request, response) -> {
            ApplicationIdSelectionOptions applicationIdSelectionOptions = readAppIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(applicationIdSelectionOptions);

            SelectConditionStep<Record4<String, String, String, BigDecimal>> qry = dsl
                    .select(APPLICATION.NAME.as("Application Name"),
                            APPLICATION.ASSET_CODE.as("Asset Code"),
                            COMPLEXITY_SCORE.COMPLEXITY_KIND.as("Complexity Kind"),
                            COMPLEXITY_SCORE.SCORE.as("Score"))
                    .from(COMPLEXITY_SCORE)
                    .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(COMPLEXITY_SCORE.ENTITY_ID))
                    .where(COMPLEXITY_SCORE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                    .and(COMPLEXITY_SCORE.ENTITY_ID.in(selector));


            return writeExtract(
                    "complexity",
                    qry,
                    request,
                    response);
        });
    }

}
