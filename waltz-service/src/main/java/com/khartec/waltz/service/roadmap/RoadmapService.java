package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.roadmap.ImmutableRoadmap;
import com.khartec.waltz.model.roadmap.Roadmap;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoadmapService {

    static Roadmap r1 = ImmutableRoadmap
            .builder()
            .id(1L)
            .name("R1")
            .columnType(EntityReference.mkRef(EntityKind.MEASURABLE_CATEGORY, 1))
            .rowType(EntityReference.mkRef(EntityKind.MEASURABLE_CATEGORY, 2))
            .description("row1 desc")
            .ratingSchemeId(1)
            .lastUpdatedBy("admin")
            .lastUpdatedAt(DateTimeUtilities.nowUtc())
            .build();


    public Roadmap getById(long id) {
        return r1;
    }


    public Collection<Roadmap> findRoadmapsRelatedToReference(EntityReference ref) {
        return ListUtilities.newArrayList(r1);
    }


    public Collection<Roadmap> findRoadmapsByAxisReference(EntityReference ref) {
        return ListUtilities.newArrayList(r1);
    }

}
