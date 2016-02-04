/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.orgunit;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.orgunit.ImmutableOrganisationalUnitHierarchy;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnitHierarchy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.ListUtilities.drop;


@Service
public class OrganisationalUnitService {

    private final OrganisationalUnitDao dao;


    @Autowired
    public OrganisationalUnitService(OrganisationalUnitDao dao) {
        this.dao = dao;
        checkNotNull(dao, "dao must not be null");
    }


    public List<OrganisationalUnit> findAll() {
        return dao.findAll();
    }


    public OrganisationalUnit getById(long id) {
        return dao.getById(id);
    }


    public Integer updateDescription(long id, String description) {
        return dao.updateDescription(id, description);
    }


    public List<OrganisationalUnit> findByIds(Long... ids) {
        return dao.findByIds(ids);
    }


    /**
     * Returns the entire hierarchy which includes the org unit specified.
     *
     * @param orgUnitId
     * @return
     */
    public Node<OrganisationalUnit, Long> loadHierarchy(long orgUnitId) {
        List<OrganisationalUnit> allInvolvedUnits = getAllRelatedOrgUnits(orgUnitId);
        Node<OrganisationalUnit, Long> rootNode = buildHierarchyTree(orgUnitId, allInvolvedUnits);
        return findNode(orgUnitId, rootNode);
    }


    private List<OrganisationalUnit> getAllRelatedOrgUnits(long orgUnitId) {
        List<OrganisationalUnit> parents = dao.findAncestors(orgUnitId);
        List<OrganisationalUnit> children = dao.findDescendants(orgUnitId);
        return ListUtilities.concat(drop(parents, 1), children);
    }


    private Node<OrganisationalUnit, Long> buildHierarchyTree(long orgUnitId, List<OrganisationalUnit> allInvolvedUnits) {
        List<FlatNode<OrganisationalUnit, Long>> allInvolvedAsFlatNodes = toFlatNodes(allInvolvedUnits);

        Forest<OrganisationalUnit, Long> unitForest = HierarchyUtilities.toForest(allInvolvedAsFlatNodes);

        Set<Node<OrganisationalUnit, Long>> rootNodes = unitForest.getRootNodes();
        checkFalse(rootNodes.isEmpty(), "Calculating tree for ouId: " + orgUnitId + " gave no root!?");
        checkTrue(rootNodes.size() == 1, "Calculating tree for ouId:" + orgUnitId + " gave more than one root!");
        return rootNodes.iterator().next();
    }


    private List<FlatNode<OrganisationalUnit, Long>> toFlatNodes(List<OrganisationalUnit> allInvolvedUnits) {
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
        if (StringUtilities.isEmpty(query)) return Collections.emptyList();
        return dao.search(query);
    }

    public OrganisationalUnitHierarchy getHierarchyById(long id) {
        return ImmutableOrganisationalUnitHierarchy.builder()
                .children(drop(dao.findDescendants(id), 1))
                .parents(drop(dao.findAncestors(id), 1))
                .unit(dao.getById(id))
                .build();
    }
}
