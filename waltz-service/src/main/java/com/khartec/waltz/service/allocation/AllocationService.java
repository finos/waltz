/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.service.allocation;

import com.khartec.waltz.data.allocation.AllocationDao;
import com.khartec.waltz.data.allocation_scheme.AllocationSchemeDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation_scheme.AllocationScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllocationService {

    private final AllocationDao allocationDao;


    @Autowired
    public AllocationService(AllocationDao allocationDao) {
        this.allocationDao = allocationDao;
    }


    public List<Allocation> findByEntityAndScheme(EntityReference ref,
                                                  long schemeId) {
        return allocationDao.findByEntityAndScheme(ref, schemeId);
    }


    public List<Allocation> findByMeasurableAndScheme(long measurableId,
                                                      long schemeId){
        return allocationDao.findByMeasurableIdAndScheme(measurableId, schemeId);
    }

}
