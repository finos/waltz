/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.measurable_rating_replacement;


import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

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


    public Set<MeasurableRatingReplacement> save(long decommId,
                        EntityReference entityReference,
                        LocalDate commissionDate,
                        String username) {

        measurableRatingReplacementDao.save(decommId, entityReference, commissionDate, username);
        return measurableRatingReplacementDao.fetchByDecommissionId(decommId);
    }


    public Collection<MeasurableRatingReplacement> remove(long decommId, long replacementId, String username) {
        measurableRatingReplacementDao.remove(decommId, replacementId);
        return measurableRatingReplacementDao.fetchByDecommissionId(decommId);
    }
}
