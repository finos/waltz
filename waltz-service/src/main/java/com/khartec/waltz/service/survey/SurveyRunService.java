package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.IdSelectorFactoryProvider;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyInstanceRecipientDao;
import com.khartec.waltz.data.survey.SurveyRunDao;
import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.SetUtilities.fromCollection;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class SurveyRunService {

    private final ChangeLogService changeLogService;
    private final IdSelectorFactoryProvider idSelectorFactoryProvider;
    private final InvolvementDao involvementDao;
    private final PersonDao personDao;
    private final SurveyInstanceDao surveyInstanceDao;
    private final SurveyInstanceRecipientDao surveyInstanceRecipientDao;
    private final SurveyRunDao surveyRunDao;
    private final SurveyTemplateDao surveyTemplateDao;


    @Autowired
    public SurveyRunService(ChangeLogService changeLogService,
                            IdSelectorFactoryProvider idSelectorFactoryProvider,
                            InvolvementDao involvementDao,
                            PersonDao personDao,
                            SurveyInstanceDao surveyInstanceDao,
                            SurveyInstanceRecipientDao surveyInstanceRecipientDao,
                            SurveyRunDao surveyRunDao,
                            SurveyTemplateDao surveyTemplateDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(idSelectorFactoryProvider, "idSelectorFactoryProvider cannot be null");
        checkNotNull(involvementDao, "involvementDao cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyInstanceDao, "surveyInstanceDao cannot be null");
        checkNotNull(surveyInstanceRecipientDao, "surveyInstanceRecipientDao cannot be null");
        checkNotNull(surveyRunDao, "surveyRunDao cannot be null");
        checkNotNull(surveyTemplateDao, "surveyTemplateDao cannot be null");

        this.changeLogService = changeLogService;
        this.idSelectorFactoryProvider = idSelectorFactoryProvider;
        this.involvementDao = involvementDao;
        this.personDao = personDao;
        this.surveyInstanceDao = surveyInstanceDao;
        this.surveyInstanceRecipientDao = surveyInstanceRecipientDao;
        this.surveyRunDao = surveyRunDao;
        this.surveyTemplateDao = surveyTemplateDao;
    }


    public IdCommandResponse createSurveyRun(String userName, SurveyRunCreateCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "create command cannot be null");

        Person owner = personDao.getByUserName(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved");

        long surveyRunId = surveyRunDao.create(owner.id().get(), command);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: " + command.name() + " added")
                        .build());

        return ImmutableIdCommandResponse.builder()
                .id(surveyRunId)
                .build();
    }


    public int updateSurveyRun(String userName, long surveyRunId, SurveyRunChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "change command cannot be null");

        validateSurveyRunUpdate(userName, surveyRunId);

        return surveyRunDao.update(surveyRunId, command);
    }


    public int updateSurveyRunStatus(String userName, long surveyRunId, SurveyRunStatus newStatus) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(newStatus, "newStatus cannot be null");

        validateSurveyRunUpdate(userName, surveyRunId);

        int result = (newStatus == SurveyRunStatus.ISSUED)
                ? surveyRunDao.issue(surveyRunId)
                : surveyRunDao.updateStatus(surveyRunId, newStatus);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: status changed to " + newStatus)
                        .build());

        return result;
    }


    public List<SurveyInstanceRecipient> generateSurveyInstanceRecipients(long surveyRunId) {
        SurveyRun surveyRun = surveyRunDao.getById(surveyRunId);
        checkNotNull(surveyRun, "surveyRun " + surveyRunId + " not found");

        SurveyTemplate surveyTemplate = surveyTemplateDao.getById(surveyRun.surveyTemplateId());
        checkNotNull(surveyTemplate, "surveyTemplate " + surveyRun.surveyTemplateId() + " not found");

        IdSelectorFactory idSelectorFactory = idSelectorFactoryProvider.getForKind(surveyTemplate.targetEntityKind());

        Select<Record1<Long>> idSelector = idSelectorFactory.apply(surveyRun.selectionOptions());
        Map<EntityReference, List<Person>> entityRefToPeople = involvementDao.findPeopleByEntitySelectorAndInvolvement(
                surveyTemplate.targetEntityKind(),
                idSelector,
                surveyRun.involvementKindIds());

        return entityRefToPeople.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream()
                        .map(p -> ImmutableSurveyInstanceRecipient.builder()
                                .surveyInstance(ImmutableSurveyInstance.builder()
                                        .surveyEntity(e.getKey())
                                        .surveyRunId(surveyRun.id().get())
                                        .status(SurveyInstanceStatus.NOT_STARTED)
                                        .build())
                                .person(p)
                                .build()))
                .collect(toList());
    }


    public boolean createSurveyInstancesAndRecipients(long surveyRunId,
                                                   List<SurveyInstanceRecipient> excludedRecipients) {
        SurveyRun surveyRun = surveyRunDao.getById(surveyRunId);
        checkNotNull(surveyRun, "surveyRun " + surveyRunId + " not found");

        Set<SurveyInstanceRecipient> excludedRecipientSet = fromCollection(excludedRecipients);
        List<SurveyInstanceRecipient> surveyInstanceRecipients = generateSurveyInstanceRecipients(surveyRunId).stream()
                .filter(r -> !excludedRecipientSet.contains(r))
                .collect(toList());

        Map<SurveyInstance, List<SurveyInstanceRecipient>> instancesAndRecipientsToSave = surveyInstanceRecipients.stream()
                .collect(groupingBy(
                        SurveyInstanceRecipient::surveyInstance,
                        toList()
                ));


        // delete existing instances and recipients
        deleteSurveyInstancesAndRecipients(surveyRunId);

        // insert new instances and recipients
        instancesAndRecipientsToSave.forEach(
                (k,v) -> {
                    if (surveyRun.issuanceKind() == SurveyIssuanceKind.GROUP) {
                        // one instance per group
                        long instanceId = createSurveyInstance(k);
                        v.forEach(r -> createSurveyInstanceRecipient(instanceId, r));
                    } else {
                        // one instance for each individual
                        v.forEach(r -> {
                            long instanceId = createSurveyInstance(k);
                            createSurveyInstanceRecipient(instanceId, r);
                        });
                    }
                }
        );

        return true;
    }


    private void deleteSurveyInstancesAndRecipients(long surveyRunId) {
        surveyInstanceRecipientDao.deleteForSurveyRun(surveyRunId);
        surveyInstanceDao.deleteForSurveyRun(surveyRunId);
    }


    private long createSurveyInstance(SurveyInstance surveyInstance) {
        return surveyInstanceDao.create(ImmutableSurveyInstanceCreateCommand.builder()
                .surveyRunId(surveyInstance.surveyRunId())
                .entityReference(surveyInstance.surveyEntity())
                .status(surveyInstance.status())
                .build());
    }


    private long createSurveyInstanceRecipient(long surveyInstanceId, SurveyInstanceRecipient surveyInstanceRecipient) {
        return surveyInstanceRecipientDao.create(ImmutableSurveyInstanceRecipientCreateCommand.builder()
                .surveyInstanceId(surveyInstanceId)
                .personId(surveyInstanceRecipient.person().id().get())
                .build());
    }


    private void validateSurveyRunUpdate(String userName, long surveyRunId) {
        Person owner = personDao.getByUserName(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved");

        SurveyRun surveyRun = surveyRunDao.getById(surveyRunId);
        checkNotNull(surveyRun, "surveyRun " + surveyRunId + " not found");

        checkTrue(Objects.equals(surveyRun.ownerId(), owner.id().get()), "Permission denied");

        checkTrue(surveyRun.status() == SurveyRunStatus.DRAFT, "survey run can only be updated when it's still in DRAFT mode");
    }
}
