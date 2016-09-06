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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.*;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.common.hierarchy.HierarchyUtilities.parents;


@Deprecated
@Service
public class AuthoritativeSourceCalculator {

    private final OrganisationalUnitService organisationalUnitService;
    private final AuthoritativeSourceService authoritativeSourceService;


    @Autowired
    public AuthoritativeSourceCalculator(OrganisationalUnitService organisationalUnitService, AuthoritativeSourceService authoritativeSourceService) {
        this.organisationalUnitService = organisationalUnitService;
        this.authoritativeSourceService = authoritativeSourceService;
    }


    /**
     * Calculates all the auth sources for a organisation tree centered around the given orgUnitId.
     *
     * <br>
     *
     * Gives back a structure which is like:
     *
     * <pre>
     *    [ orgUnitId -> dataType -> appId -> authSource ]
     * </pre>
     *
     * Typically you will just want to get the rating from the authSources, however
     * you may want more details for display purposes or similar.
     *
     * @param orgUnitId
     * @return
     */
    public Map<Long, Map<String, Map<Long, AuthoritativeSource>>> calculateAuthSourcesForOrgUnitTree(long orgUnitId) {
        Node<OrganisationalUnit, Long> orgUnitNode = organisationalUnitService.loadHierarchy(orgUnitId);
        return calculateAuthSourcesForOrgUnitTree(orgUnitNode);
    }


    public Map<String, Map<Long, AuthoritativeSource>> calculateAuthSourcesForOrgUnit(Node<OrganisationalUnit, Long> startNode) {
        List<AuthoritativeSource> allAuthSources = authoritativeSourceService.findByEntityKind(EntityKind.ORG_UNIT);
        Map<Long, Collection<AuthoritativeSource>> authSourcesByOrgId = groupBy(as -> as.parentReference().id(), allAuthSources);
        return calculateCumulativeRules(startNode, authSourcesByOrgId);
    }


    public Map<Long, Map<String, Map<Long, AuthoritativeSource>>> calculateAuthSourcesForOrgUnitTree(Node<OrganisationalUnit, Long> startNode) {
        List<AuthoritativeSource> allAuthSources = authoritativeSourceService.findByEntityKind(EntityKind.ORG_UNIT);
        Map<Long, Collection<AuthoritativeSource>> authSourcesByOrgId = groupBy(as -> as.parentReference().id(), allAuthSources);

        Map<Long, Map<String, Map<Long, AuthoritativeSource>>> rulesByOrgUnitId = newHashMap();
        traverse(startNode, rulesByOrgUnitId, authSourcesByOrgId);

        return rulesByOrgUnitId;
    }


    private void traverse(
            Node<OrganisationalUnit, Long> node,
            Map<Long, Map<String, Map<Long, AuthoritativeSource>>> accumulator,
            Map<Long, Collection<AuthoritativeSource>> authSourcesByOrgId) {
        Map<String, Map<Long, AuthoritativeSource>> cumulativeRules = calculateCumulativeRules(node, authSourcesByOrgId);
        accumulator.put(node.getId(), cumulativeRules);
        for (Node<OrganisationalUnit, Long> child : node.getChildren()) {
            traverse(child, accumulator, authSourcesByOrgId);
        }
    }

    /** (ouNode, ouId -> [authSource]) -> dataType -> appId -> rating */
    private Map<String, Map<Long, AuthoritativeSource>> calculateCumulativeRules(Node<OrganisationalUnit, Long> node,
                                                                                        Map<Long, Collection<AuthoritativeSource>> authSourcesByOrgId) {

        // get orgIds in order from root (reverse of our parents + us)
        List<Long> ids = append(reverse(map(parents(node), x -> x.getId())), node.getId());


        Map<String, Map<Long, AuthoritativeSource>> cumulativeRules = newHashMap();

        for (Long unitId : ids) {
            Collection<AuthoritativeSource> authSources = authSourcesByOrgId.getOrDefault(unitId, newArrayList());
            Map<String, Collection<AuthoritativeSource>> byType = groupBy(as -> as.dataType(), authSources);
            for (String type: byType.keySet()) {
                Map<Long, AuthoritativeSource> appRatingMap = cumulativeRules.getOrDefault(type, newHashMap());
                byType.get(type).forEach(as -> appRatingMap.put(as.applicationReference().id(), as));
                cumulativeRules.put(type, appRatingMap);
            }
        }

        return cumulativeRules;

    }


    public static void prettyPrint(Map<String, Map<Long, AuthoritativeSource>> cumulativeRules) {
        cumulativeRules.entrySet()
                .stream()
                .forEach(e -> {
                    System.out.println(e.getKey() + " ==> ");
                    e.getValue()
                            .values()
                            .forEach(v -> System.out.println("\t"
                                            + v.applicationReference().id()
                                            + " - " + v.applicationReference().name()
                                            + " ( " + v.parentReference().id() + ")"
                            ));
                });
    }

}
