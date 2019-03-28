/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.UpdateNameCommand;
import com.khartec.waltz.model.thumbnail.ThumbnailSaveCommand;
import com.khartec.waltz.service.thumbnail.ThumbnailService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class ThumbnailEndpoint implements Endpoint {

    private final String BASE_URL = mkPath("api", "thumbnail");
    private final ThumbnailService thumbnailService;


    @Autowired
    public ThumbnailEndpoint(ThumbnailService thumbnailService) {
        checkNotNull(thumbnailService, "thumbnailService cannot be null");
        this.thumbnailService = thumbnailService;
    }


    @Override
    public void register() {
        String byRefPath = mkPath(BASE_URL, ":kind", ":id");
        String savePath = mkPath(BASE_URL, "save");


        getForDatum(byRefPath, (req, res) -> {
            EntityReference entityRef = WebUtilities.getEntityReference(req);
            return thumbnailService.getByReference(entityRef);
        });


        deleteForDatum(byRefPath, (req, res) -> {
            EntityReference entityRef = WebUtilities.getEntityReference(req);
            return thumbnailService.deleteByReference(entityRef, getUsername(req));
        });


        postForDatum(savePath, (req, res) -> {
            thumbnailService.save(readBody(req, ThumbnailSaveCommand.class), getUsername(req));
            return true;
        });
    }
}
