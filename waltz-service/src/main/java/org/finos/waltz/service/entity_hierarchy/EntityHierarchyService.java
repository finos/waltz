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

package org.finos.waltz.service.entity_hierarchy;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.hierarchy.FlatNode;
import org.finos.waltz.common.hierarchy.Forest;
import org.finos.waltz.common.hierarchy.HierarchyUtilities;
import org.finos.waltz.common.hierarchy.Node;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.entity_hierarchy.EntityHierarchyDao;
import org.finos.waltz.data.entity_hierarchy.EntityRootsSelectorFactory;
import org.finos.waltz.data.entity_statistic.EntityStatisticDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.orgunit.OrganisationalUnitDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchy;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchyItem;
import org.finos.waltz.model.entity_hierarchy.ImmutableEntityHierarchy;
import org.finos.waltz.model.entity_hierarchy.ImmutableEntityHierarchyItem;
import org.finos.waltz.model.tally.ImmutableTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.service.person_hierarchy.PersonHierarchyService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityKind.PERSON;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.jooq.impl.DSL.select;

@Service
public class EntityHierarchyService {

    private final DSLContext dsl;
    private final ChangeInitiativeDao changeInitiativeDao;
    private final DataTypeDao dataTypeDao;
    private final EntityHierarchyDao entityHierarchyDao;
    private final EntityRootsSelectorFactory entityRootsSelectorFactory = new EntityRootsSelectorFactory();
    private final EntityStatisticDao entityStatisticDao;
    private final MeasurableDao measurableDao;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final PersonHierarchyService personHierarchyService;
    private final PersonDao personDao;

    @Autowired
    public EntityHierarchyService(DSLContext dsl,
                                  ChangeInitiativeDao changeInitiativeDao,
                                  DataTypeDao dataTypeDao,
                                  EntityHierarchyDao entityHierarchyDao,
                                  EntityStatisticDao entityStatisticDao,
                                  MeasurableDao measurableDao,
                                  OrganisationalUnitDao organisationalUnitDao,
                                  PersonHierarchyService personHierarchyService,
                                  PersonDao personDao) {

        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(entityHierarchyDao, "entityHierarchyDao cannot be null");
        checkNotNull(entityStatisticDao, "entityStatisticDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");
        checkNotNull(personHierarchyService, "personHierarchyService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");

        this.dsl = dsl;
        this.changeInitiativeDao = changeInitiativeDao;
        this.dataTypeDao = dataTypeDao;
        this.entityHierarchyDao = entityHierarchyDao;
        this.entityStatisticDao = entityStatisticDao;
        this.measurableDao = measurableDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.personHierarchyService = personHierarchyService;
        this.personDao = personDao;
    }


    public List<Tally<String>> tallyByKind() {
        return ListUtilities.append(
                entityHierarchyDao.tallyByKind(),
                ImmutableTally.<String>builder()
                        .id(EntityKind.PERSON.name())
                        .count(personHierarchyService.count())
                        .build());
    }


    public List<Tally<String>> getRootTallies() {
        return ListUtilities.append(
                entityHierarchyDao.getRootTallies(),
                ImmutableTally.<String>builder()
                        .id(EntityKind.PERSON.name())
                        .count(personHierarchyService.countRoots())
                        .build());
    }


