package org.finos.waltz.model.survey;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.function.BiFunction;

import static org.finos.waltz.model.survey.SurveyInstanceAction.*;
import static org.finos.waltz.model.survey.SurveyInstanceStatus.*;

public class SurveyInstanceStateMachineFactory {
    // FILTERS
    private static BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> isAdminOrOwnerOrParticipant =
            (p, i) -> p.isAdmin() || p.hasOwnership() || p.isParticipant();
    private static BiFunction<SurveyInstancePermissions, SurveyInstance, Boolean> isAdminOrOwner =
            (p, i) -> p.isAdmin() || p.hasOwnership();

    // TRANSITIONS FOR THE SIMPLE SURVEY WORKFLOW
    private static MultiValueMap<SurveyInstanceStatus, SurveyInstanceStateTransition> simpleTransitions = new LinkedMultiValueMap<>();
    static {
        simpleTransitions.add(NOT_STARTED, SurveyInstanceStateTransition.transition(WITHDRAWING, WITHDRAWN, isAdminOrOwner));
        simpleTransitions.add(NOT_STARTED, SurveyInstanceStateTransition.transition(SUBMITTING, COMPLETED, isAdminOrOwnerOrParticipant));
        simpleTransitions.add(NOT_STARTED, SurveyInstanceStateTransition.transition(SAVING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(IN_PROGRESS, SurveyInstanceStateTransition.transition(SUBMITTING, COMPLETED, isAdminOrOwnerOrParticipant));
        simpleTransitions.add(IN_PROGRESS, SurveyInstanceStateTransition.transition(WITHDRAWING, WITHDRAWN, isAdminOrOwner));
        simpleTransitions.add(IN_PROGRESS, SurveyInstanceStateTransition.transition(SAVING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(COMPLETED, SurveyInstanceStateTransition.transition(APPROVING, APPROVED, isAdminOrOwner));
        simpleTransitions.add(COMPLETED, SurveyInstanceStateTransition.transition(REJECTING, REJECTED, isAdminOrOwner));

        simpleTransitions.add(APPROVED, SurveyInstanceStateTransition.transition(REOPENING, IN_PROGRESS, isAdminOrOwner));

        simpleTransitions.add(REJECTED, SurveyInstanceStateTransition.transition(WITHDRAWING, WITHDRAWN, isAdminOrOwner));
        simpleTransitions.add(REJECTED, SurveyInstanceStateTransition.transition(REOPENING, IN_PROGRESS, isAdminOrOwnerOrParticipant));

        simpleTransitions.add(WITHDRAWN, SurveyInstanceStateTransition.transition(REOPENING, IN_PROGRESS, isAdminOrOwner));
    }

    public static SurveyInstanceStateMachine simple(String status) {
        return simple(SurveyInstanceStatus.valueOf(status));
    }

    public static SurveyInstanceStateMachine simple(SurveyInstanceStatus status) {
        return new SurveyInstanceStateMachine(status, simpleTransitions);
    }
}
