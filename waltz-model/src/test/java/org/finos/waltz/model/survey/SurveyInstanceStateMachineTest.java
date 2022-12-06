package org.finos.waltz.model.survey;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.ImmutableEntityReference;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.finos.waltz.model.survey.SurveyInstanceAction.*;
import static org.finos.waltz.model.survey.SurveyInstanceStatus.*;
import static org.junit.jupiter.api.Assertions.*;


public class SurveyInstanceStateMachineTest {
    private final SurveyInstancePermissions admin = ImmutableSurveyInstancePermissions.builder()
            .isAdmin(true)
            .build();

    private final SurveyInstancePermissions participant = ImmutableSurveyInstancePermissions.builder()
            .isParticipant(true)
            .build();

    private final SurveyInstance survey = ImmutableSurveyInstance.builder()
            .surveyRunId(1L)
            .surveyEntity(ImmutableEntityReference.builder()
                    .entityLifecycleStatus(EntityLifecycleStatus.ACTIVE)
                    .description("desc")
                    .kind(EntityKind.ACTOR)
                    .name("name")
                    .id(10)
                    .build())
            .status(APPROVED)
            .dueDate(LocalDate.now())
            .approvalDueDate(LocalDate.now())
            .issuedOn(LocalDate.now())
            .build();

    @Test
    public void nextPossibleStatus() {
        SurveyInstanceStateMachine completed = SurveyInstanceStateMachineFactory.simple("COMPLETED");
        assertEquals(2, completed.nextPossibleStatus(admin, survey).size());
        assertTrue(completed.nextPossibleStatus(admin, survey).contains(APPROVED));
        assertTrue(completed.nextPossibleStatus(admin, survey).contains(REJECTED));
        assertEquals(0, completed.nextPossibleStatus(participant, survey).size());
    }

    @Test
    public void nextPossibleTransitions() {
        SurveyInstanceStateMachine completed = SurveyInstanceStateMachineFactory.simple("COMPLETED");
        assertEquals(2, completed.nextPossibleActions(admin, survey).size());
        assertTrue(completed.nextPossibleActions(admin, survey).contains(APPROVING));
        assertTrue(completed.nextPossibleActions(admin, survey).contains(REJECTING));
        assertEquals(0, completed.nextPossibleActions(participant, survey).size());
    }

    @Test
    public void process() {
        SurveyInstanceStateMachine state = SurveyInstanceStateMachineFactory.simple("NOT_STARTED");
        assertEquals(IN_PROGRESS, state.process(SAVING, admin, survey));
        assertEquals(COMPLETED, state.process(SUBMITTING, admin, survey));
        assertEquals(APPROVED, state.process(APPROVING, admin, survey));
        assertEquals(IN_PROGRESS, state.process(REOPENING, admin, survey));
        assertEquals(WITHDRAWN, state.process(WITHDRAWING, admin, survey));
        assertEquals(IN_PROGRESS, state.process(REOPENING, admin, survey));
        assertEquals(COMPLETED, state.process(SUBMITTING, admin, survey));
        assertEquals(REJECTED, state.process(REJECTING, admin, survey));
    }

    @Test
    public void permissionCheckPasses() {
        SurveyInstanceStateMachine state = SurveyInstanceStateMachineFactory.simple("NOT_STARTED");
        assertEquals(IN_PROGRESS, state.process(SAVING, participant, survey));
        assertEquals(COMPLETED, state.process(SUBMITTING, participant, survey));
    }

    @Test
    public void permissionCheckRejects() {
        SurveyInstanceStateMachine state = SurveyInstanceStateMachineFactory.simple("APPROVED");
        assertEquals(IN_PROGRESS, state.process(REOPENING, participant, survey));
        assertThrows(IllegalArgumentException.class,
                () -> state.process(WITHDRAWING, participant, survey)); // fails as only admin
    }

}