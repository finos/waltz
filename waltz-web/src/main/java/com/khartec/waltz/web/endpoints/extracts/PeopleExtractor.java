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

package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.SelectSeekStep1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.post;


@Service
public class PeopleExtractor extends DirectQueryBasedDataExtractor {


    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


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
            IdSelectionOptions selectionOptions = mkOpts(entityRef, HierarchyQueryScope.determineUpwardsScopeForKind(entityRef.kind()));
            GenericSelector selector = genericSelectorFactory.apply(selectionOptions);

            SelectSeekStep1<Record5<String, String, String, String, String>, String> qry = dsl
                    .select(PERSON.DISPLAY_NAME.as("Name"),
                            PERSON.TITLE.as("Title"),
                            PERSON.OFFICE_PHONE.as("Telephone"),
                            PERSON.EMAIL.as("Email"),
                            INVOLVEMENT_KIND.NAME.as("Role"))
                    .from(PERSON)
                    .innerJoin(INVOLVEMENT).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                    .innerJoin(INVOLVEMENT_KIND).on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                    .where(INVOLVEMENT.ENTITY_ID.in(selector.selector())
                            .and(INVOLVEMENT.ENTITY_KIND.eq(selector.kind().name())))
                    .orderBy(PERSON.DISPLAY_NAME);

            return writeExtract(
                    "involved_people",
                    qry,
                    request,
                    response);
        });
    }
}
