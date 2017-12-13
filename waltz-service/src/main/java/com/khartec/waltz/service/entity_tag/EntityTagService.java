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

package com.khartec.waltz.service.entity_tag;

import com.khartec.waltz.data.entity_tag.EntityTagDao;
import com.khartec.waltz.model.EntityReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

/**
 * Tags for applications
 */
@Service
public class EntityTagService {

    private final EntityTagDao entityTagDao;


    @Autowired
    public EntityTagService(EntityTagDao entityTagDao) {
        this.entityTagDao = entityTagDao;
    }


    public List<String> findAllTags() {
        return entityTagDao.findAllTags();
    }


    public List<String> findTagsForEntityReference(EntityReference reference) {
        return entityTagDao.findTagsForEntityReference(reference);
    }


    public List<EntityReference> findByTag(String tag) {
        return entityTagDao.findByTag(tag);
    }


    public List<String> updateTags(EntityReference ref, Collection<String> tags, String username) {
        checkNotNull(tags, "tags cannot be null");
        entityTagDao.updateTags(ref, tags, username);
        return findTagsForEntityReference(ref);
    }
}
