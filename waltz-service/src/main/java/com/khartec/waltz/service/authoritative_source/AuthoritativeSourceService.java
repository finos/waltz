/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.Record1;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.*;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.common.hierarchy.HierarchyUtilities.parents;


@Service
public class AuthoritativeSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceService.class);

    private final AuthoritativeSourceDao authoritativeSourceDao;
    private final AuthSourceRatingCalculator ratingCalculator;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final OrganisationalUnitService organisationalUnitService;


    @Autowired
    public AuthoritativeSourceService(AuthoritativeSourceDao authoritativeSourceDao,
                                      AuthSourceRatingCalculator ratingCalculator,
                                      DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                      OrganisationalUnitService organisationalUnitService) {
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao must not be null");
        checkNotNull(ratingCalculator, "ratingCalculator cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService cannot be null");

        this.authoritativeSourceDao = authoritativeSourceDao;
        this.ratingCalculator = ratingCalculator;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.organisationalUnitService = organisationalUnitService;
    }


    public List<AuthoritativeSource> findByEntityKind(EntityKind kind) {
        return authoritativeSourceDao.findByEntityKind(kind);
    }


    public AuthoritativeSource getById(long id) {
        return authoritativeSourceDao.getById(id);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        return authoritativeSourceDao.findByEntityReference(ref);
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        return authoritativeSourceDao.findByApplicationId(applicationId);
    }


    public int update(long id, AuthoritativenessRating rating) {
        int updateCount = authoritativeSourceDao.update(id, rating);
        AuthoritativeSource updatedAuthSource = getById(id);
        ratingCalculator.update(updatedAuthSource.dataType(), updatedAuthSource.parentReference());
        return updateCount;
    }


    public int insert(EntityReference parentRef, String dataTypeCode, Long appId, AuthoritativenessRating rating) {
        int insertedCount = authoritativeSourceDao.insert(parentRef, dataTypeCode, appId, rating);
        ratingCalculator.update(dataTypeCode, parentRef);
        return insertedCount;
    }


    public int remove(long id) {
        AuthoritativeSource authSourceToDelete = getById(id);
        int deletedCount = authoritativeSourceDao.remove(id);
        ratingCalculator.update(authSourceToDelete.dataType(), authSourceToDelete.parentReference());
        return deletedCount;
    }


    public List<AuthoritativeSource> findAll() {
        return authoritativeSourceDao.findAll();
    }


    public boolean recalculateAllFlowRatings() {
        findAll()
                .forEach(authSource -> ratingCalculator.update(
                        authSource.dataType(),
                        authSource.parentReference()));

        return true;
    }


    public List<AuthoritativeSource> findByDataTypeIdSelector(IdSelectionOptions idSelectionOptions) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(idSelectionOptions);
        return authoritativeSourceDao.findByDataTypeIdSelector(selector);
    }


    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(
            IdSelectionOptions options) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(options);
        return authoritativeSourceDao.calculateConsumersForDataTypeIdSelector(selector);
    }


    /** (ouId) -> dataType -> appId -> rating
     * @param orgUnitId */
    public Map<String, Map<Long, AuthoritativeSource>> determineAuthSourcesForOrgUnit(long orgUnitId) {
        Node<OrganisationalUnit, Long> startNode = organisationalUnitService.loadHierarchy(orgUnitId);
        List<AuthoritativeSource> allAuthSources = findByEntityKind(EntityKind.ORG_UNIT);
        Map<Long, Collection<AuthoritativeSource>> authSourcesByOrgId = groupBy(as -> as.parentReference().id(), allAuthSources);
        return determineCumulativeRules(startNode, authSourcesByOrgId);
    }


    /** (ouNode, ouId -> [authSource]) -> dataType -> appId -> rating */
    private Map<String, Map<Long, AuthoritativeSource>> determineCumulativeRules(Node<OrganisationalUnit, Long> node,
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



}
