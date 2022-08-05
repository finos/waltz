package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.survey.ImmutableSurveyTemplateChangeCommand;
import org.finos.waltz.model.survey.SurveyTemplate;
import org.finos.waltz.model.survey.SurveyTemplateChangeCommand;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.service.survey.SurveyTemplateService;
import org.finos.waltz.test_common_again.helpers.ChangeLogHelper;
import org.finos.waltz.test_common_again.helpers.PersonHelper;
import org.finos.waltz.test_common_again.helpers.SurveyTemplateHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.StringUtilities.lower;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common_again.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common_again.helpers.NameHelper.mkUserId;
import static org.junit.jupiter.api.Assertions.*;

@Service
public class SurveyTemplateServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private SurveyTemplateService surveyTemplateService;

    @Autowired
    private SurveyQuestionService surveyQuestionService;

    @Autowired
    private SurveyTemplateHelper templateHelper;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private ChangeLogHelper changeLogHelper;

    @Autowired
    private DSLContext dsl;


    @Test
    public void ifCannotFindPersonMustOnlyReturnActiveTemplates() {
        List<SurveyTemplate> surveys = surveyTemplateService.findAll("foo");
        Set<SurveyTemplate> nonActiveTemplates = surveys
                .stream()
                .filter(r -> r.status().equals(ReleaseLifecycleStatus.ACTIVE))
                .collect(toSet());
        assertEquals(emptySet(), nonActiveTemplates, "all templates should be active if user cannot be found");
    }


    @Test
    public void findAllReturnsEmptyListIfNoTemplates() {
        templateHelper.deleteAllSurveyTemplate();
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

        long id = templateHelper.createTemplate(userId, templateName);
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

        long id =  templateHelper.createTemplate(userId, templateName);

        assertEquals(ReleaseLifecycleStatus.DRAFT, surveyTemplateService.getById(id).status());

        templateHelper.updateStatus(userId, id, ReleaseLifecycleStatus.ACTIVE);

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

        long id =  templateHelper.createTemplate(userId, templateName);

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
        long id =  templateHelper.createTemplate(userId, templateName);

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
        long templateId = templateHelper.createTemplate(userId, templateName);
        templateHelper.addQuestion(templateId);

        templateHelper.updateStatus(userId, templateId, ReleaseLifecycleStatus.ACTIVE);

        long cloneId = surveyTemplateService.clone(userId, templateId);

        assertTrue(cloneId != templateId, "clone id should be different");

        SurveyTemplate cloned = surveyTemplateService.getById(cloneId);

        assertEquals(ReleaseLifecycleStatus.DRAFT, cloned.status(), "status should have reset to DRAFT");
        assertTrue(cloned.name().contains(templateName));
        assertTrue(lower(cloned.name()).contains("clone"));

        assertEquals(1, surveyQuestionService.findForSurveyTemplate(cloneId).size(), "Should have copied the question");
    }


}
