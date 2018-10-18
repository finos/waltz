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

package com.khartec.waltz.service.orgunit;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.data.orgunit.search.OrganisationalUnitSearchDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.*;
import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static java.util.Collections.emptyList;


@Service
public class OrganisationalUnitService {

    private final OrganisationalUnitDao dao;
    private final OrganisationalUnitSearchDao organisationalUnitSearchDao;
    private final OrganisationalUnitIdSelectorFactory selectorFactory;


    @Autowired
    public OrganisationalUnitService(OrganisationalUnitDao dao,
                                     OrganisationalUnitSearchDao organisationalUnitSearchDao,
                                     OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        checkNotNull(dao, "dao must not be null");
        checkNotNull(organisationalUnitSearchDao, "organisationalUnitSearchDao must not be null");
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");

        this.dao = dao;
        this.organisationalUnitSearchDao = organisationalUnitSearchDao;
        this.selectorFactory = orgUnitIdSelectorFactory;
    }


    public List<OrganisationalUnit> findAll() {
        return dao.findAll();
    }


    public List<OrganisationalUnit> findRelatedByEntityRef(EntityReference ref) {
        return dao.findRelatedByEntityRef(ref);
    }


    public OrganisationalUnit getById(long id) {
        return dao.getById(id);
    }


    public Integer updateDescription(long id, String description) {
        return dao.updateDescription(id, description);
    }


    public List<OrganisationalUnit> findByIds(Long... ids) {
        return time("OUS.findByIds", () -> dao.findByIds(ids));
    }


    /**
     * Returns the entire hierarchy which includes the org unit specified.
     *
     * @param orgUnitId
     * @return
     */
    public Node<OrganisationalUnit, Long> loadHierarchy(long orgUnitId) {
        return time("OUS.loadHierarchy", () -> {
            Collection<OrganisationalUnit> allInvolvedUnits = getAllRelatedOrgUnits(orgUnitId);
            Node<OrganisationalUnit, Long> rootNode = buildHierarchyTree(orgUnitId, allInvolvedUnits);
            return findNode(orgUnitId, rootNode);
        });
    }


    private Collection<OrganisationalUnit> getAllRelatedOrgUnits(long orgUnitId) {
        IdSelectionOptions parentSelectorOptions = ImmutableIdSelectionOptions
                .builder()
                .entityReference(ImmutableEntityReference.builder()
                        .id(orgUnitId)
                        .kind(EntityKind.ORG_UNIT)
                        .build())
                .scope(HierarchyQueryScope.PARENTS)
                .build();

        IdSelectionOptions childSelectorOptions = ImmutableIdSelectionOptions
                .copyOf(parentSelectorOptions)
                .withScope(HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> parentSelector = selectorFactory.apply(parentSelectorOptions);
        Select<Record1<Long>> childSelector = selectorFactory.apply(childSelectorOptions);
        Select<Record1<Long>> relatedSelector = DSL.selectFrom(parentSelector.asTable()).union(childSelector);

        List<OrganisationalUnit> related = dao.findBySelector(relatedSelector);
        return SetUtilities.fromCollection(related);
    }


    private Node<OrganisationalUnit, Long> buildHierarchyTree(long orgUnitId, Collection<OrganisationalUnit> allInvolvedUnits) {
        List<FlatNode<OrganisationalUnit, Long>> allInvolvedAsFlatNodes = toFlatNodes(allInvolvedUnits);

        Forest<OrganisationalUnit, Long> unitForest = HierarchyUtilities.toForest(allInvolvedAsFlatNodes);

        Set<Node<OrganisationalUnit, Long>> rootNodes = unitForest.getRootNodes();
        checkFalse(rootNodes.isEmpty(), "Calculating tree for ouId: " + orgUnitId + " gave no root!?");
        checkTrue(rootNodes.size() == 1, "Calculating tree for ouId:" + orgUnitId + " gave more than one root!");
        return rootNodes.iterator().next();
    }


    private List<FlatNode<OrganisationalUnit, Long>> toFlatNodes(Collection<OrganisationalUnit> allInvolvedUnits) {
        return allInvolvedUnits.stream()
                .map(ou -> {
                    Long ouId = ou.id().get();
                    Optional<Long> pid = ou.parentId();
                    return new FlatNode<>(ouId, pid, ou);
                })
                .collect(Collectors.toList());
    }


    private Node<OrganisationalUnit, Long> findNode(long orgUnitId, Node<OrganisationalUnit, Long> startNode) {
        Node<OrganisationalUnit, Long> ptr = startNode;
        boolean notFound = true;
        while (notFound) {
            if (ptr.getId().equals(orgUnitId)) {
                break;
            } else {
                checkFalse(ptr.getChildren().size() > 1, "Should have found ouId: " + orgUnitId + " before tree fan-out");
                checkFalse(ptr.getChildren().isEmpty(), "Could not find ouId: " + orgUnitId + " in it's own tree ?!");
                ptr = ptr.getChildren().iterator().next();
            }
        }
        return ptr;
    }


    public List<OrganisationalUnit> search(String query) {
        return search(query, EntitySearchOptions.mkForEntity(EntityKind.ORG_UNIT));
    }


    public List<OrganisationalUnit> search(String query, EntitySearchOptions options) {
        if (isEmpty(query)) return emptyList();
        return organisationalUnitSearchDao.search(query, options);
    }


    public List<LeveledEntityReference> findImmediateHierarchy(long id) {
        return dao.findImmediateHierarchy(id);
    }


    public List<OrganisationalUnit> findDescendants(long id) {
        return dao.findDescendants(id);
    }

    public OrganisationalUnit getByAppId(long id) {
        return dao.getByAppId(id);
    }

}
