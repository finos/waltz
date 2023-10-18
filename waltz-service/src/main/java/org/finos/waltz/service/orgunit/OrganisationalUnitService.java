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

package org.finos.waltz.service.orgunit;

import org.finos.waltz.data.orgunit.OrganisationalUnitDao;
import org.finos.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import org.finos.waltz.data.orgunit.search.OrganisationalUnitSearchDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


@Service
public class OrganisationalUnitService {

    private final OrganisationalUnitDao dao;
    private final OrganisationalUnitSearchDao organisationalUnitSearchDao;
    private final OrganisationalUnitIdSelectorFactory ouSelectorFactory = new OrganisationalUnitIdSelectorFactory();


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
        return dao.findByIds(ids);
    }


    public List<OrganisationalUnit> search(String query) {
        if (isEmpty(query)) return emptyList();
        return search(EntitySearchOptions.mkForEntity(EntityKind.ORG_UNIT, query));
    }


    public List<OrganisationalUnit> search(EntitySearchOptions options) {
        return organisationalUnitSearchDao.search(options);
    }



    public List<OrganisationalUnit> findDescendants(long id) {
        Select<Record1<Long>> ouSelector = ouSelectorFactory.apply(mkOpts(
                mkRef(EntityKind.ORG_UNIT, id),
                HierarchyQueryScope.PARENTS));
        return dao.findBySelector(ouSelector);
    }


}
