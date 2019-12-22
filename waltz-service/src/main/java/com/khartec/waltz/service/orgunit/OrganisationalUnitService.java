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
        if (isEmpty(query)) return emptyList();
        return search(EntitySearchOptions.mkForEntity(EntityKind.ORG_UNIT, query));
    }


    public List<OrganisationalUnit> search(EntitySearchOptions options) {
        return organisationalUnitSearchDao.search(options);
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
