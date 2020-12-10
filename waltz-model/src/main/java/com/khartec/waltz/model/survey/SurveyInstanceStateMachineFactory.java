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

package com.khartec.waltz.model.survey;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.function.BiFunction;

import static com.khartec.waltz.model.survey.SurveyInstanceAction.*;
import static com.khartec.waltz.model.survey.SurveyInstanceAction.REOPENING;
import static com.khartec.waltz.model.survey.SurveyInstanceStateTransition.transition;
import static com.khartec.waltz.model.survey.SurveyInstanceStatus.*;
import static com.khartec.waltz.model.survey.SurveyInstanceStatus.IN_PROGRESS;

public class SurveyInstanceStateMachineFactory {
    // FILTERS
    private static BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> isAdminOrOwnerOrParticipant =
            (p, i) -> p.isAdmin() || p.hasOwnership() || p.isParticipant();
    private static BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> isAdminOrOwner =
            (p, i) -> p.isAdmin() || p.hasOwnership();

    // TRANSITIONS FOR THE SIMPLE SURVEY WORKFLOW
    private static MultiValueMap<SurveyInstanceStatus, SurveyInstanceStateTransition> simpleTransitions = new LinkedMultiValueMap<>();
    static {
        simpleTransitions.add(NOT_STARTED, transition(WITHDRAWING, WITHDRAWN, isAdminOrOwner));
        simpleTransitions.add(NOT_STARTED, transition(SUBMITTING, COMPLETED, isAdminOrOwnerOrParticipant));
        simpleTransitions.add(NOT_STARTED, transition(SAVING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(IN_PROGRESS, transition(SUBMITTING, COMPLETED, isAdminOrOwnerOrParticipant));
        simpleTransitions.add(IN_PROGRESS, transition(WITHDRAWING, WITHDRAWN, isAdminOrOwner));
        simpleTransitions.add(IN_PROGRESS, transition(SAVING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(COMPLETED, transition(APPROVING, APPROVED, isAdminOrOwner));
        simpleTransitions.add(COMPLETED, transition(REJECTING, REJECTED, isAdminOrOwner));

        simpleTransitions.add(APPROVED, transition(REOPENING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(REJECTED, transition(WITHDRAWING, WITHDRAWN, isAdminOrOwner));
        simpleTransitions.add(REJECTED, transition(REOPENING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(WITHDRAWN, transition(REOPENING, IN_PROGRESS, isAdminOrOwner));
    }

    public static SurveyInstanceStateMachine simple(String status) {
        return simple(SurveyInstanceStatus.valueOf(status));
    }

    public static SurveyInstanceStateMachine simple(SurveyInstanceStatus status) {
        return new SurveyInstanceStateMachine(status, simpleTransitions);
    }
}
