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
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.appview.AppView;
import com.khartec.waltz.model.appview.ImmutableAppView;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import com.khartec.waltz.service.bookmark.BookmarkService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AppViewService {

    private final ApplicationService appService;
    private final BookmarkService bookmarkService;
    private final OrganisationalUnitService organisationalUnitService;
    private final AppCapabilityService appCapabilityDao;
    private final AssetCostService assetCostService;


    @Autowired
    public AppViewService(ApplicationService appService,
                          AppCapabilityService appCapabilityDao,
                          BookmarkService bookmarkService,
                          OrganisationalUnitService organisationalUnitService,
                          AssetCostService assetCostService) {
        checkNotNull(appService, "ApplicationService must not be null");
        checkNotNull(appCapabilityDao, "appCapabilityDao must not be null");
        checkNotNull(bookmarkService, "BookmarkDao must not be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService must not be null");
        checkNotNull(assetCostService, "assetCostService must not be null");

        this.appService = appService;
        this.appCapabilityDao = appCapabilityDao;
        this.bookmarkService = bookmarkService;
        this.organisationalUnitService = organisationalUnitService;
        this.assetCostService = assetCostService;
    }


    public AppView getAppView(long id) {
        ImmutableEntityReference ref = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(id)
                .build();

        Application app = appService.getById(id);

        return ImmutableAppView.builder()
                .app(app)
                .organisationalUnit(organisationalUnitService.getById(app.organisationalUnitId()))
                .bookmarks(bookmarkService.findByReference(ref))
                .tags(appService.findTagsForApplication(id))
                .aliases(appService.findAliasesForApplication(id))
                .appCapabilities(appCapabilityDao.findCapabilitiesForApp(id))
                .costs(assetCostService.findByAppId(id))
                .build();

    }
}
