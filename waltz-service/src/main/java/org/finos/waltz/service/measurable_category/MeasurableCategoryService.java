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

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.exceptions.NotAuthorizedException;
import org.finos.waltz.model.measurable.ImmutableMeasurable;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.ImmutableMeasurableCategoryView;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_category.MeasurableCategoryView;
import org.finos.waltz.model.settings.Setting;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.settings.SettingsService;
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
    private final MeasurableService measurableService;
    private final SettingsService settingsService;

    @Autowired
    public MeasurableCategoryService(MeasurableCategoryDao measurableCategoryDao,
                                     UserRoleService userRoleService,
                                     ChangeLogService changeLogService,
                                     MeasurableService measurableService,
                                     SettingsService settingsService) {
        this.measurableCategoryDao = measurableCategoryDao;
        this.userRoleService = userRoleService;
        this.changeLogService = changeLogService;
        this.measurableService = measurableService;
        this.settingsService = settingsService;    }


    public Collection<MeasurableCategory> findAll() {
        Collection<MeasurableCategory> measurableCategoryCollection = measurableCategoryDao.findAll();
        return removeDeprecatedMeasurableCategory(measurableCategoryCollection);
    }

    public Collection<MeasurableCategory> removeDeprecatedMeasurableCategory(Collection<MeasurableCategory> measurableCategoryCollection){
        Setting setting = settingsService.getByName("DEPRECATED_MEASURABLE_CATEGORY");
        measurableCategoryCollection.removeIf(measurableCategory -> {
            if(setting != null && setting.value().isPresent()) {
                List<String> list = StringUtilities.tokenise(setting.value().get(),",");
                if(measurableCategory.externalId().isPresent()){
                    return list.contains(measurableCategory.externalId().get());
                }else{
                    return false;
                }
            }else{
                return false;
            }
        });
        return measurableCategoryCollection;
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
        removeDeprecatedMeasurableCategory(allCategories);

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
        boolean isNew = ! measurableCategory.id().isPresent();
        Long mcId = measurableCategoryDao.save(measurableCategory, username);

        changeLogService.write(ImmutableChangeLog
                .builder()
                .operation(isNew ? Operation.ADD : Operation.UPDATE)
                .parentReference(EntityReference.mkRef(EntityKind.MEASURABLE_CATEGORY, mcId))
                .message(isNew ? "Measurable category created, root node added" : "Measurable category updated")
                .userId(username)
                .build());

        if (isNew) {
            Measurable startingMeasurable = ImmutableMeasurable
                    .builder()
                    .name("Root - " + measurableCategory.name())
                    .description("Autogenerated root node, feel free to rename/remove")
                    .concrete(false)
                    .externalId(measurableCategory.externalId().map(extId -> String.format("%s-001", extId)))
                    .categoryId(mcId)
                    .lastUpdatedBy(username)
                    .build();
            measurableService.create(
                    startingMeasurable,
                    username);
        }
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
