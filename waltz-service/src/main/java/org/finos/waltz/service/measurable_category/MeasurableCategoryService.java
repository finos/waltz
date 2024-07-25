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
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.measurable_category.ImmutableMeasurableCategoryView;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_category.MeasurableCategoryView;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class MeasurableCategoryService {

    private final MeasurableCategoryDao measurableCategoryDao;
    private final UserRoleService userRoleService;
    private final ChangeLogService changeLogService;

    @Autowired
    public MeasurableCategoryService(MeasurableCategoryDao measurableCategoryDao,
                                     UserRoleService userRoleService,
                                     ChangeLogService changeLogService) {
        this.measurableCategoryDao = measurableCategoryDao;
        this.userRoleService = userRoleService;
        this.changeLogService = changeLogService;
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


    public List<MeasurableCategoryView> findPopulatedCategoriesForRef(EntityReference ref) {
        Collection<MeasurableCategory> allCategories = measurableCategoryDao.findAll();

        Map<Long, Long> ratingCountsByCategoryId = measurableCategoryDao
                .findRatingCountsByCategoryId(ref);

        return allCategories
                .stream()
                .map(category -> ImmutableMeasurableCategoryView
                        .builder()
                        .category(category)
                        .ratingCount(ratingCountsByCategoryId.getOrDefault(category.id().get(), 0L))
                        .build())
                .sorted(Comparator.comparing(d -> d.category().name()))
                .collect(Collectors.toList());
    }


    public Long save(MeasurableCategory measurableCategory, String username) {
        ensureUserHasPermission(username);
        Long mcId = measurableCategoryDao.save(measurableCategory, username);
        boolean isNew = ! measurableCategory.id().isPresent();

        changeLogService.write(ImmutableChangeLog
                .builder()
                .operation(isNew ? Operation.ADD : Operation.UPDATE)
                .parentReference(EntityReference.mkRef(EntityKind.MEASURABLE_CATEGORY, mcId))
                .message(isNew ? "Measureable category created" : "Measurable category updated")
                .userId(username)
                .build());

        return mcId;
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
