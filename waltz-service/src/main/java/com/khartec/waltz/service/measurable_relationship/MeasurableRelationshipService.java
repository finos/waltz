/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.measurable_relationship;

import com.khartec.waltz.data.measurable_relationship.MeasurableRelationshipDao;
import com.khartec.waltz.model.measurable_relationship.MeasurableRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class MeasurableRelationshipService {

    private final MeasurableRelationshipDao measurableRelationshipDao;


    @Autowired
    public MeasurableRelationshipService(MeasurableRelationshipDao measurableRelationshipDao) {
        checkNotNull(measurableRelationshipDao, "measurableRelationshipDao cannot be null");

        this.measurableRelationshipDao = measurableRelationshipDao;
    }


    public List<MeasurableRelationship> findForMeasurable(long measurableId) {
        return measurableRelationshipDao.findForMeasurable(measurableId);
    }


    public int remove(long measurable1, long measurable2) {
        checkFalse(measurable1 == measurable2, "Cannot relate a measurable to itself");
        return measurableRelationshipDao.remove(measurable1, measurable2);
    }

    public boolean save(MeasurableRelationship measurableRelationship) {
        checkNotNull(measurableRelationship, "measurableRelationship cannot be null");
        return measurableRelationshipDao.save(measurableRelationship);

    }
}
