package org.finos.waltz.integration_test.inmem.service;

import junit.framework.AssertionFailedError;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.*;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdCommandResponse;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.service.survey.SurveyInstanceService;
import org.finos.waltz.service.survey.SurveyRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Service
public class SurveyRunServiceTest extends BaseInMemoryIntegrationTest {


    @Autowired
    private AppHelper appHelper;

    @Autowired
    private AppGroupHelper groupHelper;

    @Autowired
    private SurveyTemplateHelper templateHelper;

    @Autowired
    private SurveyRunService runService;

    @Autowired
    private SurveyInstanceService instanceService;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;


    @Test
    public void surveysAreIssuedToRecipientsViaInvolvementKind() throws InsufficientPrivelegeException {
        String stem = "srt_surveysAreIssuedToRecipientsViaInvolvementKind";

        String admin = mkName(stem, "admin");
        personHelper.createPerson(admin);

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        String u2a = mkName(stem, "user2a");
        Long u2aId = personHelper.createPerson(u2a);
        String u2b = mkName(stem, "user2b");
        Long u2bId = personHelper.createPerson(u2b);

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);

        long invKind = involvementHelper.mkInvolvementKind(mkName(stem, "invKind"));
        involvementHelper.createInvolvement(u1Id, invKind, appA);
        involvementHelper.createInvolvement(u2aId, invKind, appB);
        involvementHelper.createInvolvement(u2bId, invKind, appB);

        Long grpId = groupHelper.createAppGroupWithAppRefs(mkName(stem, "group"), asSet(appA, appB));

        long tId = templateHelper.createTemplate(admin, mkName("test"));

        // remove person 2
        personHelper.updateIsRemoved(u2aId, true);

        SurveyRunCreateCommand cmd = ImmutableSurveyRunCreateCommand.builder()
                .issuanceKind(SurveyIssuanceKind.GROUP)
                .name("test")
                .description("run desc")
                .selectionOptions(IdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.APP_GROUP, grpId)))
                .surveyTemplateId(tId)
                .addInvolvementKindIds(invKind)
                .dueDate(DateTimeUtilities.today().plusMonths(1))
                .approvalDueDate(DateTimeUtilities.today().plusMonths(1))
                .contactEmail("someone@somewhere.com")
                .build();

        IdCommandResponse runResp = runService.createSurveyRun(admin, cmd);
        Long surveyRunId = runResp.id().orElseThrow(() -> new AssertionFailedError("Failed to create run"));

        ImmutableInstancesAndRecipientsCreateCommand createCmd = ImmutableInstancesAndRecipientsCreateCommand.builder()
                .surveyRunId(surveyRunId)
                .dueDate(toLocalDate(nowUtcTimestamp()))
                .approvalDueDate(toLocalDate(nowUtcTimestamp()))
                .excludedRecipients(emptySet())
                .build();
        runService.createSurveyInstancesAndRecipients(createCmd);

        Set<SurveyInstance> instances = instanceService.findForSurveyRun(surveyRunId);

        assertEquals("should be 2 instances", 2, instances.size());

        SurveyInstance instanceA = findInstanceForApp(instances, appA);
        SurveyInstance instanceB = findInstanceForApp(instances, appB);
        assertNotNull(instanceA);
        assertNotNull(instanceB);
        assertEquals("instance won't have been started yet", SurveyInstanceStatus.NOT_STARTED, instanceA.status());
        assertEquals("instance won't have been started yet", SurveyInstanceStatus.NOT_STARTED, instanceB.status());

        Long instanceAId = instanceA.id().orElseThrow(() -> new AssertionFailedError("Failed to find instance for app A"));
        Long instanceBId = instanceB.id().orElseThrow(() -> new AssertionFailedError("Failed to find instance for app B"));
        assertNotNull("check user 1 via api", instanceService.checkPersonIsRecipient(u1, instanceAId));
        assertNotNull("admin is owner of instance A", instanceService.checkPersonIsOwnerOrAdmin(admin, instanceAId));
        assertNotNull("admin is owner of instance B", instanceService.checkPersonIsOwnerOrAdmin(admin, instanceBId));

        Set<SurveyInstance> instancesForU1 = instanceService.findForRecipient(u1Id);
        Set<SurveyInstance> instancesForU2a = instanceService.findForRecipient(u2aId);
        assertEquals("instances for u1 should be just an instance for appA", asSet(instanceA), instancesForU1);
        assertEquals("should be no instances for user 2A", Collections.emptySet(), instancesForU2a);

        assertEquals("can find by name or id (1)", instanceService.findForRecipient(u1Id), instanceService.findForRecipient(u1));
        assertEquals("can find by name or id (2a)", instanceService.findForRecipient(u2bId), instanceService.findForRecipient(u2b));
        assertThrows("finding by removed user throws an exception", IllegalArgumentException.class, () -> instanceService.findForRecipient(u2a));

        List<Person> aRecips = instanceService.findRecipients(instanceAId);
        List<Person> bRecips = instanceService.findRecipients(instanceBId);
        assertEquals("Expect user1 to be the recipient", asSet(u1), recipsToUserIds(aRecips));
        assertEquals("app B has only one recipient (u2a) as the other involved person (u2a) has been removed", asSet(u2b), recipsToUserIds(bRecips));
    }


    private Set<String> recipsToUserIds(List<Person> aRecips) {
        return map(aRecips, Person::userId);
    }


    private SurveyInstance findInstanceForApp(Set<SurveyInstance> instances, EntityReference app) {
        return find(d -> d.surveyEntity().equals(app), instances).orElse(null);
    }

}
