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

package com.khartec.waltz.service.measurable_category;

import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class MeasurableCategoryService {

    private final MeasurableCategoryDao measurableCategoryDao;


    @Autowired
    public MeasurableCategoryService(MeasurableCategoryDao measurableCategoryDao) {
        this.measurableCategoryDao = measurableCategoryDao;
    }

    public Collection<MeasurableCategory> findAll() {
        return measurableCategoryDao.findAll();
    }

    public MeasurableCategory getById(long id) {
        return measurableCategoryDao.getById(id);
    }
}
