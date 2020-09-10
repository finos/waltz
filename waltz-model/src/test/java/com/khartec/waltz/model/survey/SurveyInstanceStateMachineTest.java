package com.khartec.waltz.model.survey;

import com.khartec.waltz.model.ImmutableEntityReference;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.khartec.waltz.model.EntityKind.ACTOR;
import static com.khartec.waltz.model.EntityLifecycleStatus.ACTIVE;
import static com.khartec.waltz.model.survey.SurveyInstanceAction.*;
import static com.khartec.waltz.model.survey.SurveyInstanceStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SurveyInstanceStateMachineTest {
    private final SurveyInstancePermissions admin = ImmutableSurveyInstancePermissions.builder()
            .isAdmin(true)
            .build();

    private final SurveyInstancePermissions owner = ImmutableSurveyInstancePermissions.builder()
            .isOwner(true)
            .build();

    private final SurveyInstancePermissions participant = ImmutableSurveyInstancePermissions.builder()
            .isParticipant(true)
            .build();

    private final SurveyInstance survey = ImmutableSurveyInstance.builder()
            .surveyRunId(1L)
            .surveyEntity(ImmutableEntityReference.builder()
                    .entityLifecycleStatus(ACTIVE)
                    .description("desc")
                    .kind(ACTOR)
                    .name("name")
                    .id(10)
                    .build())
            .status(APPROVED)
            .dueDate(LocalDate.now())
            .build();

    private final SurveyInstance surveyWithApprovedDate = ImmutableSurveyInstance
            .copyOf(survey)
            .withApprovedAt(LocalDateTime.MIN);

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

    @Test(expected = IllegalArgumentException.class)
    public void permissionCheckRejects() {
        SurveyInstanceStateMachine state = SurveyInstanceStateMachineFactory.simple("APPROVED");
        assertEquals(IN_PROGRESS, state.process(REOPENING, participant, survey));
        state.process(WITHDRAWING, participant, survey); // fails as only admin
    }

}