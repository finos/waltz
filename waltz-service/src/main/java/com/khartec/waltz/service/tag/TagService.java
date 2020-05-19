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

package com.khartec.waltz.service.tag;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.tag.TagDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.tag.Tag;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
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

    private static final Logger LOG = LoggerFactory.getLogger(TagService.class);

    private final TagDao tagDao;
    private final ChangeLogService changeLogService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public TagService(TagDao tagDao, ChangeLogService changeLogService) {
        this.tagDao = tagDao;
        this.changeLogService = changeLogService;
    }


    public List<Tag> findTagsForEntityReference(EntityReference reference) {
        return tagDao.findTagsForEntityReference(reference);
    }


    public List<Tag> findTagsForEntityKind(EntityKind entityKind) {
        return tagDao.findTagsForEntityKind(entityKind);
    }


    public List<Tag> findTagsForEntityKindAndTargetSelector(EntityKind targetEntityKind,
                                                            IdSelectionOptions targetEntityIdSelectionOptions) {
        checkNotNull(targetEntityKind, "targetEntityKind cannot be null");
        checkNotNull(targetEntityIdSelectionOptions, "targetEntityIdSelectionOptions cannot be null");

        Select<Record1<Long>> targetEntityIdSelector = genericSelectorFactory
                .applyForKind(targetEntityKind, targetEntityIdSelectionOptions)
                .selector();
        return tagDao.findTagsForEntityKindAndTargetSelector(targetEntityKind, targetEntityIdSelector);
    }


    public Tag getById(long id) {
        return tagDao.getById(id);
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

        writeChangeLogEntries(ref, username, toRemove, toAdd);
        
        return findTagsForEntityReference(ref);
    }


    private void writeChangeLogEntries(EntityReference ref,
                                       String username,
                                       Set<String> removed,
                                       Set<String> added) {

        String postamble = new StringBuilder()
                .append(added.isEmpty() ? "" : " Added tags: " + added + ". ")
                .append(removed.isEmpty() ? "" : "Removed tags: " + removed + ". ")
                .toString();

        changeLogService.writeChangeLogEntries(ref, username, postamble, Operation.UPDATE);
    }


    private void createTagUsage(EntityReference ref, String tag, String username) {
        Tag existingTag = tagDao.getTagByNameAndTargetKind(ref.kind(), tag);

        Long tagId =  existingTag != null
                ? existingTag.id().get()
                : tagDao.createTag(ref.kind(), tag);

        tagDao.createTagUsage(ref, username, tagId);
    }
}
