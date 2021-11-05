package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.ChangeLogHelper;
import org.finos.waltz.integration_test.inmem.helpers.PersonHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ImmutableReleaseLifecycleStatusChangeCommand;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.service.survey.SurveyTemplateService;
import org.finos.waltz.model.survey.*;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.StringUtilities.lower;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkUserId;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;
import static org.finos.waltz.schema.tables.SurveyTemplate.SURVEY_TEMPLATE;
import static org.junit.Assert.*;

@Service
public class SurveyTemplateServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private SurveyTemplateService surveyTemplateService;

    @Autowired
    private SurveyQuestionService surveyQuestionService;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private ChangeLogHelper changeLogHelper;

    @Autowired
    private DSLContext dsl;


    @Test
    public void cannotFindAllIfNoMatchingPersonForUser() {
        assertThrows(
                "should fail if no matching person",
                IllegalArgumentException.class,
                () -> surveyTemplateService.findAll("foo"));
    }


    @Test
    public void findAllReturnsEmptyListIfNoTemplates() {
        String userId = mkUserId("findAllReturnsEmptyListIfNoTemplates");
        personHelper.createPerson(userId);
        List<SurveyTemplate> templates = surveyTemplateService.findAll(userId);
        assertNotNull(templates);
        assertTrue(templates.isEmpty());
    }


    @Test
    public void surveyCanBeCreated() {
        String userId = mkUserId("surveyCanBeCreated");
        String templateName = mkName("surveyCanBeCreated");

        personHelper.createPerson(userId);

        long id = createTemplate(userId, templateName);
        List<SurveyTemplate> all = surveyTemplateService.findAll(userId);

        assertNotNull(all);
        assertNotNull(find(
                st -> Optional.of(id).equals(st.id()),
                all));

        SurveyTemplate template = surveyTemplateService.getById(id);
        assertEquals(templateName, template.name());
        assertEquals("desc", template.description());
        assertEquals(Optional.of("extId"), template.externalId());
        assertEquals(EntityKind.APPLICATION, template.targetEntityKind());
        assertEquals(ReleaseLifecycleStatus.DRAFT, template.status());

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(
                mkRef(EntityKind.SURVEY_TEMPLATE, id),
                Operation.ADD);
    }


    @Test
    public void canUpdateLifecycleStatusOfTemplates() {
        String userId = mkUserId("canUpdateLifecycleStatusOfTemplates");
        String templateName = mkName("canUpdateLifecycleStatusOfTemplates");

        personHelper.createPerson(userId);

        long id = createTemplate(userId, templateName);

        assertEquals(ReleaseLifecycleStatus.DRAFT, surveyTemplateService.getById(id).status());

        updateStatus(userId, id, ReleaseLifecycleStatus.ACTIVE);

        assertEquals(ReleaseLifecycleStatus.ACTIVE, surveyTemplateService.getById(id).status());

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(
                mkRef(EntityKind.SURVEY_TEMPLATE, id),
                Operation.UPDATE);
    }


    @Test
    public void setSurveyTemplatesCanBeUpdated() {
        String userId = mkUserId("canUpdateLifecycleStatusOfTemplates");
        String templateName = mkName("canUpdateLifecycleStatusOfTemplates");
        personHelper.createPerson(userId);

        long id = createTemplate(userId, templateName);

        SurveyTemplate preTemplate = surveyTemplateService.getById(id);
        assertEquals(templateName, preTemplate.name());

        String newName = mkName("newName");
        SurveyTemplateChangeCommand change = ImmutableSurveyTemplateChangeCommand.builder()
                .id(id)
                .name(newName)
                .externalId("extId2")
                .description("desc2")
                .targetEntityKind(EntityKind.CHANGE_INITIATIVE)
                .build();

        surveyTemplateService.update(userId, change);

        SurveyTemplate postTemplate = surveyTemplateService.getById(id);
        assertEquals(Optional.of("extId2"), postTemplate.externalId());
        assertEquals(EntityKind.CHANGE_INITIATIVE, postTemplate.targetEntityKind());
        assertEquals("desc2", postTemplate.description());
        assertEquals(newName, postTemplate.name());

        changeLogHelper.assertChangeLogContainsAtLeastOneMatchingOperation(
                mkRef(EntityKind.SURVEY_TEMPLATE, id),
                Operation.UPDATE);
    }


    @Test
    public void templateCanBeDeletedIfNoRunsHaveBeenIssued() {
        String userId = mkUserId("canUpdateLifecycleStatusOfTemplates");
        String templateName = mkName("canUpdateLifecycleStatusOfTemplates");

        personHelper.createPerson(userId);
        long id = createTemplate(userId, templateName);

        assertEquals(ReleaseLifecycleStatus.DRAFT, surveyTemplateService.getById(id).status());
        Boolean rc = surveyTemplateService.delete(id);
        assertTrue(rc);
        assertNull(surveyTemplateService.getById(id));
    }


    @Test
    public void templatesCanBeCloned() {
        String userId = mkUserId("template");
        String templateName = mkName("template");

        personHelper.createPerson(userId);
        long id = createTemplate(userId, templateName);

        updateStatus(userId, id, ReleaseLifecycleStatus.ACTIVE);

        SurveyQuestion question = ImmutableSurveyQuestion
                .builder()
                .fieldType(SurveyQuestionFieldType.TEXT)
                .sectionName("section")
                .questionText("question")
                .surveyTemplateId(id)
                .build();

        surveyQuestionService.create(question);

        long cloneId = surveyTemplateService.clone(userId, id);

        assertTrue("clone id should be different", cloneId != id);

        SurveyTemplate cloned = surveyTemplateService.getById(cloneId);

        assertEquals("status should have reset to DRAFT", ReleaseLifecycleStatus.DRAFT, cloned.status());
        assertTrue(cloned.name().contains(templateName));
        assertTrue(lower(cloned.name()).contains("clone"));

        assertEquals("Should have copied the question", 1, surveyQuestionService.findForSurveyTemplate(cloneId).size());
    }


    @After
    public void teardown() {
        dsl.deleteFrom(SURVEY_QUESTION).execute();
        dsl.deleteFrom(SURVEY_TEMPLATE).execute();
    }


    private long createTemplate(String userId, String templateName) {
        SurveyTemplateChangeCommand cmd = ImmutableSurveyTemplateChangeCommand.builder()
                .name(templateName)
                .externalId("extId")
                .description("desc")
                .targetEntityKind(EntityKind.APPLICATION)
                .build();

        return surveyTemplateService.create(userId, cmd);
    }


    private void updateStatus(String userId, long id, ReleaseLifecycleStatus newStatus) {
        surveyTemplateService.updateStatus(
                userId,
                id,
                ImmutableReleaseLifecycleStatusChangeCommand.builder()
                        .newStatus(newStatus)
                        .build());
    }
}
