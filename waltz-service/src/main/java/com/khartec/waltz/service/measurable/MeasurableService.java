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

package com.khartec.waltz.service.measurable;

import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable.search.MeasurableSearchDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class MeasurableService {
    
    private final MeasurableDao measurableDao;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory;
    private final MeasurableSearchDao measurableSearchDao;


    @Autowired
    public MeasurableService(MeasurableDao measurableDao,
                             MeasurableIdSelectorFactory measurableIdSelectorFactory,
                             MeasurableSearchDao measurableSearchDao) {
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableIdSelectorFactory, "measurableIdSelectorFactory cannot be null");
        checkNotNull(measurableSearchDao, "measurableSearchDao cannot be null");

        this.measurableDao = measurableDao;
        this.measurableIdSelectorFactory = measurableIdSelectorFactory;
        this.measurableSearchDao = measurableSearchDao;
    }


    public List<Measurable> findAll() {
        return measurableDao.findAll();
    }


    /**
     * Includes parents, this should probably be deprecated and rolled into findByMeasureableIdSelector
     * @param ref Entity reference of item to search against
     * @return List of measurable related to the given entity `ref`
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


    public Collection<Measurable> search(String query) {
        return search(query, EntitySearchOptions.mkForEntity(EntityKind.MEASURABLE));
    }


    public Collection<Measurable> search(String query, EntitySearchOptions options) {
        return measurableSearchDao.search(query, options);
    }


    public Collection<Measurable> findByExternalId(String extId) {
        return measurableDao.findByExternalId(extId);
    }


    public Measurable getById(long id) {
        return measurableDao.getById(id);
    }


    public boolean updateConcreteFlag(Long id, boolean newValue) {
        return measurableDao.updateConcreteFlag(id, newValue);
    }
}
