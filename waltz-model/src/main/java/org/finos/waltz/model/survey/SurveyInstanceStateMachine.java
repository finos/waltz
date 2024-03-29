package org.finos.waltz.model.survey;

import org.springframework.util.MultiValueMap;

import java.util.List;

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
                .map(SurveyInstanceStateTransition::getFutureStatus)
                .collect(toList());
    }

    public List<SurveyInstanceAction> nextPossibleActions(SurveyInstancePermissions permissions, SurveyInstance instance) {
        return transitions
                .getOrDefault(current, emptyList())
                .stream()
                .filter(t -> t.getPredicate().apply(permissions, instance))
                .map(SurveyInstanceStateTransition::getAction)
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
