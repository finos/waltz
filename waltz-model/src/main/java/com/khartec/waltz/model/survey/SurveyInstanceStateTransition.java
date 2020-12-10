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

import java.util.Objects;
import java.util.function.BiFunction;

public class SurveyInstanceStateTransition {
    private final SurveyInstanceAction action;
    private final SurveyInstanceStatus futureStatus;
    private final BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> predicate;

    SurveyInstanceStateTransition(SurveyInstanceAction action, SurveyInstanceStatus futureStatus, BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> predicate) {
        Objects.requireNonNull(action, "Action cannot be null");
        Objects.requireNonNull(futureStatus, "Future Status cannot be null");
        this.action = action;
        this.futureStatus = futureStatus;
        this.predicate = predicate;
    }

    public SurveyInstanceAction getAction() {
        return action;
    }

    public SurveyInstanceStatus getFutureStatus() {
        return futureStatus;
    }

    public BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> getPredicate() {
        return predicate;
    }

    public static SurveyInstanceStateTransition transition(SurveyInstanceAction action, SurveyInstanceStatus futureStatus, BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> predicate) {
        return new SurveyInstanceStateTransition(action, futureStatus, predicate);
    }

    public static SurveyInstanceStateTransition transition(SurveyInstanceAction action, SurveyInstanceStatus futureStatus) {
        return new SurveyInstanceStateTransition(action, futureStatus, (p, i) -> true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SurveyInstanceStateTransition that = (SurveyInstanceStateTransition) o;
        return action == that.action &&
                futureStatus == that.futureStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, futureStatus);
    }
}
