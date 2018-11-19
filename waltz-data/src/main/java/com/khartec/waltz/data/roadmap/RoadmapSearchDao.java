package com.khartec.waltz.data.roadmap;

import com.khartec.waltz.data.SearchUtilities;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.roadmap.Roadmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.PredicateUtilities.all;
import static com.khartec.waltz.common.StringUtilities.length;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;


@Repository
public class RoadmapSearchDao {

    private final RoadmapDao roadmapDao;


    @Autowired
    public RoadmapSearchDao(RoadmapDao roadmapDao) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        this.roadmapDao = roadmapDao;
    }


    public List<Roadmap> search(String query, EntitySearchOptions options) {
        checkNotNull(query, "query cannot be null");
        checkNotNull(options, "options cannot be null");

        if (length(query) < 3) {
            return emptyList();
        }

        List<String> terms = SearchUtilities.mkTerms(query.toLowerCase());
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
