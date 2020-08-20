package com.khartec.waltz.model.survey;

import org.junit.Test;

import static com.khartec.waltz.model.survey.SurveyInstanceAction.*;
import static com.khartec.waltz.model.survey.SurveyInstanceStatus.*;
import static org.junit.Assert.*;

public class SurveyInstanceStateMachineTest {

    @Test
    public void nextPossibleStatus() {
        SurveyInstanceStateMachine completed = SurveyInstanceStateMachine.simple("COMPLETED");
        assertEquals(2, completed.nextPossibleStatus().size());
        assertTrue(completed.nextPossibleStatus().contains(APPROVED));
        assertTrue(completed.nextPossibleStatus().contains(REJECTED));
    }

    @Test
    public void nextPossibleTransitions() {
        SurveyInstanceStateMachine completed = SurveyInstanceStateMachine.simple("COMPLETED");
        assertEquals(2, completed.nextPossibleActions().size());
        assertTrue(completed.nextPossibleActions().contains(APPROVING));
        assertTrue(completed.nextPossibleActions().contains(REJECTING));
    }

    @Test
    public void process() {
        SurveyInstanceStateMachine state = SurveyInstanceStateMachine.simple("NOT_STARTED");
        assertEquals(IN_PROGRESS, state.process(SAVING));
        assertEquals(COMPLETED, state.process(SUBMITTING));
        assertEquals(APPROVED, state.process(APPROVING));
        assertEquals(IN_PROGRESS, state.process(REOPENING));
        assertEquals(WITHDRAWN, state.process(WITHDRAWING));
        assertEquals(IN_PROGRESS, state.process(REOPENING));
        assertEquals(COMPLETED, state.process(SUBMITTING));
        assertEquals(REJECTED, state.process(REJECTING));
    }
}