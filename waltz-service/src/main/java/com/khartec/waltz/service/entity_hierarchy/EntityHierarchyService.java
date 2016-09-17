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
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.data.process.ProcessDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_hierarchy.EntityHierarchyItem;
import com.khartec.waltz.model.entity_hierarchy.ImmutableEntityHierarchyItem;
import com.khartec.waltz.model.tally.ImmutableStringTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.CAPABILITY;
import static com.khartec.waltz.model.EntityKind.PERSON;

@Service
public class EntityHierarchyService {

    private final DSLContext dsl;
    private final CapabilityService capabilityService;
    private final ChangeInitiativeDao changeInitiativeDao;
    private final DataTypeDao dataTypeDao;
    private final EntityHierarchyDao entityHierarchyDao;
    private final EntityRootsSelectorFactory entityRootsSelectorFactory;
    private final EntityStatisticDao entityStatisticDao;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final ProcessDao processDao;
    private final PersonHierarchyService personHierarchyService;

    @Autowired
    public EntityHierarchyService(DSLContext dsl,
                                  CapabilityService capabilityService,
                                  ChangeInitiativeDao changeInitiativeDao,
                                  DataTypeDao dataTypeDao,
                                  EntityHierarchyDao entityHierarchyDao,
                                  EntityRootsSelectorFactory entityRootsSelectorFactory,
                                  EntityStatisticDao entityStatisticDao,
                                  OrganisationalUnitDao organisationalUnitDao,
                                  PersonHierarchyService personHierarchyService,
                                  ProcessDao processDao) {

        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(capabilityService, "capabilityService cannot be null");
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(entityHierarchyDao, "entityHierarchyDao cannot be null");
        checkNotNull(entityRootsSelectorFactory, "entityRootsSelectorFactory cannot be null");
        checkNotNull(entityStatisticDao, "entityStatisticDao cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");
        checkNotNull(personHierarchyService, "personHierarchyService cannot be null");
        checkNotNull(processDao, "processDao cannot be null");

        this.dsl = dsl;
        this.capabilityService = capabilityService;
        this.changeInitiativeDao = changeInitiativeDao;
        this.dataTypeDao = dataTypeDao;
        this.entityHierarchyDao = entityHierarchyDao;
        this.entityRootsSelectorFactory = entityRootsSelectorFactory;
        this.entityStatisticDao = entityStatisticDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.personHierarchyService = personHierarchyService;
        this.processDao = processDao;
    }


    public List<Tally<String>> tallyByKind() {
        return ListUtilities.append(
                entityHierarchyDao.tallyByKind(),
                ImmutableStringTally.builder()
                        .id(EntityKind.PERSON.name())
                        .count(personHierarchyService.count())
                        .build());
    }


    public List<Tally<String>> getRootTallies() {
        return ListUtilities.append(
                entityHierarchyDao.getRootTallies(),
                ImmutableStringTally.builder()
                        .id(EntityKind.PERSON.name())
                        .count(personHierarchyService.countRoots())
                        .build());
    }


    public List<EntityReference> getRoots(EntityKind kind) {
        Select<Record1<Long>> selector = entityRootsSelectorFactory.apply(kind);

        switch (kind) {
            case CAPABILITY:
                return capabilityService.findByIdSelectorAsEntityReference(kind);
            case CHANGE_INITIATIVE:
                return changeInitiativeDao.findByIdSelectorAsEntityReference(selector);
            case DATA_TYPE:
                return dataTypeDao.findByIdSelectorAsEntityReference(selector);
            case ENTITY_STATISTIC:
                return entityStatisticDao.findByIdSelectorAsEntityReference(selector);
            case ORG_UNIT:
                return organisationalUnitDao.findByIdSelectorAsEntityReference(selector);
            case PROCESS:
                return processDao.findByIdSelectorAsEntityReference(selector);
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

        //TODO: remove this once all code using the entity_hierarchy service and related fixes
        if (kind == CAPABILITY) {
            capabilityService.rebuildHierarchy();
        }

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
        return Stream.of(ImmutableEntityHierarchyItem.builder()
                .id(node.getId())
                .parentId(node.getId())
                .level(idToLevel.get(node.getId()))
                .kind(kind)
                .build());
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
            case CAPABILITY:
                return Tables.CAPABILITY;
            case CHANGE_INITIATIVE:
                return Tables.CHANGE_INITIATIVE;
            case DATA_TYPE:
                return Tables.DATA_TYPE;
            case ENTITY_STATISTIC:
                return Tables.ENTITY_STATISTIC_DEFINITION;
            case ORG_UNIT:
                return Tables.ORGANISATIONAL_UNIT;
            case PROCESS:
                return Tables.PROCESS;
            default:
                throw new IllegalArgumentException("Cannot determine hierarchy table for kind: "+kind);
        }
    }

}
