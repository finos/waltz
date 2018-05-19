/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.survey.SurveyQuestionDropdownEntryService;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.service.survey.SurveyTemplateService;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static java.util.Collections.emptyList;
import static org.jooq.lambda.tuple.Tuple.tuple;


public class SurveyBuilder {

    private static final AtomicInteger positionCounter = new AtomicInteger(0);

    private static final List<SurveyQuestionDropdownEntry> adoptionEnum = newArrayList(
            SurveyQuestionDropdownEntry.mkEntry("Yes", 10),
            SurveyQuestionDropdownEntry.mkEntry("No", 20),
            SurveyQuestionDropdownEntry.mkEntry("Already adopted", 30),
            SurveyQuestionDropdownEntry.mkEntry("Not required", 40));

    private static final List<SurveyQuestionDropdownEntry> processingEnum = newArrayList(
            SurveyQuestionDropdownEntry.mkEntry("Data is passed through (No change)", 10),
            SurveyQuestionDropdownEntry.mkEntry("Data is added (additional data added)", 20),
            SurveyQuestionDropdownEntry.mkEntry("Data is altered (existing data is updated)", 30));

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQA =
            (tId, section) -> tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.BOOLEAN)
                        .questionText(String.format("Is there any %s data in the system?", section))
                        .isMandatory(true)
                        .surveyTemplateId(tId)
                        .sectionName(section)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQB =
            (tId, dt) -> tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.BOOLEAN)
                        .questionText(String.format("Is the system originating %s data (creating)", dt))
                        .isMandatory(false)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQC1 =
            (tId, dt) -> tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.APPLICATION)
                        .questionText("Provide the primary system originating the data into this system")
                        .isMandatory(false)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQC2 = (tId, dt) ->
            tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.BOOLEAN)
                        .questionText("Is the data provided by multiple systems?")
                        .isMandatory(false)
                        .allowComment(true)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQD = (tId, dt) ->
            tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.DROPDOWN)
                        .questionText("What happens to the data once it is consumed?")
                        .isMandatory(false)
                        .allowComment(true)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                processingEnum);

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQE1 = (tId, dt) ->
            tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.APPLICATION)
                        .questionText("Provide the primary system consuming the data into this system")
                        .isMandatory(false)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQE2 = (tId, dt) ->
            tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.BOOLEAN)
                        .questionText("Is the data consumed by multiple systems?")
                        .isMandatory(false)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQF = (tId, dt) ->
            tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.DROPDOWN)
                        .questionText("Is the system scheduled to adopt from an authoritative source / distributor?")
                        .isMandatory(false)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                adoptionEnum);

    private static final BiFunction<Long, String, Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> mkQG = (tId, dt) ->
            tuple(
                ImmutableSurveyQuestion.builder()
                        .position(positionCounter.getAndAdd(10))
                        .fieldType(SurveyQuestionFieldType.DATE)
                        .questionText("Specify target date (or historical adoption date) when all phases of the adoption completed")
                        .isMandatory(false)
                        .surveyTemplateId(tId)
                        .sectionName(dt)
                        .build(),
                emptyList());

    private static final BiFunction<Long, String, List<Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>>> mkSectionQs = (tId, section) -> newArrayList(
            mkQA.apply(tId, section),
            mkQB.apply(tId, section),
            mkQC1.apply(tId, section),
            mkQC2.apply(tId, section),
            mkQD.apply(tId, section),
            mkQE1.apply(tId, section),
            mkQE2.apply(tId, section),
            mkQF.apply(tId, section),
            mkQG.apply(tId, section));

    private static final List<String> sections = newArrayList(
            "Book - How the bank's businesses are organised (e.g. Books, Business Cost & Profit Centre, Hierarchy, UBR)",
            "Instrument - Instances of the product and services the bank provides (e.g. Indices & Basket, listed derivatives securities)",
            "Client (party) - Parties to whom the bank provides product and services",
            "Product - Product and service the bank sells to clients",
            "Employee / Person - Bank staff / external person who produce/support its product and services",
            "Agreement (Legal) - Contractual agreements (e.g. Netting, Master, Collateral, Cash Deposit, Settlement, Guarantee)",
            "DB Organisation", "Consolidated hierarchies, Internal agreements, Transfer pricing, Revenue shares, Legal hierarchies",
            "Market Data", "Sourced and derived data to form input into pricing and other valuation models",
            "Global Static", "Calendars, Periods, Countries, Regions, Cities and Currencies");

    private static final Function<Long, List<Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>>> mkAllQs =
            (tId) -> sections
                .stream()
                .flatMap(s -> mkSectionQs.apply(tId, s).stream())
                .collect(Collectors.toList());

    private static Long ownerId = 278769L; // watkdav

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SurveyTemplateService surveyTemplateService = ctx.getBean(SurveyTemplateService.class);
        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);
        SurveyQuestionDropdownEntryService surveyQuestionDropdownEntryService = ctx.getBean(SurveyQuestionDropdownEntryService.class);

        long templateId = storeTemplate(surveyTemplateService);
        storeQuestions(surveyQuestionService, surveyQuestionDropdownEntryService, mkAllQs.apply(templateId));
   }

    private static void storeQuestions(SurveyQuestionService surveyQuestionService,
                                       SurveyQuestionDropdownEntryService surveyQuestionDropdownEntryService,
                                       List<Tuple2<SurveyQuestion, List<SurveyQuestionDropdownEntry>>> questions) {
        questions.forEach(t -> {
            long qId = surveyQuestionService.create(t.v1);
            if (! t.v2.isEmpty()) {
                surveyQuestionDropdownEntryService.saveEntries(qId, t.v2);
            }
        });
    }

    private static long storeTemplate(SurveyTemplateService surveyTemplateService) {
        ImmutableSurveyTemplate template = ImmutableSurveyTemplate.builder()
                .name("GTB Ref Data Survey")
                .description("TBC")
                .targetEntityKind(EntityKind.APPLICATION)
                .status(ReleaseLifecycleStatus.DRAFT)
                .createdAt(DateTimeUtilities.nowUtc())
                .ownerId(ownerId)
                .build();

        SurveyTemplateChangeCommand createTemplateCommand = ImmutableSurveyTemplateChangeCommand.builder()
                .name(template.name())
                .description(template.description())
                .targetEntityKind(template.targetEntityKind())
                .build();

        return surveyTemplateService.create(
                "david.watkins@somewhere.com",
                createTemplateCommand);
    }
}
