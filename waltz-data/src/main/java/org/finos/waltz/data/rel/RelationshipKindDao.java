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

package org.finos.waltz.data.rel;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rel.ImmutableRelationshipKind;
import org.finos.waltz.model.rel.RelationshipKind;
import org.finos.waltz.model.rel.UpdateRelationshipKindCommand;
import org.finos.waltz.schema.tables.records.RelationshipKindRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.function.Function;

import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.RELATIONSHIP_KIND;

@Repository
public class RelationshipKindDao {

    private static final RecordMapper<Record, RelationshipKind> TO_DOMAIN_MAPPER = r -> {
        RelationshipKindRecord record = r.into(RelationshipKindRecord.class);
        return ImmutableRelationshipKind
                .builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .isReadonly(record.getIsReadonly())
                .kindA(EntityKind.valueOf(record.getKindA()))
                .kindB(EntityKind.valueOf(record.getKindB()))
                .categoryA(record.getCategoryA())
                .categoryB(record.getCategoryB())
                .code(record.getCode())
                .reverseName(record.getReverseName())
                .position(record.getPosition())
                .reverseName(record.getReverseName())
                .build();
    };


    private static final Function<RelationshipKind, RelationshipKindRecord> TO_RECORD_MAPPER = relKind -> {
        RelationshipKindRecord record = new RelationshipKindRecord();
        record.setName(relKind.name());
        record.setCode(relKind.code());
        record.setDescription(relKind.description());
        record.setKindA(relKind.kindA().name());
        record.setKindB(relKind.kindB().name());
        record.setCategoryA(relKind.categoryA());
        record.setCategoryB(relKind.categoryB());
        record.setPosition(relKind.position());
        record.setIsReadonly(relKind.isReadonly());
        record.setReverseName(relKind.reverseName());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public RelationshipKindDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<RelationshipKind> findAll() {
        return dsl
                .select(RELATIONSHIP_KIND.fields())
                .from(RELATIONSHIP_KIND)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<RelationshipKind> findRelationshipKindsBetweenEntites(EntityReference parent, EntityReference target){

        Condition categoryACondition = (isMeasurable(parent))
                ? RELATIONSHIP_KIND.CATEGORY_A.isNull().or(RELATIONSHIP_KIND.CATEGORY_A.eq(getCategory(parent.id())))
                : RELATIONSHIP_KIND.CATEGORY_A.isNull();

        Condition categoryBCondition = (isMeasurable(target))
                ? RELATIONSHIP_KIND.CATEGORY_B.isNull().or(RELATIONSHIP_KIND.CATEGORY_B.eq(getCategory(target.id())))
                : RELATIONSHIP_KIND.CATEGORY_B.isNull();

        return dsl
                .select(RELATIONSHIP_KIND.fields())
                .from(RELATIONSHIP_KIND)
                .where(RELATIONSHIP_KIND.KIND_A.eq(parent.kind().name())
                        .and(RELATIONSHIP_KIND.KIND_B.eq(target.kind().name())
                                .and(categoryACondition)
                                .and(categoryBCondition)))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public boolean create(RelationshipKind relationshipKind){
        return dsl.executeInsert(TO_RECORD_MAPPER.apply(relationshipKind)) == 1;
    }


    public boolean remove(Long id){
        return dsl
                .deleteFrom(RELATIONSHIP_KIND)
                .where(RELATIONSHIP_KIND.ID.eq(id))
                .execute() == 1;
    }


    private SelectConditionStep<Record1<Long>> getCategory(long measurableId) {
        return DSL
                .select(MEASURABLE.MEASURABLE_CATEGORY_ID)
                .from(MEASURABLE)
                .where(MEASURABLE.ID.eq(measurableId));
    }


    private Boolean isMeasurable(EntityReference ref) {
        return ref.kind().equals(EntityKind.MEASURABLE);
    }


    public boolean update(long relKindId, UpdateRelationshipKindCommand updateCommand) {
        return dsl
                .update(RELATIONSHIP_KIND)
                .set(RELATIONSHIP_KIND.NAME, updateCommand.name())
                .set(RELATIONSHIP_KIND.REVERSE_NAME, updateCommand.reverseName())
                .set(RELATIONSHIP_KIND.DESCRIPTION, updateCommand.description())
                .set(RELATIONSHIP_KIND.POSITION, updateCommand.position())
                .where(RELATIONSHIP_KIND.ID.eq(relKindId))
                .execute() == 1;
    }
}
