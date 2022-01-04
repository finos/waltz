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

package org.finos.waltz.service.survey;


import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.survey.SurveyViewDao;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;

@Service
public class SurveyInstanceViewService {

    private final PersonDao personDao;
    private final SurveyViewDao surveyViewDao;
    private final SurveyInstanceEvaluator instanceEvaluator;


    @Autowired
    public SurveyInstanceViewService(PersonDao personDao,
                                     SurveyInstanceEvaluator instanceEvaluator,
                                     SurveyViewDao surveyViewDao) {

        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(instanceEvaluator, "instanceEvaluator cannot be null");
        checkNotNull(surveyViewDao, "surveyViewDao cannot be null");

        this.personDao = personDao;
        this.instanceEvaluator = instanceEvaluator;
        this.surveyViewDao = surveyViewDao;
    }


    public SurveyInstanceInfo getInfoById(long instanceId) {
        return surveyViewDao.getById(instanceId);
    }


    public SurveyInstanceFormDetails getFormDetailsById(long instanceId) {
        return instanceEvaluator.eval(instanceId);
    }


    public Set<SurveyInstanceUserInvolvement> findForUser(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person person = getPersonByUsername(userName);

        Set<SurveyInstanceInfo> surveysOwned = surveyViewDao.findForOwner(person.id().get());
        Set<SurveyInstanceInfo> surveysAssigned = surveyViewDao.findForRecipient(person.id().get());

        return asSet(
                ImmutableSurveyInstanceUserInvolvement.builder()
                        .surveyInvolvementKind(SurveyInvolvementKind.OWNER)
                        .surveyInstances(surveysOwned)
                        .build(),
                ImmutableSurveyInstanceUserInvolvement.builder()
                        .surveyInvolvementKind(SurveyInvolvementKind.RECIPIENT)
                        .surveyInstances(surveysAssigned)
                        .build());
    }

    private Person getPersonByUsername(String userName) {
        Person person = personDao.getActiveByUserEmail(userName);
        checkNotNull(person, "userName %s cannot be resolved", userName);
        return person;
    }
}
