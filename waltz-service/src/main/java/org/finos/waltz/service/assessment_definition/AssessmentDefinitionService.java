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

package org.finos.waltz.service.assessment_definition;


import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.measurable.Measurable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;


@Service
public class AssessmentDefinitionService {

    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final MeasurableDao measurableDao;


    @Autowired
    public AssessmentDefinitionService(AssessmentDefinitionDao assessmentDefinitionDao,
                                       MeasurableDao measurableDao) {
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");

        this.measurableDao = measurableDao;
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


    public Collection<AssessmentDefinition> findByEntityReference(EntityReference entityReference) {
        switch (entityReference.kind()) {
            case MEASURABLE:
                Measurable m = measurableDao.getById(entityReference.id());
                return assessmentDefinitionDao.findByEntityKindAndQualifier(
                        entityReference.kind(),
                        mkRef(EntityKind.MEASURABLE_CATEGORY, m.categoryId()));
            default:
                return findByEntityKind(entityReference.kind());
        }
    }


    public Long save(AssessmentDefinition def) {
        return assessmentDefinitionDao.save(def);
    }


    public boolean remove(long definitionId) {
        return assessmentDefinitionDao.remove(definitionId) == 1;
    }

}
