package com.khartec.waltz.service.measurable_rating_replacement;


import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class MeasurableRatingReplacementService {

    private final MeasurableRatingReplacementDao measurableRatingReplacementDao;

    @Autowired
    public MeasurableRatingReplacementService(MeasurableRatingReplacementDao measurableRatingReplacementDao){
        Checks.checkNotNull(measurableRatingReplacementDao, "measurableRatingReplacementDao cannot be null");
        this.measurableRatingReplacementDao = measurableRatingReplacementDao;
    }


    public Collection<MeasurableRatingReplacement> findForEntityRef(EntityReference ref){
        return measurableRatingReplacementDao.fetchByEntityRef(ref);
    }

}
