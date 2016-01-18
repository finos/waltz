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

package com.khartec.waltz.service.application;

import com.khartec.waltz.data.application.AppAliasDao;
import com.khartec.waltz.data.application.AppTagDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.application.AppRegistrationRequest;
import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.AssetCodeRelationshipKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotEmptyString;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.utils.IdUtilities.toIds;
import static java.util.Collections.emptyMap;


@Service
public class ApplicationService {


    private final ApplicationDao applicationDao;
    private final OrganisationalUnitDao orgUnitDao;
    private final AppTagDao appTagDao;
    private final AppAliasDao appAliasDao;


    @Autowired
    public ApplicationService(ApplicationDao appDao, 
                              AppTagDao appTagDao,
                              AppAliasDao appAliasDao,
                              OrganisationalUnitDao orgUnitDao) {
        checkNotNull(appDao, "appDao must not be null");
        checkNotNull(appTagDao, "appTagDao must not be null");
        checkNotNull(appAliasDao, "appAliasDao must not be null");
        checkNotNull(orgUnitDao, "orgUnitDao must not be null");
        
        this.applicationDao = appDao;
        this.appTagDao = appTagDao;
        this.appAliasDao = appAliasDao;
        this.orgUnitDao = orgUnitDao;
    }


    public Application getById(long id) {
        return applicationDao.getById(id);
    }


    public List<Application> findByOrganisationalUnitId(long id) {
        return applicationDao.findByOrganisationalUnitId(id);
    }


    public List<Application> findByOrganisationalUnitTree(long id) {
        List<Long> orgUnitIds = toIds(orgUnitDao.findDescendants(id));
        return applicationDao.findByOrganisationalUnitIds(orgUnitIds);
    }


    public List<Application> getAll() {
        return applicationDao.getAll();
    }


    public List<Tally> countByOrganisationalUnit() {
        return applicationDao.countByOrganisationalUnit();
    }


    public List<Application> search(String query) {
        return applicationDao.search(query);
    }


    public List<Application> findByIds(List<Long> ids) throws SQLException {
        return applicationDao.findByIds(ids);
    }


    public AppRegistrationResponse registerApp(AppRegistrationRequest request) {
        checkNotEmptyString(request.name(), "Cannot register app with no name");
        return applicationDao.registerApp(request);
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


    public List<String> findAllTags() {
        return appTagDao.findAllTags();
    }


    public List<Application> findByTag(String tag) {
        return appTagDao.findByTag(tag);
    }


    public List<String> findTagsForApplication(long appId) {
        return appTagDao.findTagsForApplication(appId);
    }

    public List<String> findAliasesForApplication(long appId) {
        return appAliasDao.findAliasesForApplication(appId);
    }

    public int[] updateTags(long id, List<String> tags) {
        return appTagDao.updateTags(id, tags);
    }

    public int[] updateAliases(long id, List<String> aliases) {
        return appAliasDao.updateAliases(id, aliases);
    }
}

