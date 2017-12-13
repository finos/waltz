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

package com.khartec.waltz.service.application;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.application.search.ApplicationSearchDao;
import com.khartec.waltz.data.entity_alias.EntityAliasDao;
import com.khartec.waltz.data.entity_tag.EntityTagDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.AppRegistrationRequest;
import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.AssetCodeRelationshipKind;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.tally.Tally;
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

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.Collections.emptyMap;


@Service
public class ApplicationService {


    private final ApplicationDao applicationDao;
    private final EntityTagDao entityTagDao;
    private final EntityAliasDao entityAliasDao;
    private final ApplicationSearchDao appSearchDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;


    @Autowired
    public ApplicationService(ApplicationDao appDao,
                              EntityTagDao entityTagDao,
                              EntityAliasDao entityAliasDao,
                              ApplicationSearchDao appSearchDao,
                              ApplicationIdSelectorFactory appIdSelectorFactory) {
        checkNotNull(appDao, "appDao must not be null");
        checkNotNull(entityTagDao, "entityTagDao must not be null");
        checkNotNull(entityAliasDao, "entityAliasDao must not be null");
        checkNotNull(appSearchDao, "appSearchDao must not be null");
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");

        this.applicationDao = appDao;
        this.entityTagDao = entityTagDao;
        this.entityAliasDao = entityAliasDao;
        this.appSearchDao = appSearchDao;
        this.appIdSelectorFactory = appIdSelectorFactory;
    }


    public Application getById(long id) {
        return applicationDao.getById(id);
    }


    public List<Application> findAll() {
        return applicationDao.getAll();
    }


    public List<Tally<Long>> countByOrganisationalUnit() {
        return applicationDao.countByOrganisationalUnit();
    }


    public List<Application> search(String query) {
        return search(query, EntitySearchOptions.mkForEntity(EntityKind.APPLICATION));
    }


    public List<Application> search(String query, EntitySearchOptions options) {
        return appSearchDao.search(query, options);
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

            entityTagDao.updateTags(entityReference, request.tags(), username);
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

            Map<AssetCodeRelationshipKind, List<Application>> grouped = related.stream()
                    .filter(relatedApp -> relatedApp != app)  // can do simple ref check here
                    .collect(Collectors.groupingBy(classifier));

            return grouped;
        } else {
            return emptyMap();
        }

    }


    public List<Application> findByAssetCode(String assetCode) {
        return applicationDao.findByAssetCode(assetCode);
    }

}

