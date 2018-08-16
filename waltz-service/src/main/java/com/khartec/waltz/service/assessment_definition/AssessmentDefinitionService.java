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

package com.khartec.waltz.service.assessment_definition;


import com.khartec.waltz.data.assessment_definition.AssessmentDefinitionDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AssessmentDefinitionService {

    private final AssessmentDefinitionDao assessmentDefinitionDao;


    @Autowired
    public AssessmentDefinitionService(AssessmentDefinitionDao assessmentDefinitionDao) {
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");

        this.assessmentDefinitionDao = assessmentDefinitionDao;
    }


    public AssessmentDefinition getById(long id) {
        return assessmentDefinitionDao.getById(id);
    }


    public List<AssessmentDefinition> findAll() {
        return assessmentDefinitionDao.findAll();
    }


    public List<AssessmentDefinition> findByEntityKind(EntityKind kind) {
        return assessmentDefinitionDao.findByEntityKind(kind);
    }
}
