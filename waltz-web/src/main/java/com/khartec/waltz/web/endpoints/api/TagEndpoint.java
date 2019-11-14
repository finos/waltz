/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.tag.Tag;
import com.khartec.waltz.service.tag.TagService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class TagEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "tag");
    private final TagService tagService;


    @Autowired
    public TagEndpoint(TagService tagService) {
        checkNotNull(tagService, "tagService cannot be null");
        this.tagService = tagService;
    }

    @Override
    public void register() {
        DatumRoute<Tag> getByIdRoute = (req, res) ->
             tagService.getById(getId(req));

        ListRoute<Tag> updateRoute = (req, resp) -> {
            String username = getUsername(req);
            EntityReference ref = getEntityReference(req);
            List<String> tags = readStringsFromBody(req);
            return tagService.updateTags(ref, tags, username);
        };

        ListRoute<Tag> findTagsForEntityReference = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            return tagService.findTagsForEntityReference(ref);
        };

        ListRoute<Tag> findTagsForEntityKind = (req, resp) -> {
            EntityKind entityKind = getKind(req);
            return tagService.findTagsForEntityKind(entityKind);
        };

        getForDatum(mkPath(BASE_URL, "id", ":id"), getByIdRoute);
        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), findTagsForEntityReference);
        getForList(mkPath(BASE_URL, "target-kind", ":kind"), findTagsForEntityKind);
        postForList(mkPath(BASE_URL, "entity", ":kind", ":id"), updateRoute);
    }

}
