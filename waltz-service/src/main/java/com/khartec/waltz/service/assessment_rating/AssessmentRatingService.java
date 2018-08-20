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
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.RemoveAssessmentRatingCommand;
import com.khartec.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class AssessmentRatingService {

    private final AssessmentRatingDao assessmentRatingDao;


    @Autowired
    public AssessmentRatingService(AssessmentRatingDao assessmentRatingDao) {
        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");

        this.assessmentRatingDao = assessmentRatingDao;
    }


    public List<AssessmentRating> findForEntity(EntityReference ref) {
        return assessmentRatingDao.findForEntity(ref);
    }


    public boolean update(SaveAssessmentRatingCommand command) {
        return assessmentRatingDao.update(command);
    }


    public boolean create(SaveAssessmentRatingCommand command) {
        return assessmentRatingDao.create(command);
    }


    public boolean remove(RemoveAssessmentRatingCommand command) {
        return assessmentRatingDao.remove(command);
    }
}
