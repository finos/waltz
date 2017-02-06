package com.khartec.waltz.jobs.sample;


import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.involvement_kind.InvolvementKindDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.involvement_kind.InvolvementKind;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.SurveyIssuanceKind;
import com.khartec.waltz.model.survey.SurveyRunStatus;
import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.schema.tables.records.SurveyRunRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.survey.SurveyRunService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.schema.Tables.SURVEY_RUN;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.joining;

/**
 * Generates random survey runs and associated instances and recipients
 */
public class SurveyRunGenerator {
    private static final int NUMBER_OF_RUNS = 20;
    private static final int MAX_INVOLVEMENT_KINDS_PER_RUN = 3;
    private static final int MAX_SURVEY_AGE_IN_DAYS = 30;
    private static final int MAX_SURVEY_LIFESPAN_IN_DAYS = 120;

    private static final String ID_SEPARATOR = ";";

    private static final Random random = new Random();

    private static final String[] SURVEY_RUN_PREFIXES = {"ANNUAL", "Q1", "Q2", "Q3", "Q4"};
    public static void main(String[] args) {

        try {
            final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
            final DSLContext dsl = ctx.getBean(DSLContext.class);

            final SurveyTemplateDao surveyTemplateDao = ctx.getBean(SurveyTemplateDao.class);
            List<SurveyTemplate> surveyTemplates = surveyTemplateDao.findAllActive();
            checkFalse(isEmpty(surveyTemplates), "No template found, please generate templates first");

            final AppGroupDao appGroupDao = ctx.getBean(AppGroupDao.class);
            List<AppGroup> appGroups = appGroupDao.findPublicGroups();
            checkFalse(isEmpty(appGroups), "No public app group found, please generate app groups first");

            List<Person> owners = dsl.selectFrom(PERSON)
                    .limit(NUMBER_OF_RUNS)
                    .fetch()
                    .map(PersonDao.personMapper);
            checkFalse(isEmpty(owners), "No person found, please generate person data first");

            final InvolvementKindDao involvementKindDao = ctx.getBean(InvolvementKindDao.class);
            List<InvolvementKind> involvementKinds = involvementKindDao.findAll();

            final SurveyRunService surveyRunService = ctx.getBean(SurveyRunService.class);

            IntStream.range(0, NUMBER_OF_RUNS).forEach(idx -> {
                SurveyTemplate surveyTemplate = surveyTemplates.get(random.nextInt(surveyTemplates.size()));
                Person owner = owners.get(random.nextInt(owners.size()));

                SurveyRunRecord surveyRunRecord = mkRandomSurveyRunRecord(dsl, appGroups, involvementKinds, surveyTemplate, owner);

                surveyRunRecord.store();
                long surveyRunId = surveyRunRecord.getId();

                surveyRunService.createSurveyInstancesAndRecipients(surveyRunId, Collections.emptyList());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SurveyRunRecord mkRandomSurveyRunRecord(DSLContext dsl,
                                                           List<AppGroup> appGroups,
                                                           List<InvolvementKind> involvementKinds,
                                                           SurveyTemplate surveyTemplate,
                                                           Person owner) {
        SurveyRunRecord surveyRunRecord = dsl.newRecord(SURVEY_RUN);
        surveyRunRecord.setOwnerId(owner.id().get());
        surveyRunRecord.setContactEmail(owner.email());
        surveyRunRecord.setSurveyTemplateId(surveyTemplate.id().get());
        surveyRunRecord.setName(surveyTemplate.name() + " " + SURVEY_RUN_PREFIXES[random.nextInt(SURVEY_RUN_PREFIXES.length)]);
        surveyRunRecord.setDescription(surveyTemplate.description());
        surveyRunRecord.setSelectorEntityKind(EntityKind.APP_GROUP.name());
        surveyRunRecord.setSelectorEntityId(appGroups.get(random.nextInt(appGroups.size())).id().get());
        surveyRunRecord.setSelectorHierarchyScope(HierarchyQueryScope.EXACT.name());

        Collections.shuffle(involvementKinds, random);
        surveyRunRecord.setInvolvementKindIds(involvementKinds.stream()
                .limit(random.nextInt(MAX_INVOLVEMENT_KINDS_PER_RUN) + 1)
                .map(kind -> kind.id().get().toString())
                .collect(joining(ID_SEPARATOR)));

        surveyRunRecord.setIssuanceKind(getRandomEnum(SurveyIssuanceKind.class, random).name());
        LocalDate issuedOn = LocalDate.now().minusDays(random.nextInt(MAX_SURVEY_AGE_IN_DAYS));
        surveyRunRecord.setIssuedOn(java.sql.Date.valueOf(issuedOn));
        LocalDate dueDate = random.nextBoolean()
                ? issuedOn.plusDays(random.nextInt(MAX_SURVEY_LIFESPAN_IN_DAYS))
                : null;
        surveyRunRecord.setDueDate(dueDate == null? null : java.sql.Date.valueOf(dueDate));
        surveyRunRecord.setStatus(SurveyRunStatus.ISSUED.name());

        return surveyRunRecord;
    }


    private static <E extends Enum<E>> E getRandomEnum(Class<E> enumClass, Random random) {
        List<E> values = new ArrayList<E>(EnumSet.allOf(enumClass));
        checkFalse(isEmpty(values), "No values found for enum " + enumClass.getSimpleName());

        return values.get(random.nextInt(values.size()));
    }
}
