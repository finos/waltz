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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.schema.tables.records.BookmarkRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import javax.xml.stream.events.DTD;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.Bookmark.BOOKMARK;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


public class BookmarkGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();


    String[] text = new String[] {
            "All about %s",
            "%s Info",
            "%s Data",
            "Docs on %s",
            "Link to %s",
    };

    String[] urls = new String[] {
            "https://en.wikipedia.org/wiki/%s",
            "https://www.bing.com/search?q=%s",
            "https://www.google.co.uk/search?q=%s",
    };

    String[] bookmarkKinds = new String[] {
            "APPLICATION_INSTANCE",
            "BUILD_SYSTEM",
            "BUSINESS_SPECIFICATION",
            "DOCUMENTATION",
            "ISSUE_TRACKER",
            "MONITORING",
            "QUALITY_CONTROL",
            "RETIREMENT_PLAN",
            "SOURCE_CODE_CONTROL",
            "TECHNICAL_SPECIFICATION"
    };


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<EntityReference> appRefs = dsl.select(APPLICATION.NAME, APPLICATION.ID)
                .from(APPLICATION)
                .fetch(r -> EntityReference.mkRef(EntityKind.APPLICATION, r.value2(), r.value1()));

        List<EntityReference> measurableRefs = dsl.select(MEASURABLE.NAME, MEASURABLE.ID)
                .from(MEASURABLE)
                .fetch(r -> EntityReference.mkRef(EntityKind.MEASURABLE, r.value2(), r.value1()));

        List<EntityReference> ouRefs = dsl.select(ORGANISATIONAL_UNIT.NAME, ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch(r -> EntityReference.mkRef(EntityKind.ORG_UNIT, r.value2(), r.value1()));

        List<EntityReference> appGroupRefs = dsl.select(APPLICATION_GROUP.NAME, APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .fetch(r -> EntityReference.mkRef(EntityKind.APP_GROUP, r.value2(), r.value1()));

        List<EntityReference> refs = ListUtilities.concat(appRefs, measurableRefs, ouRefs, appGroupRefs);

        List<BookmarkRecord> bookmarks = refs.stream()
                .flatMap(r -> RandomUtilities
                        .randomlySizedIntStream(2, 15)
                        .mapToObj(idx -> {
                            BookmarkRecord record = dsl.newRecord(BOOKMARK);
                            record.setParentKind(r.kind().name());
                            record.setParentId(r.id());
                            record.setTitle(mkText(r.name().get()));
                            record.setDescription(mkText(r.name().get()));
                            record.setKind(randomPick(bookmarkKinds));
                            record.setUrl(mkUrl(r.name().get()));
                            record.setProvenance(SAMPLE_DATA_PROVENANCE);
                            record.setLastUpdatedBy("admin");
                            record.setUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                            record.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
                            return record;
                        }))
                .collect(Collectors.toList());

        dsl.batchStore(bookmarks).execute();
        return null;
    }

    private String mkText(String name) {
        return String.format(randomPick(text), name);
    }

    private String mkUrl(String name) {
        return String.format(randomPick(urls), name);
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        dsl.deleteFrom(BOOKMARK).where(BOOKMARK.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE)).execute();
        return false;
    }
}
