/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.service.thumbnail.ThumbnailService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.thumbnail.ThumbnailSaveCommand;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class ThumbnailEndpoint implements Endpoint {

    private final String BASE_URL = WebUtilities.mkPath("api", "thumbnail");
    private final ThumbnailService thumbnailService;


    @Autowired
    public ThumbnailEndpoint(ThumbnailService thumbnailService) {
        checkNotNull(thumbnailService, "thumbnailService cannot be null");
        this.thumbnailService = thumbnailService;
    }


    @Override
    public void register() {
        String byRefPath = WebUtilities.mkPath(BASE_URL, ":kind", ":id");
        String savePath = WebUtilities.mkPath(BASE_URL, "save");


        EndpointUtilities.getForDatum(byRefPath, (req, res) -> {
            EntityReference entityRef = WebUtilities.getEntityReference(req);
            return thumbnailService.getByReference(entityRef);
        });


        EndpointUtilities.deleteForDatum(byRefPath, (req, res) -> {
            EntityReference entityRef = WebUtilities.getEntityReference(req);
            return thumbnailService.deleteByReference(entityRef, WebUtilities.getUsername(req));
        });


        EndpointUtilities.postForDatum(savePath, (req, res) -> {
            thumbnailService.save(WebUtilities.readBody(req, ThumbnailSaveCommand.class), WebUtilities.getUsername(req));
            return true;
        });
    }
}
