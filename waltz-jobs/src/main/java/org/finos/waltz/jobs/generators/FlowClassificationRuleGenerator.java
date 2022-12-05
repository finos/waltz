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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.ColorUtilities.HexStrings;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.schema.tables.records.FlowClassificationRecord;
import org.finos.waltz.schema.tables.records.FlowClassificationRuleRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.StringUtilities.capitalise;
import static org.finos.waltz.schema.Tables.FLOW_CLASSIFICATION;
import static org.finos.waltz.schema.Tables.FLOW_CLASSIFICATION_RULE;
import static org.finos.waltz.schema.tables.DataType.DATA_TYPE;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

/**
 * Created by dwatkins on 04/03/2017.
 */
public class FlowClassificationRuleGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);

        List<Long> ouIds = dsl
                .select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch(ORGANISATIONAL_UNIT.ID);

        List<Long> typeIds = dsl
                .select(DATA_TYPE.ID)
                .from(DATA_TYPE)
                .fetch(DATA_TYPE.ID);

        List<AuthoritativenessRatingValue> classificationValues = asList(
                AuthoritativenessRatingValue.of("PRIMARY"),
                AuthoritativenessRatingValue.of("SECONDARY"),
                AuthoritativenessRatingValue.DISCOURAGED,
                AuthoritativenessRatingValue.NO_OPINION);

        List<String> colors = asList(HexStrings.AMBER, HexStrings.RED, HexStrings.GREEN);

        Set<FlowClassificationRecord> classificationRecords = classificationValues
                .stream()
                .map(v -> {
                    FlowClassificationRecord record = dsl.newRecord(FLOW_CLASSIFICATION);
                    record.setCode(v.value());
                    record.setName(capitalise(v.value()));
                    record.setColor(randomPick(colors));
                    return record;
                })
                .collect(toSet());

        dsl.batchInsert(classificationRecords).execute();

        List<Long> classificationIds = dsl
                .select(FLOW_CLASSIFICATION.ID)
                .from(FLOW_CLASSIFICATION)
                .where(FLOW_CLASSIFICATION.CODE.notIn(
                        AuthoritativenessRatingValue.DISCOURAGED.value(),
                        AuthoritativenessRatingValue.NO_OPINION.value()))
                .fetch(FLOW_CLASSIFICATION.ID);

        List<FlowClassificationRuleRecord> records = typeIds.stream()
                .flatMap(t -> IntStream
                        .range(0, 2 + rnd.nextInt(2))
                        .mapToObj(i -> {
                            FlowClassificationRuleRecord record = dsl.newRecord(FLOW_CLASSIFICATION_RULE);
                            record.setDataTypeId(t);
                            record.setFlowClassificationId(randomPick(classificationIds));
                            record.setSubjectEntityId(randomPick(appIds));
                            record.setSubjectEntityKind(EntityKind.APPLICATION.name());
                            record.setParentId(randomPick(ouIds));
                            record.setParentKind(EntityKind.ORG_UNIT.name());
                            record.setProvenance(SAMPLE_DATA_PROVENANCE);
                            record.setLastUpdatedAt(nowUtcTimestamp());
                            record.setLastUpdatedBy(SAMPLE_DATA_USER);
                            return record;
                        }))
                .collect(Collectors.toList());

        dsl.batchStore(records).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {

        getDsl(ctx)
                .deleteFrom(FLOW_CLASSIFICATION_RULE)
                .where(FLOW_CLASSIFICATION_RULE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();

        getDsl(ctx)
                .deleteFrom(FLOW_CLASSIFICATION)
                .execute();

        return true;
    }
}
