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

package org.finos.waltz.service.application;

import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.application.search.ApplicationSearchDao;
import org.finos.waltz.data.entity_alias.EntityAliasDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.application.AppRegistrationRequest;
import org.finos.waltz.model.application.AppRegistrationResponse;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.AssetCodeRelationshipKind;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.service.tag.TagService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.isEmpty;


@Service
public class ApplicationService {


    private final ApplicationDao applicationDao;
    private final TagService tagService;
    private final EntityAliasDao entityAliasDao;
    private final ApplicationSearchDao appSearchDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ApplicationService(ApplicationDao appDao,
                              TagService tagService,
                              EntityAliasDao entityAliasDao,
                              ApplicationSearchDao appSearchDao) {
        checkNotNull(appDao, "appDao must not be null");
        checkNotNull(tagService, "tagService must not be null");
        checkNotNull(entityAliasDao, "entityAliasDao must not be null");
        checkNotNull(appSearchDao, "appSearchDao must not be null");

        this.applicationDao = appDao;
        this.tagService = tagService;
        this.entityAliasDao = entityAliasDao;
        this.appSearchDao = appSearchDao;
    }


    public Application getById(long id) {
        return applicationDao.getById(id);
    }


    public List<Application> findAll() {
        return applicationDao.findAll();
    }


    public List<Tally<Long>> countByOrganisationalUnit() {
        return applicationDao.countByOrganisationalUnit();
    }


    public List<Application> search(String query) {
        if (isEmpty(query)) return emptyList();
        return search(EntitySearchOptions.mkForEntity(EntityKind.APPLICATION, query));
    }


    public List<Application> search(EntitySearchOptions options) {
        return appSearchDao.search(options);
    }


    public List<Application> findByIds(Collection<Long> ids) {
        return applicationDao.findByIds(ids);
    }


    public List<Application> findByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = appIdSelectorFactory.apply(options);
        return applicationDao.findByAppIdSelector(selector);
    }


    public AppRegistrationResponse registerApp(AppRegistrationRequest request, String username) {
        checkNotEmpty(request.name(), "Cannot register app with no name");
        AppRegistrationResponse response = applicationDao.registerApp(request);

        if (response.registered()) {
            EntityReference entityReference = ImmutableEntityReference.builder()
                    .id(response.id().get())
                    .kind(EntityKind.APPLICATION)
                    .build();

            entityAliasDao.updateAliases(entityReference,
                    request.aliases());

            tagService.updateTags(entityReference, request.tags(), username);
        }

        return response;
    }


    public Integer update(Application application) {
        return applicationDao.update(application);
    }


    public Map<AssetCodeRelationshipKind, List<Application>> findRelated(long appId) {

        List<Application> related = applicationDao.findRelatedByApplicationId(appId);

        Optional<Application> foundApp = related.stream()
                .filter(app -> app.id().equals(Optional.of(appId)))
                .findFirst();

        if (foundApp.isPresent()) {
            Application app = foundApp.get();

            Function<Application, AssetCodeRelationshipKind> classifier = relatedApp -> {

                boolean sameParent = relatedApp.parentAssetCode().equals(app.parentAssetCode());
                boolean sameCode = relatedApp.assetCode().equals(app.assetCode());
                boolean isParent = relatedApp.assetCode().equals(app.parentAssetCode());
                boolean isChild = relatedApp.parentAssetCode().equals(app.assetCode());

                if (sameCode) {
                    return AssetCodeRelationshipKind.SHARING;
                } else if (isParent) {
                    return AssetCodeRelationshipKind.PARENT;
                } else if (isChild) {
                    return AssetCodeRelationshipKind.CHILD;
                } else if (sameParent && app.parentAssetCode().isPresent()) {
                    return AssetCodeRelationshipKind.SIBLING;
                } else {
                    return AssetCodeRelationshipKind.NONE;
                }
            };

            return related
                    .stream()
                    .filter(relatedApp -> relatedApp != app)  // can do simple ref check here
                    .collect(Collectors.groupingBy(classifier));
        } else {
            return emptyMap();
        }

    }


    public List<Application> findByAssetCode(ExternalIdValue assetCode) {
        return applicationDao.findByAssetCode(assetCode);
    }

}

