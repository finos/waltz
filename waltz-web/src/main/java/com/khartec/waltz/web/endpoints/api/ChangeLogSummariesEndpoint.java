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

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.tally.ChangeLogTally;
import com.khartec.waltz.service.changelog.ChangeLogSummariesService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


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

        String findSummariesForDatePath = mkPath(BASE_URL, "kind", ":kind", "selector");

        ListRoute<ChangeLogTally> findSummariesForDateRoute = (request, response) -> {

            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);

            Date date = getDate(request);
            Optional<Integer> limit = getLimit(request);

            return changeLogSummariesService.findCountByParentAndChildKindForDateBySelector(
                    getKind(request),
                    idSelectionOptions,
                    date,
                    limit);
        };

        postForList(findSummariesForDatePath, findSummariesForDateRoute);
    }


    private Date getDate(Request request) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return toSqlDate(formatter.parse(request.queryParams("date")));
    }
}
