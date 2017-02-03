package com.khartec.waltz.jobs.sample;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.survey.SurveyQuestionFieldType;
import com.khartec.waltz.model.survey.SurveyTemplateStatus;
import com.khartec.waltz.schema.tables.Person;
import com.khartec.waltz.schema.tables.records.PersonRecord;
import com.khartec.waltz.schema.tables.records.SurveyQuestionRecord;
import com.khartec.waltz.schema.tables.records.SurveyTemplateRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.Tables.SURVEY_QUESTION;
import static com.khartec.waltz.schema.Tables.SURVEY_TEMPLATE;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Comparator.comparing;

public class SurveyTemplateGenerator {
    private final static int MIN_QUESTIONS_PER_SURVEY   = 10;
    private final static int MAX_QUESTIONS_PER_SURVEY   = 40;

    private static final Random random = new Random();


    public static void main(String[] args) {

        try {
            final List<List<String>> csvTemplates = readFromCsv("surveys.csv");
            final List<List<String>> csvQuestions = readFromCsv("questions.csv");

            final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
            final DSLContext dsl = ctx.getBean(DSLContext.class);

            List<Long> ownerIds = dsl.selectFrom(Person.PERSON)
                    .limit(csvTemplates.size())
                    .fetch()
                    .map(PersonRecord::getId);

            csvTemplates.forEach(template -> {
                SurveyTemplateRecord surveyTemplateRecord = dsl.newRecord(SURVEY_TEMPLATE);
                surveyTemplateRecord.setName(template.get(0));
                surveyTemplateRecord.setDescription(template.get(1));
                surveyTemplateRecord.setTargetEntityKind(getRandomEntityKind().name());
                surveyTemplateRecord.setOwnerId(ownerIds.get(random.nextInt(ownerIds.size())));
                surveyTemplateRecord.setStatus(SurveyTemplateStatus.ACTIVE.name());

                surveyTemplateRecord.store();
                Long surveyTemplateId = surveyTemplateRecord.getId();

                List<SurveyQuestionRecord> surveyQuestionRecords = generateSurveyQuestions(
                        dsl, surveyTemplateId, csvQuestions);

                dsl.batchInsert(surveyQuestionRecords).execute();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static List<SurveyQuestionRecord> generateSurveyQuestions(DSLContext dsl,
                                                                      long surveyTemplateId,
                                                                      List<List<String>> questions) {
        int numOfQuestions = random.nextInt(
                MAX_QUESTIONS_PER_SURVEY - MIN_QUESTIONS_PER_SURVEY) + MIN_QUESTIONS_PER_SURVEY;

        final AtomicInteger position = new AtomicInteger(1);

        return random.ints(numOfQuestions, 0, questions.size())
                .mapToObj(questions::get)
                .sorted(comparing(q -> q.get(0)))    // section
                .map(q -> {
                    if (q.size() < 3) {
                        System.out.print(q);
                    }
                    SurveyQuestionRecord surveyQuestionRecord = dsl.newRecord(SURVEY_QUESTION);
                    surveyQuestionRecord.setAllowComment(random.nextBoolean());
                    surveyQuestionRecord.setFieldType(getRandomFieldType().name());
                    if (random.nextBoolean()) {
                        surveyQuestionRecord.setHelpText(q.get(2));     // help
                    }
                    surveyQuestionRecord.setIsMandatory(random.nextBoolean());
                    surveyQuestionRecord.setQuestionText(q.get(1));     // question
                    surveyQuestionRecord.setSectionName(q.get(0));      // section
                    surveyQuestionRecord.setSurveyTemplateId(surveyTemplateId);
                    surveyQuestionRecord.setPosition(position.getAndIncrement());
                    return surveyQuestionRecord;
                })
                .collect(Collectors.toList());

    }


    private static SurveyQuestionFieldType getRandomFieldType() {
        SurveyQuestionFieldType[] allTypes = SurveyQuestionFieldType.values();
        return allTypes[random.nextInt(allTypes.length)];
    }


    private static EntityKind getRandomEntityKind() {
        final EntityKind[] possibleEntityKinds = {EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE};
        return possibleEntityKinds[random.nextInt(possibleEntityKinds.length)];
    }


    private static List<List<String>> readFromCsv(String filename) throws IOException, URISyntaxException {
        URL classPathResource = getSystemClassLoader().getResource(filename);
        if (classPathResource == null) {
            throw new FileNotFoundException(filename);
        }

        Path filePath = Paths.get(classPathResource.toURI());
        try (CsvListReader csvReader = new CsvListReader(Files.newBufferedReader(filePath), CsvPreference.STANDARD_PREFERENCE)) {

            csvReader.getHeader(true);  // skip header

            final List<List<String>> rows = new ArrayList<>();
            List<String> colValues;
            while ((colValues = csvReader.read()) != null) {
                rows.add(colValues);
            }

            return rows;
        }
    }

}