    public List<EntityReference> getRoots(EntityKind kind) {
        Select<Record1<Long>> selector = entityRootsSelectorFactory.apply(kind);

        switch (kind) {
            case CHANGE_INITIATIVE:
                return changeInitiativeDao.findByIdSelectorAsEntityReference(selector);
            case DATA_TYPE:
                return dataTypeDao.findByIdSelectorAsEntityReference(selector);
            case ENTITY_STATISTIC:
                return entityStatisticDao.findByIdSelectorAsEntityReference(selector);
            case MEASURABLE:
                return measurableDao.findByIdSelectorAsEntityReference(selector);
            case ORG_UNIT:
                return organisationalUnitDao.findByIdSelectorAsEntityReference(selector);
            case PERSON:
                return personDao.findByPersonIdSelectorAsEntityReference(selector);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + kind);
        }
    }


    public int buildFor(EntityKind kind) {
        if (kind == PERSON) {
            int[] rc = personHierarchyService.build();
            return rc.length;
        } else {
            Table<?> table = determineTableToRebuild(kind);
            return buildFor(table, kind, DSL.trueCondition(), DSL.trueCondition());
        }
    }


    public int buildForMeasurableByCategory(long categoryId) {
        return buildFor(MEASURABLE,
                        EntityKind.MEASURABLE,
                        MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId),
                        ENTITY_HIERARCHY.ID.in(select(MEASURABLE.ID)
                                                .from(MEASURABLE)
                                                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))));
    }


    private int buildFor(Table<?> table,
                         EntityKind kind,
                         Condition selectFilter,
                         Condition deleteFilter) {
        Collection<FlatNode<Long, Long>> flatNodes = fetchFlatNodes(table, selectFilter);
        List<EntityHierarchyItem> hierarchyItems = convertFlatNodesToHierarchyItems(kind, flatNodes);

        return entityHierarchyDao.replaceHierarchy(kind, hierarchyItems, deleteFilter);
    }


    private List<FlatNode<Long, Long>> fetchFlatNodes(Table<?> table,
                                                      Condition selectFilter) {
        Field<Long> idField = table.field("id", Long.class);
        Field<Long> parentIdField = table.field("parent_id", Long.class);

        checkNotNull(idField, "cannot find id column");
        checkNotNull(parentIdField, "cannot find parent_id column");

        return dsl
                .select(idField, parentIdField)
                .from(table)
                .where(selectFilter)
                .fetch(r -> new FlatNode<>(
                        r.value1(),
                        Optional.ofNullable(r.value2()),
                        r.value1()));
    }


    private List<EntityHierarchyItem> convertFlatNodesToHierarchyItems(EntityKind kind,
                                                                       Collection<FlatNode<Long, Long>> flatNodes) {
        Forest<Long, Long> forest = HierarchyUtilities.toForest(flatNodes);
        Map<Long, Integer> idToLevel = HierarchyUtilities.assignDepths(forest);

        return forest.getAllNodes()
                .values()
                .stream()
                .flatMap(streamItemsForNode(kind, idToLevel))
                .collect(Collectors.toList());
    }


    private Function<Node<Long, Long>, Stream<? extends EntityHierarchyItem>> streamItemsForNode(EntityKind kind,
                                                                                                 Map<Long, Integer> idToLevel) {
        return node -> Stream
                .concat(
                    streamAncestors(kind, idToLevel, node),
                    streamSelf(kind, idToLevel, node));
    }


    private Stream<EntityHierarchyItem> streamSelf(EntityKind kind,
                                                   Map<Long, Integer> idToLevel,
                                                   Node<Long, Long> node) {
        Long nodeId = node.getId();
        Integer level = idToLevel.get(nodeId);
        ImmutableEntityHierarchyItem selfAsEntityHierarchyItem = ImmutableEntityHierarchyItem.builder()
                .id(nodeId)
                .parentId(nodeId)
                .ancestorLevel(level == null ? -1 : level)
                .descendantLevel(idToLevel.getOrDefault(node.getId(), -1))
                .kind(kind)
                .build();
        return Stream.of(selfAsEntityHierarchyItem);
    }


    private Stream<EntityHierarchyItem> streamAncestors(EntityKind kind,
                                                        Map<Long, Integer> idToLevel,
                                                        Node<Long, Long> node) {
        return HierarchyUtilities
            .parents(node)
            .stream()
            .map(p -> ImmutableEntityHierarchyItem.builder()
                    .id(node.getId())
                    .parentId(p.getId())
                    .ancestorLevel(idToLevel.get(p.getId()))
                    .descendantLevel(idToLevel.getOrDefault(node.getId(), -1))
                    .kind(kind)
                    .build());
    }


    private Table<?> determineTableToRebuild(EntityKind kind) {
        switch (kind) {
            case CHANGE_INITIATIVE:
                return Tables.CHANGE_INITIATIVE;
            case DATA_TYPE:
                return Tables.DATA_TYPE;
            case ENTITY_STATISTIC:
                return Tables.ENTITY_STATISTIC_DEFINITION;
            case MEASURABLE:
                return MEASURABLE;
            case ORG_UNIT:
                return Tables.ORGANISATIONAL_UNIT;
            default:
                throw new IllegalArgumentException("Cannot determine hierarchy table for kind: "+kind);
        }
    }

    public EntityHierarchy fetchHierarchyForKind(EntityKind kind) {
        return ImmutableEntityHierarchy
                .builder()
                .hierarchyItems(entityHierarchyDao.fetchHierarchyForKind(kind))
                .build();
    }

}
