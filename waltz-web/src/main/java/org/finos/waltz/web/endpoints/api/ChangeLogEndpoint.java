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

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;
import static org.finos.waltz.common.Checks.checkNotNull;


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
                    Optional<java.util.Date> dateParam = getDateParam(request);
                    Optional<Integer> limitParam = getLimit(request);

                    if (ref.kind() == EntityKind.PERSON) {
                        return service.findByPersonReference(ref, dateParam, limitParam);
                    } else {
                        return service.findByParentReference(ref, dateParam, limitParam);
                    }
                });

        getForList(
                mkPath(BASE_URL, ":kind", ":id", "date-range"),
                (request, response) -> {
                    EntityReference ref = getEntityReference(request);
                    java.sql.Date startDate = getStartDate(request);
                    java.sql.Date endDate = getEndDate(request);
                    Optional<Integer> limitParam = getLimit(request);

                    if (ref.kind() == EntityKind.PERSON) {
                        return service.findByPersonReferenceForDateRange(ref, startDate, endDate, limitParam);
                    } else {
                        return service.findByParentReferenceForDateRange(ref, startDate, endDate, limitParam);
                    }
                });

        getForList(
                mkPath(BASE_URL, ":kind", ":id", "unattested"),
                (request, response) -> {
                    EntityReference ref = getEntityReference(request);
                    return service.findUnattestedChanges(ref);
                });


    }


    private java.sql.Date getStartDate(Request request) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return toSqlDate(formatter.parse(request.queryParams("startDate")));
    }


    private java.sql.Date getEndDate(Request request) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return toSqlDate(formatter.parse(request.queryParams("endDate")));
    }
}
