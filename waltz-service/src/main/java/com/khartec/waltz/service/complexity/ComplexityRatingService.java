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
package com.khartec.waltz.service.complexity;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.complexity.ComplexityScoreDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.schema.tables.records.ComplexityScoreRecord;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


@Service
public class ComplexityRatingService {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexityRatingService.class);

    private final ComplexityScoreDao complexityScoreDao;
    private final MeasurableComplexityService measurableComplexityService;
    private final ConnectionComplexityService connectionComplexityService;
    private final ServerComplexityService serverComplexityService;

    private final ApplicationIdSelectorFactory appIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ComplexityRatingService(ComplexityScoreDao complexityScoreDao,
                                   MeasurableComplexityService measurableComplexityService,
                                   ConnectionComplexityService connectionComplexityService,
                                   ServerComplexityService serverComplexityService) {

        checkNotNull(complexityScoreDao, "complexityScoreDao cannot be null");
        checkNotNull(measurableComplexityService, "measurableComplexityService cannot be null");
        checkNotNull(connectionComplexityService, "connectionComplexityService cannot be null");
        checkNotNull(serverComplexityService, "serverComplexityService cannot be null");

        this.complexityScoreDao = complexityScoreDao;
        this.measurableComplexityService = measurableComplexityService;
        this.connectionComplexityService = connectionComplexityService;
        this.serverComplexityService = serverComplexityService;
    }


    public ComplexityRating getForApp(long appId) {
        return complexityScoreDao.getForApp(appId);
    }



    /**
     * Find connection complexity of the given applications The complexity
     * ratings are baselined against the application with the most
     * connections in the system.  If you wish specify a specific baseline use
     * the overloaded method.
     * @param options
     * @return
     */
    public List<ComplexityRating> findForAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = appIdSelectorFactory.apply(options);
        return complexityScoreDao.findForAppIdSelector(appIdSelector);
    }


    public int rebuild() {

        LOG.info("Rebuild complexity score table");
        SelectJoinStep<Record1<Long>> appIdSelector = DSL.select(APPLICATION.ID).from(APPLICATION);

        List<ComplexityScore> connectionScores = connectionComplexityService.findByAppIdSelector(appIdSelector);
        List<ComplexityScore> serverScores = serverComplexityService.findByAppIdSelector(appIdSelector);
        List<ComplexityScore> measurableScores = measurableComplexityService.findByAppIdSelector(appIdSelector);

        List<ComplexityScoreRecord> records = ListUtilities.concat(
                map(serverScores, r -> buildComplexityScoreRecord(r, ComplexityKind.SERVER)),
                map(connectionScores, r -> buildComplexityScoreRecord(r, ComplexityKind.CONNECTION)),
                map(measurableScores, r -> buildComplexityScoreRecord(r, ComplexityKind.MEASURABLE)));

        LOG.info("Scrubbing existing complexity score table");
        complexityScoreDao.deleteAll();

        LOG.info("Inserting {} new records into complexity score table", records.size());
        int[] results = complexityScoreDao.bulkInsert(records);

        LOG.info("Completed insertion of new records, results: {}", results.length);
        return results.length;
    }


    private static ComplexityScoreRecord buildComplexityScoreRecord(ComplexityScore r, ComplexityKind kind) {
        ComplexityScoreRecord record = new ComplexityScoreRecord();
        record.setEntityKind(EntityKind.APPLICATION.name());
        record.setEntityId(r.id());
        record.setComplexityKind(kind.name());
        record.setScore(BigDecimal.valueOf(r.score()));
        return record;
    }
}
