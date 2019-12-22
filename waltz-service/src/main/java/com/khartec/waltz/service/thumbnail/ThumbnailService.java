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

package com.khartec.waltz.service.thumbnail;


import com.khartec.waltz.data.thumbnail.ThumbnailDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.thumbnail.ImmutableThumbnail;
import com.khartec.waltz.model.thumbnail.Thumbnail;
import com.khartec.waltz.model.thumbnail.ThumbnailSaveCommand;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.lang.String.format;


@Service
public class ThumbnailService {

    private final ThumbnailDao thumbnailDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public ThumbnailService(ChangeLogService changeLogService,
                            ThumbnailDao thumbnailDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(thumbnailDao, "entityAttachmentDao cannot be null");

        this.changeLogService = changeLogService;
        this.thumbnailDao = thumbnailDao;
    }


    public Thumbnail getByReference(EntityReference reference) {
        return thumbnailDao.getByReference(reference);
    }


    public void save(ThumbnailSaveCommand cmd, String username) {
        checkNotNull(cmd, "cmd cannot be null");
        checkNotNull(username, "username cannot be null");

        // create an thumbnail
        Thumbnail thumbnail = ImmutableThumbnail.builder()
                .parentEntityReference(cmd.parentEntityReference())
                .mimeType(cmd.mimeType())
                .blob(cmd.blob())
                .lastUpdatedBy(username)
                .build();


        Thumbnail existingThumbnail = thumbnailDao.getByReference(cmd.parentEntityReference());
        if(existingThumbnail == null) {
            // existing so update
            thumbnailDao.deleteByReference(cmd.parentEntityReference());
            thumbnailDao.create(thumbnail);
            auditChange("updated", cmd.parentEntityReference(), username, Operation.UPDATE);

        } else {
            // not existing create
            thumbnailDao.create(thumbnail);
            auditChange("created", cmd.parentEntityReference(), username, Operation.REMOVE);
        }
    }


    public boolean deleteByReference(EntityReference ref, String username) {
        auditChange("removed", ref, username, Operation.REMOVE);
        return thumbnailDao.deleteByReference(ref) == 1;
    }


    private void auditChange(String verb, EntityReference parentRef, String username, Operation operation) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(parentRef)
                .severity(Severity.INFORMATION)
                .userId(username)
                .message(format(
                        "Thumbnail %s",
                        verb))
                .childKind(parentRef.kind())
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }

}
