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

package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.ServerComplexityDao;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;

@Service
public class ServerComplexityService {

    private final ServerComplexityDao serverComplexityDao;


    @Autowired
    public ServerComplexityService(ServerComplexityDao serverComplexityDao) {
        this.serverComplexityDao = serverComplexityDao;
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector) {
        int baseline = serverComplexityDao.findBaseline();
        return findByAppIdSelector(idSelector, baseline);

    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector, int baseline) {
        return serverComplexityDao.findCountsByAppIdSelector(idSelector)
                .stream()
                .map(tally -> tallyToComplexityScore(
                        ComplexityKind.SERVER,
                        tally,
                        baseline,
                        Math::log))
                .collect(Collectors.toList());
    }


    public ComplexityScore getForApp(long appId) {
        int baseline = serverComplexityDao.findBaseline();
        return getForApp(appId, baseline);
    }


    public ComplexityScore getForApp(long appId, int baseline) {
        List<Tally<Long>> tallies = serverComplexityDao.findCountsByAppIdSelector(DSL.select(DSL.value(appId)));
        if (tallies.isEmpty()) { return null; }

        return tallyToComplexityScore(
                ComplexityKind.SERVER,
                tallies.get(0),
                baseline,
                Math::log);
    }

}
