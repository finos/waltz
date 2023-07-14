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

package org.finos.waltz.data.flow_diagram;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.flow_diagram.FlowDiagram;
import org.finos.waltz.model.flow_diagram.ImmutableFlowDiagram;
import org.finos.waltz.schema.tables.records.FlowDiagramRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.schema.tables.FlowDiagram.FLOW_DIAGRAM;
import static org.finos.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;

@Repository
public class FlowDiagramDao {

    private static final RecordMapper<Record, FlowDiagram> TO_DOMAIN_MAPPER = r -> {
        FlowDiagramRecord record = r.into(FLOW_DIAGRAM);
        return ImmutableFlowDiagram.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .layoutData(record.getLayoutData())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isRemoved(record.getIsRemoved())
                .editorRole(Optional.ofNullable(record.getEditorRole()))
                .build();
    };


    public static final Function<FlowDiagram, FlowDiagramRecord> TO_RECORD_MAPPER = fd -> {
        FlowDiagramRecord record = new FlowDiagramRecord();

        fd.id().ifPresent(record::setId);
        record.setName(fd.name());
        record.setDescription(fd.description());
        record.setLayoutData(fd.layoutData());
        record.setLastUpdatedBy(fd.lastUpdatedBy());
        record.setLastUpdatedAt(Timestamp.valueOf(fd.lastUpdatedAt()));
        record.setIsRemoved(fd.isRemoved());
        fd.editorRole().ifPresent(record::setEditorRole);
        return record;
    };


    private final DSLContext dsl;

    private final Condition notRemoved = FLOW_DIAGRAM.IS_REMOVED.eq(false);


    @Autowired
    public FlowDiagramDao(DSLContext dsl) {
        checkNotNull(dsl, " cannot be null");
        this.dsl = dsl;
    }


    public FlowDiagram getById(long id) {
        return dsl
                .select(FLOW_DIAGRAM.fields())
                .from(FLOW_DIAGRAM)
                .where(FLOW_DIAGRAM.ID.eq(id))
                .and(notRemoved)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<FlowDiagram> findByEntityReference(EntityReference ref) {
        // cannot do a select distinct as LAYOUT is a clob
        return dsl
                .select(FLOW_DIAGRAM.fields())
                .from(FLOW_DIAGRAM)
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(FLOW_DIAGRAM.ID))
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(ref.id()))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(ref.kind().name()))
                .and(notRemoved)
                .fetch(TO_DOMAIN_MAPPER)
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }


    public List<FlowDiagram> findForSelector(Select<Record1<Long>> selector) {
        return dsl
                .select(FLOW_DIAGRAM.fields())
                .from(FLOW_DIAGRAM)
                .where(FLOW_DIAGRAM.ID.in(selector))
                .and(notRemoved)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(FlowDiagram flowDiagram) {
        FlowDiagramRecord record = TO_RECORD_MAPPER.apply(flowDiagram);
        return dsl.insertInto(FLOW_DIAGRAM)
                .set(record)
                .returning(FLOW_DIAGRAM.ID)
                .fetchOne()
                .getId();
    }


    public boolean update(FlowDiagram flowDiagram) {
        FlowDiagramRecord record = TO_RECORD_MAPPER.apply(flowDiagram);
        record.changed(FLOW_DIAGRAM.ID, false);
        return dsl.executeUpdate(record) == 1;
    }


    public boolean updateName(long id, String name) {
        return dsl
                .update(FLOW_DIAGRAM)
                .set(FLOW_DIAGRAM.NAME, name)
                .where(FLOW_DIAGRAM.ID.eq(id))
                .execute() == 1;
    }

    public boolean updateDescription(long id, String des) {
        return dsl
                .update(FLOW_DIAGRAM)
                .set(FLOW_DIAGRAM.DESCRIPTION, des)
                .where(FLOW_DIAGRAM.ID.eq(id))
                .execute() == 1;
    }

    public boolean deleteById(long id, String username) {
        return dsl
                .update(FLOW_DIAGRAM)
                .set(FLOW_DIAGRAM.IS_REMOVED, true)
                .set(FLOW_DIAGRAM.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(FLOW_DIAGRAM.LAST_UPDATED_BY, username)
                .where(FLOW_DIAGRAM.ID.eq(id))
                .execute() == 1;
    }

    public Long clone(long diagramId, String newName, String userId) {
        FlowDiagram diagram = getById(diagramId);
        FlowDiagram copiedDiagram = ImmutableFlowDiagram
                .copyOf(diagram)
                .withId(Optional.empty())
                .withName(newName)
                .withLastUpdatedBy(userId)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withEditorRole(Optional.empty());

        return create(copiedDiagram);
    }

    public Collection<FlowDiagram> search(EntitySearchOptions options) {

        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return newArrayList();
        }

        Condition likeName = JooqUtilities.mkBasicTermSearch(FLOW_DIAGRAM.NAME, terms);

        SelectQuery<Record> query = dsl
                .select(FLOW_DIAGRAM.fields())
                .from(FLOW_DIAGRAM)
                .where(FLOW_DIAGRAM.IS_REMOVED.isFalse()
                .and(likeName))
                .orderBy(FLOW_DIAGRAM.NAME)
                .limit(options.limit())
                .getQuery();

        List<FlowDiagram> results = query
                .fetch(TO_DOMAIN_MAPPER);

        results.sort(SearchUtilities.mkRelevancyComparator(
                NameProvider::name,
                terms.get(0)));

        return results;
    }
}
