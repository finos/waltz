package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.involvement.EntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement.ImmutableEntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement_kind.ImmutableInvolvementKindCreateCommand;
import org.finos.waltz.model.involvement_kind.InvolvementKindCreateCommand;
import org.finos.waltz.model.survey.SurveyInstanceAction;
import org.finos.waltz.model.survey.SurveyInstanceActionStatus;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.schema.tables.records.SurveyInstanceActionQueueRecord;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.involvement_kind.InvolvementKindService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.SURVEY_INSTANCE_ACTION_QUEUE;

@Service
public class ActionQueueHelper {

    @Autowired
    private DSLContext dsl;

    @Autowired
    public ActionQueueHelper() {
    }

    public Long addActionToQueue(Long surveyInstanceId, SurveyInstanceAction action, String actionParams, SurveyInstanceStatus initialState, String submitter) {
        SurveyInstanceActionQueueRecord r = dsl.newRecord(SURVEY_INSTANCE_ACTION_QUEUE);
        r.setSurveyInstanceId(surveyInstanceId);
        r.setAction(action.name());
        r.setActionParams(actionParams);
        r.setInitialState(initialState.name());
        r.setStatus(SurveyInstanceActionStatus.PENDING.name());
        r.setSubmittedBy(submitter);
        r.setProvenance("ActionQueueHelper");
        r.insert();
        return r.getId();
    }
}
