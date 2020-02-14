package com.khartec.waltz.service.measurable_rating_planned_decommission;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.exception.UpdateFailedException;
import com.khartec.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.DateFieldChange;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.lang.String.format;

@Service
public class MeasurableRatingPlannedDecommissionService {

    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final ChangeLogService changeLogService;

    @Autowired
    public MeasurableRatingPlannedDecommissionService(MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                                                      ChangeLogService changeLogService){
        checkNotNull(measurableRatingPlannedDecommissionDao, "MeasurableRatingPlannedDecommissionDao cannot be null");
        checkNotNull(changeLogService, "ChangeLogService cannot be null");
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.changeLogService = changeLogService;
    }


    public Collection<MeasurableRatingPlannedDecommission> findForEntityRef(EntityReference ref){
        return measurableRatingPlannedDecommissionDao.fetchByEntityRef(ref);
    }


    public MeasurableRatingPlannedDecommission save(EntityReference entityReference, long measurableId, DateFieldChange dateChange, String userName) {
        boolean success = measurableRatingPlannedDecommissionDao.save(
                entityReference,
                measurableId,
                dateChange,
                userName);

        if (! success) {
            throw new UpdateFailedException(
                    "DECOM_DATE_SAVE_FAILED",
                    format("Failed to store date change for entity %s:%d and measurable %d",
                            entityReference.kind(),
                            entityReference.id(),
                            measurableId));
        }


        changeLogService.write(ImmutableChangeLog.builder()
                .message(format("Saved planned decommission date {%s} for measurable: %d", dateChange.newVal().toString(), measurableId))
                .parentReference(entityReference)
                .userId(userName)
                .createdAt(DateTimeUtilities.nowUtc())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.MEASURABLE_RATING)
                .operation(Operation.UNKNOWN)
                .build());

        return measurableRatingPlannedDecommissionDao.getByEntityAndMeasurable(entityReference, measurableId);
    }

    public Boolean remove(Long id){
        return measurableRatingPlannedDecommissionDao.remove(id);
    }
}
