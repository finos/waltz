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

package org.finos.waltz.service.measurable_category;

import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;

@Service
public class MeasurableCategoryService {

    private final MeasurableCategoryDao measurableCategoryDao;
    private final UserRoleService userRoleService;


    @Autowired
    public MeasurableCategoryService(MeasurableCategoryDao measurableCategoryDao, UserRoleService userRoleService) {
        this.measurableCategoryDao = measurableCategoryDao;
        this.userRoleService = userRoleService;
    }


    public Collection<MeasurableCategory> findAll() {
        return measurableCategoryDao.findAll();
    }


    public MeasurableCategory getById(long id) {
        return measurableCategoryDao.getById(id);
    }


    public Collection<MeasurableCategory> findCategoriesByDirectOrgUnit(long id) {
        return measurableCategoryDao.findCategoriesByDirectOrgUnit(id);
    }


    public Set<MeasurableCategory> findByExternalId(String externalId) {
        return measurableCategoryDao.findByExternalId(externalId);
    }


    public Set<MeasurableCategory> findPopulatedCategoriesForRef(EntityReference ref) {
        return measurableCategoryDao
                .findPopulatedCategoriesForRef(ref);
    }


    public boolean save(MeasurableCategory measurableCategory, String username) {
        ensureUserHasPermission(username);
        return measurableCategoryDao.save(measurableCategory, username);
    }


    private void ensureUserHasPermission(String username) {
        if (! userRoleService.hasAnyRole(
                username,
                SystemRole.ADMIN,
                SystemRole.TAXONOMY_EDITOR)) {
            throw new NotAuthorizedException(format(
                    "User: %s does not have permission to edit measurable categories",
                    username));
        }
    }
}
