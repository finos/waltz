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

package com.khartec.waltz.service.assessment_rating;

import com.khartec.waltz.data.assessment_rating.AssessmentRatingDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.RemoveAssessmentRatingCommand;
import com.khartec.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.CHANGE_INITIATIVE;
import static com.khartec.waltz.model.EntityReference.mkRef;

@Service
public class AssessmentRatingService {

    private final AssessmentRatingDao assessmentRatingDao;
    private final ChangeLogService changeLogService;

    @Autowired
    public AssessmentRatingService(
            AssessmentRatingDao assessmentRatingDao,
            ChangeLogService changeLogService) {
        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.assessmentRatingDao = assessmentRatingDao;
        this.changeLogService = changeLogService;

    }


    public List<AssessmentRating> findForEntity(EntityReference ref) {
        return assessmentRatingDao.findForEntity(ref);
    }


    public boolean update(SaveAssessmentRatingCommand command, String username) {
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(String.format(
                        "Updated architecture funding recommendation as [%s - %s]",
                        command.ratingName(),
                        command.description()))
                .parentReference(mkRef(CHANGE_INITIATIVE, command.entityReference().id()))
                .userId(username)
                .childKind(command.entityReference().kind())
                .severity(Severity.INFORMATION)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(logEntry);

        return assessmentRatingDao.update(command);
    }


    public boolean create(SaveAssessmentRatingCommand command, String username) {
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(String.format(
                        "Created architecture funding recommendation as [%s - %s]",
                        command.ratingName(),
                        command.description()))
                .parentReference(mkRef(CHANGE_INITIATIVE, command.entityReference().id()))
                .userId(username)
                .childKind(command.entityReference().kind())
                .severity(Severity.INFORMATION)
                .operation(Operation.ADD)
                .build();

        changeLogService.write(logEntry);

        return assessmentRatingDao.create(command);
    }


    public boolean remove(RemoveAssessmentRatingCommand command, String username) {
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(String.format(
                        "Removed architecture funding recommendation"))
                .parentReference(mkRef(CHANGE_INITIATIVE, command.entityReference().id()))
                .userId(username)
                .childKind(command.entityReference().kind())
                .severity(Severity.INFORMATION)
                .operation(Operation.REMOVE)
                .build();

        changeLogService.write(logEntry);

        return assessmentRatingDao.remove(command);
    }
}
