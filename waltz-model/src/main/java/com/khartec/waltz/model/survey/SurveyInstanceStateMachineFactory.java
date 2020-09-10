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
