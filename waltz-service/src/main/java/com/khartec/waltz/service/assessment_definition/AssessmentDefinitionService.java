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
