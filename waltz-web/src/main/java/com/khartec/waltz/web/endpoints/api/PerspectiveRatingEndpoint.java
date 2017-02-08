/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.perspective.PerspectiveRating;
import com.khartec.waltz.model.perspective.PerspectiveRatingValue;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.perspective_rating.PerspectiveRatingService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.fromArray;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.putForDatum;


@Service
public class PerspectiveRatingEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "perspective-rating");


    private final PerspectiveRatingService perspectiveRatingService;
    private final UserRoleService userRoleService;


    @Autowired
    public PerspectiveRatingEndpoint(PerspectiveRatingService perspectiveRatingService, 
                                     UserRoleService userRoleService) {
        checkNotNull(perspectiveRatingService, "perspectiveRatingService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.perspectiveRatingService = perspectiveRatingService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String entityPath = mkPath(BASE, "entity", ":kind", ":id");
        String entityAxisPath = mkPath(entityPath, ":x", ":y");

        getForList(entityPath, this::findForEntity);
        putForDatum(entityAxisPath, this::updateForEntityAxis);
        getForList(entityAxisPath, this::findForEntityAxis);
    }


    private Collection<PerspectiveRating> findForEntity(Request request, Response z) {
        EntityReference ref = getEntityReference(request);
        return perspectiveRatingService.findForEntity(ref);
    }


    private Collection<PerspectiveRating> findForEntityAxis(Request request, Response z) {
        EntityReference ref = getEntityReference(request);
        long categoryX = getLong(request, "x");
        long categoryY = getLong(request, "y");
        return perspectiveRatingService.findForEntity(categoryX, categoryY, ref);
    }


    private int updateForEntityAxis(Request request, Response z) throws IOException {
        requireRole(userRoleService, request, Role.CAPABILITY_EDITOR);
        String username = getUsername(request);
        EntityReference ref = getEntityReference(request);
        long categoryX = getLong(request, "x");
        long categoryY = getLong(request, "y");
        PerspectiveRatingValue values[] = readBody(request, PerspectiveRatingValue[].class);

        return perspectiveRatingService.updatePerspectiveRatings(categoryX, categoryY, ref, username, fromArray(values));
    }
}
