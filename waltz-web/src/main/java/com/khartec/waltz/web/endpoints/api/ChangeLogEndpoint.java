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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class ChangeLogEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "change-log");

    private final ChangeLogService service;


    @Autowired
    public ChangeLogEndpoint(ChangeLogService changeLogService) {
        checkNotNull(changeLogService, "changeLogService must not be null");
        this.service = changeLogService;
    }

    @Override
    public void register() {

        getForList(
                mkPath(BASE_URL, "user", ":userId"),
                (request, response) -> service.findByUser(request.params("userId"), getLimit(request)));

        postForList(
                mkPath(BASE_URL, "summaries", ":kind"),
                (request, response) -> service.findCountByDateForParentKindBySelector(
                        getKind(request),
                        readIdSelectionOptionsFromBody(request),
                        getLimit(request)));

        getForList(
                mkPath(BASE_URL, ":kind", ":id"),
                (request, response) -> {
                    EntityReference ref = getEntityReference(request);
                    Optional<Date> dateParam = getDateParam(request);
                    Optional<Integer> limitParam = getLimit(request);

                    if(ref.kind() == EntityKind.PERSON) {
                        return service.findByPersonReference(ref, dateParam, limitParam);
                    } else {
                        return service.findByParentReference(ref, dateParam, limitParam);
                    }
                });

        getForList(
                mkPath(BASE_URL, ":kind", ":id", "unattested"),
                (request, response) -> {
                    EntityReference ref = getEntityReference(request);
                    return service.findUnattestedChanges(ref);
                });


    }
}
