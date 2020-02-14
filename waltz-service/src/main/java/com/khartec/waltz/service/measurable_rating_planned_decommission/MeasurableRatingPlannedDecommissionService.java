package com.khartec.waltz.service.measurable_rating_planned_decommission;

import com.khartec.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class MeasurableRatingPlannedDecommissionService {

    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;

    @Autowired
    public MeasurableRatingPlannedDecommissionService(MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao){
        checkNotNull(measurableRatingPlannedDecommissionDao, "MeasurableRatingPlannedDecommissionDao cannot be null");
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
    }


    public Collection<MeasurableRatingPlannedDecommission> findForEntityRef(EntityReference ref){
        return measurableRatingPlannedDecommissionDao.fetchByEntityRef(ref);
    }


}
