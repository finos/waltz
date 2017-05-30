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

import com.khartec.waltz.model.measurable_relationship.MeasurableRelationship;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MeasurableRelationshipService {



    public MeasurableRelationshipService() {
    }


    public List<MeasurableRelationship> findForMeasurable(long measurableId) {
        return Collections.emptyList();
    }


    public int remove(long measurable1, long measurable2) {
        return 0;
    }

    public boolean save(MeasurableRelationship measurableRelationship) {
        return false;
    }
}
