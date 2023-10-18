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

package org.finos.waltz.web.endpoints.extracts;


import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.involvement.InvolvementViewDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        registerExtractForEntity( WebUtilities.mkPath("data-extract", "people", "entity", ":kind", ":id"));
    }


    private void registerExtractForEntity(String path) {
        post(path, (request, response) -> {

            EntityReference entityRef = WebUtilities.getEntityReference(request);

            Select<Record> qry = InvolvementViewDao.mkInvolvementExtractorQuery(dsl, entityRef);

            return writeExtract(
                    "involved_people",
                    qry,
                    request,
                    response);
        });
    }
}
