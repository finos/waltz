/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.survey.SurveyQuestionDao;
import org.finos.waltz.data.survey.SurveyQuestionDropdownEntryDao;
import org.finos.waltz.data.survey.SurveyTemplateDao;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.finos.waltz.model.survey.SurveyTemplate;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.web.json.survey_template_exchange.ImmutableSurveyDropdownEntryModel;
import org.finos.waltz.web.json.survey_template_exchange.ImmutableSurveyQuestionModel;
import org.finos.waltz.web.json.survey_template_exchange.ImmutableSurveyTemplateExchange;
import org.finos.waltz.web.json.survey_template_exchange.ImmutableSurveyTemplateModel;
import org.finos.waltz.web.json.survey_template_exchange.SurveyDropdownEntryModel;
import org.finos.waltz.web.json.survey_template_exchange.SurveyQuestionModel;
import org.finos.waltz.web.json.survey_template_exchange.SurveyTemplateExchange;
import org.finos.waltz.web.json.survey_template_exchange.SurveyTemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.tables.SurveyTemplate.SURVEY_TEMPLATE;
import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;

@Service
public class SurveyTemplateExtractor implements DataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyTemplateExtractor.class);
    private static final String BASE_URL = mkPath("data-extract", "survey-template");
    private static final org.finos.waltz.schema.tables.SurveyTemplate st = SURVEY_TEMPLATE.as("st");
    private static final org.finos.waltz.schema.tables.Person p = PERSON.as("p");
    private static final org.finos.waltz.schema.tables.MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");


    private final SurveyQuestionDao questionDao;
    private final SurveyQuestionDropdownEntryDao dropdownEntryDao;
    private final SurveyTemplateDao templateDao;
    private final PersonDao personDao;


    @Autowired
    public SurveyTemplateExtractor(SurveyQuestionDao questionDao,
                                   SurveyQuestionDropdownEntryDao dropdownEntryDao,
                                   SurveyTemplateDao templateDao,
                                   PersonDao personDao) {
        this.questionDao = questionDao;
        this.dropdownEntryDao = dropdownEntryDao;
        this.templateDao = templateDao;
        this.personDao = personDao;
    }


    @Override
    public void register() {
        registerTemplateExtract();
    }


    private void registerTemplateExtract() {
        getForDatum(
            mkPath(BASE_URL, "template-id", ":id"),
            (request, response) -> prepareExtractModel(getId(request)));
    }


    private SurveyTemplateExchange prepareExtractModel(long templateId) {
        SurveyTemplate template = templateDao.getById(templateId);
        List<SurveyQuestion> questions = questionDao.findForTemplate(templateId);
        List<SurveyQuestionDropdownEntry> dropdownEntries = dropdownEntryDao.findForSurveyTemplate(templateId);
        Person owner = personDao.getById(template.ownerId());

        SurveyTemplateModel templateModel = ImmutableSurveyTemplateModel
                .builder()
                .description(template.description())
                .targetEntityKind(template.targetEntityKind())
                .name(template.name())
                .externalId(template.externalId())
                .ownerEmployeeId(owner.employeeId())
                .issuanceRole(template.issuanceRole())
                .build();

        Map<Long, Collection<SurveyDropdownEntryModel>> dropdownEntriesByQuestionId = groupBy(
                dropdownEntries,
                d -> d.questionId().orElse(null),
                d -> ImmutableSurveyDropdownEntryModel
                        .builder()
                        .position(d.position())
                        .value(d.value())
                        .build());

        List<SurveyQuestionModel> questionModels = questions
                .stream()
                .map(q -> ImmutableSurveyQuestionModel
                        .builder()
                        .questionText(q.questionText())
                        .fieldType(q.fieldType())
                        .position(q.position())
                        .isMandatory(q.isMandatory())
                        .allowComment(q.allowComment())
                        .externalId(q.externalId())
                        .inclusionPredicate(q.inclusionPredicate().orElse(null))
                        .helpText(q.helpText().orElse(null))
                        .qualifierEntity(q.qualifierEntity().orElse(null))
                        .sectionName(q.sectionName().orElse(null))
                        .parentExternalId(q.parentExternalId().orElse(null))
                        .label(q.label().orElse(null))
                        .dropdownEntries(dropdownEntriesByQuestionId.getOrDefault(
                                q.id().get(),
                                Collections.emptySet()))
                        .build())
                .collect(Collectors.toList());

        return ImmutableSurveyTemplateExchange
                .builder()
                .template(templateModel)
                .questions(questionModels)
                .build();
    }


    public static void main(String[] args) {
        LoggingUtilities.configureLogging();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        SurveyTemplateExtractor extractor = ctx.getBean(SurveyTemplateExtractor.class);
        SurveyTemplateExchange kitchenSink = extractor.prepareExtractModel(116L);
        System.out.println(kitchenSink);

    }

}
