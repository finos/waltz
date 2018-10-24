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

package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.MeasurableComplexityDao;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexityScore;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;


@Service
public class MeasurableComplexityService {

    private final MeasurableComplexityDao measurableComplexityDao;


    @Autowired
    public MeasurableComplexityService(MeasurableComplexityDao measurableComplexityDao) {
        checkNotNull(measurableComplexityDao, "measurableComplexityDao cannot be null");
        this.measurableComplexityDao = measurableComplexityDao;
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector) {
        Double baseline = measurableComplexityDao.calculateBaseline();
        return findByAppIdSelector(idSelector, baseline);
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector, double baseline) {
        return measurableComplexityDao.findScoresForAppIdSelector(idSelector)
                .stream()
                .map(tally -> tallyToComplexityScore(
                        ComplexityKind.MEASURABLE,
                        tally,
                        baseline))
                .collect(Collectors.toList());
    }

}
