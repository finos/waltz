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
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class MeasurableService {
    
    private final MeasurableDao measurableDao;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory;


    @Autowired
    public MeasurableService(MeasurableDao measurableDao,
                             MeasurableIdSelectorFactory measurableIdSelectorFactory) {
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableIdSelectorFactory, "measurableIdSelectorFactory cannot be null");
        this.measurableDao = measurableDao;
        this.measurableIdSelectorFactory = measurableIdSelectorFactory;
    }


    public List<Measurable> findAll() {
        return measurableDao.findAll();
    }


    /**
     * Includes parents, this should probably be deprecated and rolled into findByMeasureableIdSelector
     * @param ref
     * @return
     */
    public List<Measurable> findMeasurablesRelatedToEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return measurableDao.findMeasuresRelatedToEntity(ref);
    }


    public List<Measurable> findByMeasurableIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableDao.findByMeasurableIdSelector(selector);
    }

}
