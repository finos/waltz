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

package com.khartec.waltz.service.app_view;

import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.appview.AppView;
import com.khartec.waltz.model.appview.ImmutableAppView;
import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.service.entity_alias.EntityAliasService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.entity_tag.EntityTagService;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;


@Service
public class AppViewService {

    private final ApplicationService applicationService;
    private final EntityTagService entityTagService;
    private final ComplexityRatingService complexityRatingService;
    private final EntityAliasService entityAliasService;
    private final OrganisationalUnitService organisationalUnitService;
    private final DBExecutorPoolInterface dbExecutorPool;


    @Autowired
    public AppViewService(EntityTagService entityTagService,
                          ApplicationService applicationService,
                          ComplexityRatingService complexityRatingService,
                          EntityAliasService entityAliasService,
                          DBExecutorPoolInterface dbExecutorPool,
                          OrganisationalUnitService organisationalUnitService) {

        checkNotNull(entityTagService, "appTagService cannot be null");
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(complexityRatingService, "complexityRatingService cannot be null");
        checkNotNull(entityAliasService, "entityAliasService cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService must not be null");

        this.entityTagService = entityTagService;
        this.applicationService = applicationService;
        this.complexityRatingService = complexityRatingService;
        this.entityAliasService = entityAliasService;
        this.dbExecutorPool = dbExecutorPool;
        this.organisationalUnitService = organisationalUnitService;
    }


    public AppView getAppView(long id) {
        EntityReference ref = ImmutableEntityReference
                .builder()
                .kind(EntityKind.APPLICATION)
                .id(id)
                .build();

        Future<Application> application = dbExecutorPool.submit(() ->
                applicationService.getById(id));

        Future<OrganisationalUnit> orgUnit = dbExecutorPool.submit(() ->
                organisationalUnitService.getByAppId(id));

        Future<List<String>> tags = dbExecutorPool.submit(() ->
                entityTagService.findTagsForEntityReference(mkRef(EntityKind.APPLICATION, id)));

        Future<List<String>> aliases = dbExecutorPool.submit(() ->
                entityAliasService.findAliasesForEntityReference(ref));

        Future<ComplexityRating> complexity = dbExecutorPool.submit(() ->
                complexityRatingService.getForApp(id));

        return Unchecked.supplier(() -> ImmutableAppView.builder()
                    .app(application.get())
                    .organisationalUnit(orgUnit.get())
                    .tags(tags.get())
                    .aliases(aliases.get())
                    .complexity(complexity.get())
                    .build()).get();
    }

}
