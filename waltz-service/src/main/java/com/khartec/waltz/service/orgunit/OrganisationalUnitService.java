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

import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.data.orgunit.search.OrganisationalUnitSearchDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LeveledEntityReference;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static java.util.Collections.emptyList;


@Service
public class OrganisationalUnitService {

    private final OrganisationalUnitDao dao;
    private final OrganisationalUnitSearchDao organisationalUnitSearchDao;


    @Autowired
    public OrganisationalUnitService(OrganisationalUnitDao dao,
                                     OrganisationalUnitSearchDao organisationalUnitSearchDao) {
        checkNotNull(dao, "dao must not be null");
        checkNotNull(organisationalUnitSearchDao, "organisationalUnitSearchDao must not be null");

        this.dao = dao;
        this.organisationalUnitSearchDao = organisationalUnitSearchDao;
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
        return time("OUS.findActiveByFlowIds", () -> dao.findByIds(ids));
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
