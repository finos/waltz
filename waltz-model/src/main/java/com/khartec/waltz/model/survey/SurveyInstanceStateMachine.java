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

import com.khartec.waltz.model.exceptions.NotAuthorizedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.survey.SurveyInstanceAction.*;
import static com.khartec.waltz.model.survey.SurveyInstanceStateTransition.transition;
import static com.khartec.waltz.model.survey.SurveyInstanceStatus.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class SurveyInstanceStateMachine {
    private SurveyInstanceStatus current;
    private final MultiValueMap<SurveyInstanceStatus, SurveyInstanceStateTransition> transitions;

    SurveyInstanceStateMachine(SurveyInstanceStatus current, MultiValueMap<SurveyInstanceStatus, SurveyInstanceStateTransition> transitions) {
        this.current = current;
        this.transitions = transitions;
    }

    public SurveyInstanceStatus getCurrent() {
        return current;
    }

    public List<SurveyInstanceStatus> nextPossibleStatus(SurveyInstancePermissions permissions, SurveyInstance instance) {
        return transitions.getOrDefault(current, emptyList())
                .stream()
                .filter(t -> t.getPredicate().apply(permissions, instance))
                .map(t -> t.getFutureStatus())
                .collect(toList());
    }

    public List<SurveyInstanceAction> nextPossibleActions(SurveyInstancePermissions permissions, SurveyInstance instance) {
        return transitions.getOrDefault(current, emptyList())
                .stream()
                .filter(t -> t.getPredicate().apply(permissions, instance))
                .map(t -> t.getAction())
                .collect(toList());
    }

    public SurveyInstanceStatus process(SurveyInstanceAction action, SurveyInstancePermissions permissions, SurveyInstance instance) {
        for (SurveyInstanceStateTransition possibleTransition: transitions.getOrDefault(current, emptyList())) {
            boolean isSameAction = possibleTransition.getAction() == action;
            boolean isAllowedByPredicate = possibleTransition.getPredicate().apply(permissions, instance);
            if (isSameAction && isAllowedByPredicate) {
                this.current = possibleTransition.getFutureStatus();
                return this.current;
            }
        }
        throw new IllegalArgumentException("You cannot transition from "  + current + " with action " + action  + " given permissions: " + permissions);
    }
}
