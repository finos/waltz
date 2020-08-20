package com.khartec.waltz.model.survey;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<SurveyInstanceStatus> nextPossibleStatus() {
        return transitions.getOrDefault(current, emptyList())
                .stream()
                .map(t -> t.getFutureStatus())
                .collect(toList());
    }

    public List<SurveyInstanceAction> nextPossibleActions() {
        return transitions.getOrDefault(current, emptyList())
                .stream()
                .map(t -> t.getAction())
                .collect(toList());
    }

    public SurveyInstanceStatus process(SurveyInstanceAction action) {
        for (SurveyInstanceStateTransition possibleTransition: transitions.getOrDefault(current, emptyList())) {
            if (possibleTransition.getAction() == action) {
                current = possibleTransition.getFutureStatus();
                return current;
            }
        }
        throw new RuntimeException("You cannot transition from "  + current + " with action " + action);
    }

    public static SurveyInstanceStateMachine simple(String status) {
        return simple(SurveyInstanceStatus.valueOf(status));
    }

    public static SurveyInstanceStateMachine simple(SurveyInstanceStatus status) {
        MultiValueMap<SurveyInstanceStatus, SurveyInstanceStateTransition> transitions = new LinkedMultiValueMap<>();
        transitions.add(SurveyInstanceStatus.NOT_STARTED, transition(WITHDRAWING, WITHDRAWN));
        transitions.add(SurveyInstanceStatus.NOT_STARTED, transition(SUBMITTING, COMPLETED));
        transitions.add(SurveyInstanceStatus.NOT_STARTED, transition(SAVING, IN_PROGRESS));

        transitions.add(SurveyInstanceStatus.IN_PROGRESS, transition(SUBMITTING, COMPLETED));
        transitions.add(SurveyInstanceStatus.IN_PROGRESS, transition(WITHDRAWING, WITHDRAWN));
        transitions.add(SurveyInstanceStatus.IN_PROGRESS, transition(SAVING, IN_PROGRESS));

        transitions.add(SurveyInstanceStatus.COMPLETED, transition(APPROVING, APPROVED));
        transitions.add(SurveyInstanceStatus.COMPLETED, transition(REJECTING, REJECTED));

        transitions.add(SurveyInstanceStatus.APPROVED, transition(REOPENING, IN_PROGRESS));

        transitions.add(SurveyInstanceStatus.REJECTED, transition(WITHDRAWING, WITHDRAWN));
        transitions.add(SurveyInstanceStatus.REJECTED, transition(REOPENING, IN_PROGRESS));

        transitions.add(SurveyInstanceStatus.WITHDRAWN, transition(REOPENING, IN_PROGRESS));

        return new SurveyInstanceStateMachine(status, transitions);
    }
}
