/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.app_view;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.appview.AppView;
import com.khartec.waltz.model.appview.ImmutableAppView;
import com.khartec.waltz.model.bookmark.Bookmark;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.entity_alias.EntityAliasService;
import com.khartec.waltz.service.entity_statistic.EntityStatisticService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.tags.AppTagService;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.DB_EXECUTOR_POOL;


@Service
public class AppViewService {

    private final AppCapabilityService appCapabilityDao;
    private final ApplicationService appService;
    private final AssetCostService assetCostService;
    private final BookmarkService bookmarkService;
    private final CapabilityService capabilityService;
    private final EntityAliasService entityAliasService;
    private final EntityStatisticService entityStatisticService;
    private final OrganisationalUnitService organisationalUnitService;
    private final AppTagService appTagService;


    @Autowired
    public AppViewService(ApplicationService appService,
                          AppTagService appTagService,
                          AppCapabilityService appCapabilityService,
                          AssetCostService assetCostService,
                          BookmarkService bookmarkService,
                          CapabilityService capabilityService,
                          EntityAliasService entityAliasService,
                          EntityStatisticService entityStatisticService,
                          OrganisationalUnitService organisationalUnitService) {
        checkNotNull(appService, "ApplicationService must not be null");
        checkNotNull(appTagService, "appTagService cannot be null");
        checkNotNull(appCapabilityService, "appCapabilityService must not be null");
        checkNotNull(assetCostService, "assetCostService must not be null");
        checkNotNull(bookmarkService, "BookmarkDao must not be null");
        checkNotNull(capabilityService, "capabilityService must not be null");
        checkNotNull(entityAliasService, "entityAliasService cannot be null");
        checkNotNull(entityStatisticService, "entityStatisticService must not be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService must not be null");

        this.appService = appService;
        this.appTagService = appTagService;
        this.appCapabilityDao = appCapabilityService;
        this.assetCostService = assetCostService;
        this.bookmarkService = bookmarkService;
        this.capabilityService = capabilityService;
        this.entityAliasService = entityAliasService;
        this.entityStatisticService = entityStatisticService;
        this.organisationalUnitService = organisationalUnitService;
    }


    public AppView getAppView(long id) {
        EntityReference ref = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(id)
                .build();

        Future<List<ApplicationCapability>> appCapabilities = DB_EXECUTOR_POOL.submit(() ->
                appCapabilityDao.findCapabilitiesForApp(id));
        Future<List<Capability>> capabilities = DB_EXECUTOR_POOL.submit(() ->
                capabilityService.findByAppIds(id));
        Future<OrganisationalUnit> orgUnit = DB_EXECUTOR_POOL.submit(() ->
                organisationalUnitService.getByAppId(id));
        Future<List<Bookmark>> bookmarks = DB_EXECUTOR_POOL.submit(() ->
                bookmarkService.findByReference(ref));
        Future<List<String>> tags = DB_EXECUTOR_POOL.submit(() ->
                appTagService.findTagsForApplication(id));
        Future<List<String>> aliases = DB_EXECUTOR_POOL.submit(() ->
                entityAliasService.findAliasesForEntityReference(ref));
        Future<List<AssetCost>> costs = DB_EXECUTOR_POOL.submit(() ->
                assetCostService.findByAppId(id));
        Future<List<EntityStatistic>> stats = DB_EXECUTOR_POOL.submit(() ->
                entityStatisticService.findStatisticsForEntity(ref, true));

        return Unchecked.supplier(() -> ImmutableAppView.builder()
                    .organisationalUnit(orgUnit.get())
                    .bookmarks(bookmarks.get())
                    .tags(tags.get())
                    .aliases(aliases.get())
                    .appCapabilities(appCapabilities.get())
                    .capabilities(capabilities.get())
                    .costs(costs.get())
                    .entityStatistics(stats.get())
                    .build()).get();
    }

}
