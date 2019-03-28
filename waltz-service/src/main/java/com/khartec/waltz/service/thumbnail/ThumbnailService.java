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
