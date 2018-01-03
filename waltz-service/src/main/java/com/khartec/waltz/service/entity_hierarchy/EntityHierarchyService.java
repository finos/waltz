/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.entity_hierarchy;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.entity_hierarchy.EntityHierarchyDao;
import com.khartec.waltz.data.entity_hierarchy.EntityRootsSelectorFactory;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDao;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_hierarchy.EntityHierarchyItem;
import com.khartec.waltz.model.entity_hierarchy.ImmutableEntityHierarchyItem;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.PERSON;

@Service
public class EntityHierarchyService {

    private final DSLContext dsl;
    private final ChangeInitiativeDao changeInitiativeDao;
    private final DataTypeDao dataTypeDao;
    private final EntityHierarchyDao entityHierarchyDao;
    private final EntityRootsSelectorFactory entityRootsSelectorFactory;
    private final EntityStatisticDao entityStatisticDao;
    private final MeasurableDao measurableDao;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final PersonHierarchyService personHierarchyService;

    @Autowired
    public EntityHierarchyService(DSLContext dsl,
                                  ChangeInitiativeDao changeInitiativeDao,
                                  DataTypeDao dataTypeDao,
                                  EntityHierarchyDao entityHierarchyDao,
                                  EntityRootsSelectorFactory entityRootsSelectorFactory,
                                  EntityStatisticDao entityStatisticDao,
                                  MeasurableDao measurableDao, 
                                  OrganisationalUnitDao organisationalUnitDao,
                                  PersonHierarchyService personHierarchyService) {

        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(entityHierarchyDao, "entityHierarchyDao cannot be null");
        checkNotNull(entityRootsSelectorFactory, "entityRootsSelectorFactory cannot be null");
        checkNotNull(entityStatisticDao, "entityStatisticDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");
        checkNotNull(personHierarchyService, "personHierarchyService cannot be null");

        this.dsl = dsl;
        this.changeInitiativeDao = changeInitiativeDao;
        this.dataTypeDao = dataTypeDao;
        this.entityHierarchyDao = entityHierarchyDao;
        this.entityRootsSelectorFactory = entityRootsSelectorFactory;
        this.entityStatisticDao = entityStatisticDao;
        this.measurableDao = measurableDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.personHierarchyService = personHierarchyService;
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
                return Collections.emptyList();
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + kind);
        }
    }


    public int buildFor(EntityKind kind) {
        if (kind == PERSON) {
            int[] rc = personHierarchyService.build();
            return rc.length;
        } else {
            Table table = determineTableToRebuild(kind);
            return buildFor(table, kind);
        }
    }


    private int buildFor(Table table, EntityKind kind) {
        Collection<FlatNode<Long, Long>> flatNodes = fetchFlatNodes(table);
        List<EntityHierarchyItem> hierarchyItems = convertFlatNodesToHierarchyItems(kind, flatNodes);

        return entityHierarchyDao.replaceHierarchy(kind, hierarchyItems);
    }


    private List<FlatNode<Long, Long>> fetchFlatNodes(Table table) {
        Field<Long> idField = table.field("id", Long.class);
        Field<Long> parentIdField = table.field("parent_id", Long.class);

        checkNotNull(idField, "cannot find id column");
        checkNotNull(parentIdField, "cannot find parent_id column");

        return dsl.select(idField, parentIdField)
                .from(table)
                .fetch(r -> new FlatNode<>(
                        r.value1(),
                        Optional.ofNullable(r.value2()),
                        r.value1()));
    }


    private List<EntityHierarchyItem> convertFlatNodesToHierarchyItems(EntityKind kind, Collection<FlatNode<Long, Long>> flatNodes) {
        Forest<Long, Long> forest = HierarchyUtilities.toForest(flatNodes);
        Node<Long, Long> r = forest.getAllNodes().get(811L);
        Map<Long, Integer> idToLevel = HierarchyUtilities.assignDepths(forest);

        return forest.getAllNodes()
                .values()
                .stream()
                .flatMap(streamItemsForNode(kind, idToLevel))
                .collect(Collectors.toList());
    }


    private Function<Node<Long, Long>, Stream<? extends EntityHierarchyItem>> streamItemsForNode(EntityKind kind, Map<Long, Integer> idToLevel) {
        return node -> Stream.concat(
                streamAncestors(kind, idToLevel, node),
                streamSelf(kind, idToLevel, node));
    }


    private Stream<EntityHierarchyItem> streamSelf(EntityKind kind, Map<Long, Integer> idToLevel, Node<Long, Long> node) {
        Long nodeId = node.getId();
        Integer level = idToLevel.get(nodeId);
        ImmutableEntityHierarchyItem selfAsEntityHierarchyItem = ImmutableEntityHierarchyItem.builder()
                .id(nodeId)
                .parentId(nodeId)
                .level(level == null ? -1 : level)
                .kind(kind)
                .build();
        return Stream.of(selfAsEntityHierarchyItem);
    }


    private Stream<EntityHierarchyItem> streamAncestors(EntityKind kind, Map<Long, Integer> idToLevel, Node<Long, Long> node) {
        return HierarchyUtilities.parents(node)
            .stream()
            .map(p -> ImmutableEntityHierarchyItem.builder()
                    .id(node.getId())
                    .parentId(p.getId())
                    .level(idToLevel.get(p.getId()))
                    .kind(kind)
                    .build());
    }


    private Table determineTableToRebuild(EntityKind kind) {
        switch (kind) {
            case CHANGE_INITIATIVE:
                return Tables.CHANGE_INITIATIVE;
            case DATA_TYPE:
                return Tables.DATA_TYPE;
            case ENTITY_STATISTIC:
                return Tables.ENTITY_STATISTIC_DEFINITION;
            case MEASURABLE:
                return Tables.MEASURABLE;
            case ORG_UNIT:
                return Tables.ORGANISATIONAL_UNIT;
            default:
                throw new IllegalArgumentException("Cannot determine hierarchy table for kind: "+kind);
        }
    }

}
