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

package com.khartec.waltz.service.perspective_rating;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.perspective_rating.PerspectiveRatingDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.perspective.PerspectiveDefinition;
import com.khartec.waltz.model.perspective.PerspectiveRating;
import com.khartec.waltz.model.perspective.PerspectiveRatingValue;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;


@Service
public class PerspectiveRatingService {
    
    private final PerspectiveRatingDao perspectiveRatingDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public PerspectiveRatingService(PerspectiveRatingDao perspectiveRatingDao,
                                    ChangeLogService changeLogService) {
        checkNotNull(perspectiveRatingDao, "perspectiveRatingDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.perspectiveRatingDao = perspectiveRatingDao;
        this.changeLogService = changeLogService;
    }


    public List<PerspectiveRating> findForEntity(
            long categoryX,
            long categoryY,
            EntityReference ref) {
        return perspectiveRatingDao.findForEntity(categoryX, categoryY, ref);
    }


    public List<PerspectiveRating> findForEntity(
            PerspectiveDefinition defn,
            EntityReference ref) {
        return findForEntity(defn.categoryX(), defn.categoryY(), ref);
    }


    public int updatePerspectiveRatings(long categoryX,
                                         long categoryY,
                                         EntityReference ref,
                                         String username,
                                         Set<PerspectiveRatingValue> pendingValues) {
        List<PerspectiveRating> existingRatings = findForEntity(categoryX, categoryY, ref);
        Set<PerspectiveRatingValue> existingValues = SetUtilities.fromCollection(
                map(existingRatings, PerspectiveRating::value));

        Set<PerspectiveRatingValue> removals = SetUtilities.minus(existingValues, pendingValues);
        Set<PerspectiveRatingValue> additions = SetUtilities.minus(pendingValues, existingValues);

        int removalCount = perspectiveRatingDao.remove(ref, removals);
        int additionCount = perspectiveRatingDao.add(ref, additions, username);

        auditUpdatesAndRemovals(ref, username, removalCount, additionCount);

        return removalCount + additionCount;
    }


    public Collection<PerspectiveRating> findForEntity(EntityReference ref) {
        return perspectiveRatingDao.findForEntity(ref);
    }


    private void auditUpdatesAndRemovals(EntityReference ref, String username, int removalCount, int additionCount) {
        if (removalCount > 0) {
            changeLogService.write(ImmutableChangeLog.builder()
                    .message(String.format("%d perspective ratings removed", removalCount))
                    .parentReference(ref)
                    .userId(username)
                    .createdAt(LocalDateTime.now())
                    .severity(Severity.INFORMATION)
                    .operation(Operation.REMOVE)
                    .build());
        }

        if (additionCount > 0) {
            changeLogService.write(ImmutableChangeLog.builder()
                    .message(String.format("%d perspective ratings updated", additionCount))
                    .parentReference(ref)
                    .userId(username)
                    .createdAt(LocalDateTime.now())
                    .severity(Severity.INFORMATION)
                    .operation(Operation.UPDATE)
                    .build());
        }
    }

}
