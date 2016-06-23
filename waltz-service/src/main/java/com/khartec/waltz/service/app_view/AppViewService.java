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
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.appview.AppView;
import com.khartec.waltz.model.appview.ImmutableAppView;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.trait.Trait;
import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.entity_alias.EntityAliasService;
import com.khartec.waltz.service.entity_statistic.EntityStatisticService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.trait.TraitService;
import com.khartec.waltz.service.trait.TraitUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static java.util.Collections.emptyList;


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
    private final TraitService traitService;
    private final TraitUsageService traitUsageService;


    @Autowired
    public AppViewService(ApplicationService appService,
                          AppCapabilityService appCapabilityService,
                          AssetCostService assetCostService,
                          BookmarkService bookmarkService,
                          CapabilityService capabilityService,
                          EntityAliasService entityAliasService,
                          EntityStatisticService entityStatisticService,
                          OrganisationalUnitService organisationalUnitService,
                          TraitService traitService,
                          TraitUsageService traitUsageService) {
        checkNotNull(appService, "ApplicationService must not be null");
        checkNotNull(appCapabilityService, "appCapabilityService must not be null");
        checkNotNull(assetCostService, "assetCostService must not be null");
        checkNotNull(bookmarkService, "BookmarkDao must not be null");
        checkNotNull(capabilityService, "capabilityService must not be null");
        checkNotNull(entityAliasService, "entityAliasService cannot be null");
        checkNotNull(entityStatisticService, "entityStatisticService must not be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService must not be null");
        checkNotNull(traitService, "traitService must not be null");
        checkNotNull(traitUsageService, "traitUsageService must not be null");

        this.appService = appService;
        this.appCapabilityDao = appCapabilityService;
        this.assetCostService = assetCostService;
        this.bookmarkService = bookmarkService;
        this.capabilityService = capabilityService;
        this.entityAliasService = entityAliasService;
        this.entityStatisticService = entityStatisticService;
        this.organisationalUnitService = organisationalUnitService;
        this.traitUsageService = traitUsageService;
        this.traitService = traitService;
    }


    public AppView getAppView(long id) {
        EntityReference ref = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(id)
                .build();

        Application app = appService.getById(id);

        List<ApplicationCapability> appCapabilities = appCapabilityDao.findCapabilitiesForApp(id);
        List<Trait> traits = findTraitsForApplication(ref);

        List<Capability> capabilities = capabilityService.findByIds(map(appCapabilities, ac -> ac.capabilityId()).toArray(new Long[0]));

        return ImmutableAppView.builder()
                .app(app)
                .organisationalUnit(organisationalUnitService.getById(app.organisationalUnitId()))
                .bookmarks(bookmarkService.findByReference(ref))
                .tags(appService.findTagsForApplication(id))
                .aliases(entityAliasService.findAliasesForEntityReference(ref))
                .appCapabilities(appCapabilities)
                .capabilities(capabilities)
                .costs(assetCostService.findByAppId(id))
                .explicitTraits(traits)
                .entityStatisticsWithValues(entityStatisticService.findStatisticsForEntity(ref, true))
                .build();
    }


    private List<Trait> findTraitsForApplication(EntityReference ref) {
        List<TraitUsage> traitUsages = traitUsageService.findByEntityReference(ref);
        return traitUsages.isEmpty()
                ? emptyList()
                : traitService.findByIds(map(traitUsages, tu -> tu.traitId()));
    }
}
