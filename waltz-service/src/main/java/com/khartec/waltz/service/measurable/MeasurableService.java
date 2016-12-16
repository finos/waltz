/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.measurable;

import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.measurable.Measurable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class MeasurableService {
    
    private final MeasurableDao measurableDao;


    @Autowired
    public MeasurableService(MeasurableDao measurableDao) {
        checkNotNull(measurableDao, "measurableDao cannot be null");
        this.measurableDao = measurableDao;
    }


    public List<Measurable> findAll() {
        return measurableDao.findAll();
    }

}
