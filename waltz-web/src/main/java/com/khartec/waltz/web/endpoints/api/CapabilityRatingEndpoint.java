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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.capability_rating.AppRatingChangesAction;
import com.khartec.waltz.service.capability_rating.CapabilityRatingService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class CapabilityRatingEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "capability-rating");

    private final CapabilityRatingService ratingService;
    private final ChangeLogService changeLogService;
    private final UserService userService;


    @Autowired
    public CapabilityRatingEndpoint(CapabilityRatingService ratingService, ChangeLogService changeLogService, UserService userService) {
        checkNotNull(ratingService, "ratingService must not be null");
        this.ratingService = ratingService;
        this.changeLogService = changeLogService;
        this.userService = userService;
    }


    @Override
    public void register() {

        String byParentPath = WebUtilities.mkPath(BASE_URL, "parent", ":kind", ":id");
        String byParentAndPerspectivePath = WebUtilities.mkPath(byParentPath, ":perspectiveCode");
        String byCapabilityPath = WebUtilities.mkPath(BASE_URL, "capability", ":id");
        String byOrgUnitPath = WebUtilities.mkPath(BASE_URL, "org-unit", ":id");
        String byOrgUnitTreePath = WebUtilities.mkPath(BASE_URL, "org-unit-tree", ":id");


        EndpointUtilities.getForList(byParentPath, (request, response) -> ratingService.findByParent(WebUtilities.getEntityReference(request)));

        EndpointUtilities.getForList(byParentAndPerspectivePath, (request, response) ->
                ratingService.findByParentAndPerspective(
                        WebUtilities.getEntityReference(request),
                        request.params("perspectiveCode")));

        EndpointUtilities.getForList(byCapabilityPath, (request, response) -> ratingService.findByCapability(WebUtilities.getId(request)));

        EndpointUtilities.getForList(byOrgUnitPath, (request, response) -> ratingService.findByOrganisationalUnit(WebUtilities.getId(request)));

        EndpointUtilities.getForList(byOrgUnitTreePath, (request, response) -> ratingService.findByOrganisationalUnitTree(WebUtilities.getId(request)));

        EndpointUtilities.post(WebUtilities.mkPath(BASE_URL), (request, response) -> {
            WebUtilities.requireRole(userService, request, Role.RATING_EDITOR);
            AppRatingChangesAction appRatingChangesAction = WebUtilities.readBody(request, AppRatingChangesAction.class);

            int changeCount = ratingService.update(appRatingChangesAction);

            changeLogService.write(ImmutableChangeLog.builder()
                    .userId(WebUtilities.getUser(request).userName())
                    .message("Updated capability ratings ( " + changeCount + " )")
                    .parentReference(appRatingChangesAction.application())
                    .severity(Severity.INFORMATION)
                    .build());

            return changeCount;
        });
    }
}
