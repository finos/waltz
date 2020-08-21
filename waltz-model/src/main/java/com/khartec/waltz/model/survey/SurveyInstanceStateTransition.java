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
