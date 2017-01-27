package com.khartec.waltz.data.survey;

import com.khartec.waltz.model.survey.SurveyInstanceRecipientCreateCommand;
import com.khartec.waltz.schema.tables.records.SurveyInstanceRecipientRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE_RECIPIENT;

@Repository
public class SurveyInstanceRecipientDao {

    private final DSLContext dsl;


    @Autowired
    public SurveyInstanceRecipientDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }



    public long create(SurveyInstanceRecipientCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SurveyInstanceRecipientRecord record = dsl.newRecord(SURVEY_INSTANCE_RECIPIENT);
        record.setSurveyInstanceId(command.surveyInstanceId());
        record.setPersonId(command.personId());

        record.store();
        return record.getId();
    }


    public void deleteForSurveyRun(long surveyRunId) {
        Select<Record1<Long>> surveyInstanceIdSelector = dsl.select(SURVEY_INSTANCE.ID)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId));

        dsl.delete(SURVEY_INSTANCE_RECIPIENT)
                .where(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.in(surveyInstanceIdSelector));
    }
}
