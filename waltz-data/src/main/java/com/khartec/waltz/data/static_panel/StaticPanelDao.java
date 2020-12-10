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

package com.khartec.waltz.data.static_panel;

import com.khartec.waltz.model.staticpanel.ContentKind;
import com.khartec.waltz.model.staticpanel.ImmutableStaticPanel;
import com.khartec.waltz.model.staticpanel.StaticPanel;
import com.khartec.waltz.schema.tables.records.StaticPanelRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.StaticPanel.STATIC_PANEL;

@Repository
public class StaticPanelDao {

    private final DSLContext dsl;


    private RecordMapper<Record, StaticPanel> panelMapper = r -> {
        StaticPanelRecord record = r.into(STATIC_PANEL);
        return ImmutableStaticPanel.builder()
                .id(record.getId())
                .content(record.getContent())
                .group(record.getGroup())
                .icon(record.getIcon())
                .encoding(ContentKind.valueOf(record.getEncoding()))
                .title(record.getTitle())
                .priority(record.getPriority())
                .width(record.getWidth())
                .build();
    };




    @Autowired
    public StaticPanelDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<StaticPanel> findByGroups(String... groups) {
        return dsl
                .selectFrom(STATIC_PANEL)
                .where(STATIC_PANEL.GROUP.in(groups))
                .orderBy(STATIC_PANEL.PRIORITY.asc())
                .fetch(panelMapper);
    }


    public List<StaticPanel> findAll() {
        return dsl
                .selectFrom(STATIC_PANEL)
                .fetch(panelMapper);
    }


    public boolean update(StaticPanel panel) {
        StaticPanelRecord record = mkRecord(panel);
        return dsl.executeUpdate(record) == 1;
    }


    public boolean create(StaticPanel panel) {
        StaticPanelRecord record = mkRecord(panel);
        return record.insert() == 1;
    }

    private StaticPanelRecord mkRecord(StaticPanel panel) {
        StaticPanelRecord record = dsl.newRecord(STATIC_PANEL);

        panel.id().ifPresent(id -> {
            record.setId(id);
            record.changed(STATIC_PANEL.ID, false);
        });

        record.setContent(panel.content());
        record.setTitle(panel.title());
        record.setWidth(panel.width());
        record.setPriority(panel.priority());
        record.setGroup(panel.group());
        record.setIcon(panel.icon());
        record.setEncoding(panel.encoding().name());

        return record;
    }

}
