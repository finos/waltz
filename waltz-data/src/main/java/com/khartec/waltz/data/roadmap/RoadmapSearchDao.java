/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.data.roadmap;

import com.khartec.waltz.data.SearchUtilities;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.roadmap.Roadmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.PredicateUtilities.all;
import static java.util.stream.Collectors.toList;


@Repository
public class RoadmapSearchDao {

    private final RoadmapDao roadmapDao;


    @Autowired
    public RoadmapSearchDao(RoadmapDao roadmapDao) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        this.roadmapDao = roadmapDao;
    }


    public List<Roadmap> search(EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery().toLowerCase());
        List<Roadmap> collect = roadmapDao.findAll()
                .stream()
                .filter(roadmap -> {
                    if (!options.entityLifecycleStatuses().contains(roadmap.entityLifecycleStatus())) {
                        return false;
                    }

                    String s = (roadmap.name() + " " + roadmap.description()).toLowerCase();
                    return all(
                            terms,
                            t -> s.indexOf(t) > -1);
                })
                .limit(options.limit())
                .collect(toList());

        return collect;
    }

}
