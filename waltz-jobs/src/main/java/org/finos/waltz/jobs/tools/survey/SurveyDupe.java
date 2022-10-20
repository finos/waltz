package org.finos.waltz.jobs.tools.survey;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.survey.SurveyQuestionDao;
import org.finos.waltz.data.survey.SurveyQuestionDropdownEntryDao;
import org.finos.waltz.data.survey.SurveyTemplateDao;
import org.finos.waltz.jobs.tools.survey.config.*;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.schema.tables.SurveyTemplate;
import org.finos.waltz.service.DIBaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.jobs.tools.survey.config.TargetSectionConfig.mkTargetSection;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class SurveyDupe {


    private static final SurveyTemplate st = SurveyTemplate.SURVEY_TEMPLATE;


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        SurveyQuestionDao surveyQuestionDao = ctx.getBean(SurveyQuestionDao.class);
        SurveyTemplateDao surveyTemplateDao = ctx.getBean(SurveyTemplateDao.class);
        SurveyQuestionDropdownEntryDao surveyDropdownDao = ctx.getBean(SurveyQuestionDropdownEntryDao.class);

        SurveyDupeConfig dupeConfig = prepConfig();

        ensureTargetDoesNotExist(dsl, dupeConfig);

        long sourceTemplateId = findSourceTemplateId(
                dsl,
                dupeConfig);

        long targetTemplateId = dupeBasicSurveyRecord(
                surveyTemplateDao,
                dupeConfig,
                sourceTemplateId);

        List<SurveyQuestion> sourceQuestions = surveyQuestionDao.findForTemplate(sourceTemplateId);

        Set<Tuple2<Long, ImmutableSurveyQuestion>> basics = dupeBasicSurveyQuestions(
                sourceQuestions,
                targetTemplateId,
                map(dupeConfig.sections(), SectionDupeConfig::sectionName));

        Set<Tuple2<Long, ImmutableSurveyQuestion>> repeating = dupeSectionSurveyQuestions(
                sourceQuestions,
                targetTemplateId,
                dupeConfig.sections());

        Map<Optional<Long>, Collection<SurveyQuestionDropdownEntry>> dropDownEntriesByQuestionId = groupBy(
                surveyDropdownDao.findForSurveyTemplate(sourceTemplateId),
                SurveyQuestionDropdownEntry::questionId);

        SetUtilities
                .union(basics, repeating)
                .forEach(t -> {
                    long sourceQuestionId = t.v1;
                    long targetQuestionId = surveyQuestionDao.create(t.v2);

                    Collection<SurveyQuestionDropdownEntry> dropDownsToDupe = dropDownEntriesByQuestionId
                            .getOrDefault(
                                    Optional.of(sourceQuestionId),
                                    emptySet());

                    surveyDropdownDao.saveEntries(
                            targetQuestionId,
                            map(dropDownsToDupe,
                                d -> ImmutableSurveyQuestionDropdownEntry
                                        .copyOf(d)
                                        .withQuestionId(targetQuestionId)));
                });

        System.out.println("done");


    }

    private static Set<Tuple2<Long, ImmutableSurveyQuestion>> dupeSectionSurveyQuestions(List<SurveyQuestion> sourceQuestions,
                                                                                         long targetTemplateId,
                                                                                         Set<SectionDupeConfig> sections) {

        Set<String> sectionNames = map(sections, SectionDupeConfig::sectionName);

        Set<SurveyQuestion> questionsToDupe = filter(
                sourceQuestions,
                q -> q.sectionName()
                        .map(sectionNames::contains)
                        .orElse(false));

        Map<String, Collection<SurveyQuestion>> questionsBySourceSection = groupBy(
                questionsToDupe,
                q -> q.sectionName().orElse(null));

        AtomicInteger sectionCounter = new AtomicInteger(1);

        return sections
                .stream()
                .flatMap(section -> {
                    Collection<SurveyQuestion> sourceSectionQuestions = questionsBySourceSection.getOrDefault(
                            section.sectionName(),
                            emptySet());

                    return section
                            .targets()
                            .stream()
                            .map(targetSection -> tuple(
                                    sectionCounter.getAndIncrement(),
                                    targetSection,
                                    sourceSectionQuestions));
                })
                .flatMap(t -> {
                    Integer sectionNumber = t.v1;
                    TargetSectionConfig targetSection = t.v2;
                    Collection<SurveyQuestion> questions = t.v3;

                    Set<String> inScopeQuestionExtIds = fromOptionals(map(
                            questions,
                            SurveyQuestion::externalId));

                    return questions
                            .stream()
                            .map(q -> tuple(
                                    q.id().get(),
                                    ImmutableSurveyQuestion
                                        .copyOf(q)
                                        .withSurveyTemplateId(targetTemplateId)
                                        .withId(Optional.empty())
                                        .withSectionName(targetSection.name())
                                        .withPosition(q.position() + (1000 * sectionNumber))
                                        .withExternalId(q
                                                .externalId()
                                                .map(extId -> mkExtId(
                                                        targetSection.extIdPrefix(),
                                                        extId)))
                                        .withInclusionPredicate(q
                                                .inclusionPredicate()
                                                .map(pred -> fixupInclusionPredicate(
                                                        pred,
                                                        inScopeQuestionExtIds,
                                                        targetSection.extIdPrefix())))));
                })
                .collect(Collectors.toSet());
    }


    private static Set<Tuple2<Long, ImmutableSurveyQuestion>> dupeBasicSurveyQuestions(List<SurveyQuestion> surveyQuestions,
                                                                                       long targetTemplateId,
                                                                                       Set<String> sectionsToOmit) {

        Set<SurveyQuestion> questionsToDupeWithoutRepeating = filter(
                surveyQuestions,
                q -> q.sectionName()
                        .map(sn -> !sectionsToOmit.contains(sn))
                        .orElse(false));

        return questionsToDupeWithoutRepeating
                .stream()
                .map(q -> tuple(
                        q.id().get(),
                        ImmutableSurveyQuestion
                            .copyOf(q)
                            .withSurveyTemplateId(targetTemplateId)
                            .withId(Optional.empty())))
                .collect(Collectors.toSet());

    }


    private static Long findSourceTemplateId(DSLContext dsl, SurveyDupeConfig dupeConfig) {
        return dsl
                .select(st.ID)
                .from(st)
                .where(st.EXTERNAL_ID.eq(dupeConfig.sourceSurveyExternalId()))
                .fetchOptional()
                .map(r -> r.get(st.ID))
                .orElseThrow(() -> new IllegalStateException("Cannot find the source survey"));
    }


    private static void ensureTargetDoesNotExist(DSLContext dsl, SurveyDupeConfig dupeConfig) {
        dsl.select(st.ID)
                .from(st)
                .where(st.EXTERNAL_ID.eq(dupeConfig.targetSurveyExternalId()))
                .fetchOptional()
                .ifPresent(unchecked((id) -> {
                    throw new IllegalStateException("Cannot create target survey as it already exists ");
                }));
    }



    private static long dupeBasicSurveyRecord(SurveyTemplateDao templateDao, SurveyDupeConfig cfg, Long sourceTemplateId) {
        ImmutableSurveyTemplate target = ImmutableSurveyTemplate
                .copyOf(templateDao.getById(sourceTemplateId))
                .withCreatedAt(DateTimeUtilities.nowUtc())
                .withStatus(ReleaseLifecycleStatus.DRAFT)
                .withName(cfg.targetSurveyName())
                .withExternalId(cfg.targetSurveyExternalId());

        return templateDao.create(target);
    }


    private static String mkExtId(String prefix, String existing) {
        return String.format("%s_%s", prefix, existing);
    }


    private static String fixupInclusionPredicate(String inclusionPredicate,
                                                  Set<String> inScopeQuestionExtIds,
                                                  String extIdPrefix) {
        return inScopeQuestionExtIds
                .stream()
                .reduce(inclusionPredicate,
                        (acc, qExtId) -> acc.replace(
                                qExtId,
                                mkExtId(extIdPrefix, qExtId)));
    }


    private static SurveyDupeConfig prepConfig() {
        return ImmutableSurveyDupeConfig
                .builder()
                .sourceSurveyExternalId("KD_REVIEW_TEMPLATE")
                .targetSurveyName("KD Review 2022")
                .targetSurveyExternalId("KD_REVIEW_2022")
                .addSections(
                        ImmutableSectionDupeConfig
                            .builder()
                            .sectionName("EA Principles")
                            .addTargets(
                                    mkTargetSection("AUTHSO", "Authorised Sources publish quality data"),
                                    mkTargetSection("AUTHCO", "Consumers use data from Authorised Sources without changing it"),
                                    mkTargetSection("REFDAT", "All business activities use reference data from Authorised Sources as the starting point"),
                                    mkTargetSection("BRINGPR", "Bring the processing to the data"),
                                    mkTargetSection("PLATPROC", "Platforms and processes support using data in new ways, with automated controls"),
                                    mkTargetSection("USETECH", "Reuse where possible, extend if not - Technology Products"),
                                    mkTargetSection("USEAPP", "Reuse where possible, extend if not - Applications/Services"),
                                    mkTargetSection("USESOFT", "Reuse where possible, extend if not - Software"),
                                    mkTargetSection("REDTECH", "Reduce complexity - Technology Products"),
                                    mkTargetSection("REDAPP", "Reduce complexity - Applications / Services"),
                                    mkTargetSection("REDSOFT", "Reduce complexity - Software"),
                                    mkTargetSection("DEVOPS", "Adopt DevOps Principles"),
                                    mkTargetSection("CLOUD", "Cloud native"),
                                    mkTargetSection("API", "API First"),
                                    mkTargetSection("PRODRES", "Automate production resilience"),
                                    mkTargetSection("TARLOC", "Build Functions in Target State Locations"),
                                    mkTargetSection("STANSEC", "Use of standard / strategic security solutions"),
                                    mkTargetSection("SECDES", "Secure by Design"))
                            .build())
                .build();
    }
}
