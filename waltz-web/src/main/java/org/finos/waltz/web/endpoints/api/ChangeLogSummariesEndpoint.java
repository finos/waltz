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

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.service.changelog.ChangeLogSummariesService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.tally.ChangeLogTally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;


@Service
public class ChangeLogSummariesEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "change-log-summaries");

    private final ChangeLogSummariesService changeLogSummariesService;


    @Autowired
    public ChangeLogSummariesEndpoint(ChangeLogSummariesService changeLogSummariesService) {
        checkNotNull(changeLogSummariesService, "changeLogSummariesService must not be null");

        this.changeLogSummariesService = changeLogSummariesService;
    }

    @Override
    public void register() {

        String findSummariesForDateRangePath = mkPath(BASE_URL, "kind", ":kind", "selector");
        String findYearOnYearChangesPath = mkPath(BASE_URL, "year_on_year");

        ListRoute<ChangeLogTally> findSummariesForDateRangeRoute = (request, response) -> {

            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);

            Optional<Integer> limit = getLimit(request);

            return changeLogSummariesService.findCountByParentAndChildKindForDateRangeBySelector(
                    getKind(request),
                    idSelectionOptions,
                    getStartDate(request),
                    getEndDate(request),
                    limit);
        };

        DatumRoute<Map<Integer, Long>> findYearOnYearChangesRoute = (request, response) -> {
            EntityKind[] validKinds = EntityKind.values();

            EntityKind parentKind = Arrays
                    .stream(validKinds)
                    .noneMatch(t -> StringUtilities.safeEq(t.name(), request.queryParams("parentKind"))) ? null
                        : EntityKind.valueOf(request.queryParams("parentKind"));

            EntityKind childKind = Arrays
                    .stream(validKinds)
                    .noneMatch(t -> StringUtilities.safeEq(t.name(), request.queryParams("childKind"))) ? null
                    : EntityKind.valueOf(request.queryParams("childKind"));

            return changeLogSummariesService.findYearOnYearChanges(parentKind, childKind);
        };

        postForList(findSummariesForDateRangePath, findSummariesForDateRangeRoute);
        getForDatum(findYearOnYearChangesPath, findYearOnYearChangesRoute);
    }


    private Date getDate(Request request) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return toSqlDate(formatter.parse(request.queryParams("date")));
    }


    private Date getStartDate(Request request) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return toSqlDate(formatter.parse(request.queryParams("startDate")));
    }


    private Date getEndDate(Request request) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return toSqlDate(formatter.parse(request.queryParams("endDate")));
    }
}
