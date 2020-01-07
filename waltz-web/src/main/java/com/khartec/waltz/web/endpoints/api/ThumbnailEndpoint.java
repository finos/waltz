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
