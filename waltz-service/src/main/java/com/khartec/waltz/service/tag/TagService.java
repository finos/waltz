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

package com.khartec.waltz.service.tag;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.tag.TagDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.tag.ImmutableTag;
import com.khartec.waltz.model.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.fromCollection;

@Service
public class TagService {

    private final TagDao tagDao;
    private static final Logger LOG = LoggerFactory.getLogger(TagService.class);

    @Autowired
    public TagService(TagDao tagDao) {
        this.tagDao = tagDao;
    }


    @Deprecated
    public List<Tag> findAllTags() {
        return tagDao.findAllTags();
    }

    @Deprecated
    public List<EntityReference> findByTag(String tag) {
        return tagDao.findByTag(tag);
    }

    public List<Tag> findTagsForEntityReference(EntityReference reference) {
        return tagDao.findTagsForEntityReference(reference);
    }


    public List<Tag> findTagsForEntityKind(EntityKind entityKind) {
        return tagDao.findTagsForEntityKind(entityKind);
    }

    public List<Tag> updateTags(EntityReference ref, Collection<String> tags, String username) {
        checkNotNull(tags, "tags cannot be null");
        LOG.info("Adding tags {} for entity ref {}", tags, ref);

        List<Tag> currentTags = findTagsForEntityReference(ref);
        Set<String> currentTagNames = fromCollection(currentTags)
                .stream()
                .map(Tag::name)
                .collect(Collectors.toSet());

        Set<String> requiredTags = fromCollection(tags);

        Set<String> toRemove = SetUtilities.minus(currentTagNames, requiredTags);
        Set<String> toAdd = SetUtilities.minus(requiredTags, currentTagNames);

        if(!toRemove.isEmpty()) {
            LOG.debug("removing tag {} from entity reference {}", toRemove, ref);
            toRemove.forEach(tag -> tagDao.removeTagUsage(ref, tag));
        }

        if(!toAdd.isEmpty()) {
            LOG.debug("adding tag {} to entity reference {}", toRemove, ref);
            toAdd.forEach(tag -> createTagUsage(ref, tag, username));
        }

        return findTagsForEntityReference(ref);
    }

    private void createTagUsage(EntityReference ref, String tag, String username) {
        Tag existingTag = tagDao.findTagByNameAndTargetKind(ref.kind(), tag);

        Long tagId =  existingTag != null
                ? existingTag.id().get()
                : tagDao.createTag(ref.kind(), tag);

        tagDao.createTagUsage(ref, username, tagId);
    }

    public Tag getTagWithUsageById(long id) {
        Tag tag = tagDao.getById(id);
        return ImmutableTag
                .builder()
                .from(tag)
                .tagUsages(tagDao.getTagUsageByTagId(id))
                .build();
    }
}
