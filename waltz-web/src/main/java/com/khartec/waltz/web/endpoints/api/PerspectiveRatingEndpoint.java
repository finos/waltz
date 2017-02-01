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
import com.khartec.waltz.service.perspective_rating.PerspectiveRatingService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.util.Collection;

import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.getLong;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class PerspectiveRatingEndpoint implements Endpoint {

    private static final String BASE = mkPath("api", "perspective-rating");


    private final PerspectiveRatingService perspectiveRatingService;


    @Autowired
    public PerspectiveRatingEndpoint(PerspectiveRatingService perspectiveRatingService) {
        this.perspectiveRatingService = perspectiveRatingService;
    }


    @Override
    public void register() {
        String findForEntityPath = mkPath(BASE, ":x", ":y", "entity", ":kind", ":id");
        getForList(findForEntityPath, this::findForEntity);
    }


    private Collection<PerspectiveRating> findForEntity(Request request, Response z) {
        EntityReference ref = getEntityReference(request);
        long categoryX = getLong(request, "x");
        long categoryY = getLong(request, "y");
        return perspectiveRatingService.findForEntity(categoryX, categoryY, ref);
    }
}
