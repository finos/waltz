package com.khartec.waltz.service.entity_hierarchy;

import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.entity_hierarchy.EntityHierarchyDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_hierarchy.EntityHierarchyItem;
import com.khartec.waltz.model.entity_hierarchy.ImmutableEntityHierarchyItem;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.service.capability.CapabilityService;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityHierarchyService {

    private final DSLContext dsl;
    private final EntityHierarchyDao entityHierarchyDao;
    private final CapabilityService capabilityService;

    @Autowired
    public EntityHierarchyService(DSLContext dsl, EntityHierarchyDao entityHierarchyDao, CapabilityService capabilityService ) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(entityHierarchyDao, "entityHierarchyDao cannot be null");
        checkNotNull(capabilityService, "capabilityService cannot be null");

        this.dsl = dsl;
        this.entityHierarchyDao = entityHierarchyDao;
        this.capabilityService = capabilityService;
    }


    public List<Tally<String>> tallyByKind() {
        return entityHierarchyDao.tallyByKind();
    }


    public int buildFor(EntityKind kind) {
        Table table = determineTableToRebuild(kind);
        return buildFor(table, kind);
    }


    private int buildFor(Table table, EntityKind kind) {
        Collection<FlatNode<Long, Long>> flatNodes = fetchFlatNodes(table);
        List<EntityHierarchyItem> hierarchyItems = convertFlatNodesToHierarchyItems(kind, flatNodes);

        //TODO: remove this once all code using the entity_hierarchy service and related fixes
        if (kind == EntityKind.CAPABILITY) {
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
                throw new IllegalArgumentException("Cannot deteremine hierarchy table for kind: "+kind);
        }
    }


}
