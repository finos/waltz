package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.roadmap.Roadmap;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class RoadmapService {

    private final RoadmapDao roadmapDao;
    private RoadmapIdSelectorFactory roadmapIdSelectorFactory;


    @Autowired
    public RoadmapService(RoadmapDao roadmapDao, RoadmapIdSelectorFactory roadmapIdSelectorFactory) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        checkNotNull(roadmapIdSelectorFactory, "roadmapIdSelectorFactory cannot be null");
        this.roadmapDao = roadmapDao;
        this.roadmapIdSelectorFactory = roadmapIdSelectorFactory;
    }


    public Roadmap getById(long id) {
        return roadmapDao.getById(id);
    }


    public Collection<Roadmap> findRoadmapsRelatedToReference(EntityReference ref) {
        throw new UnsupportedOperationException("TODO"); //TODO: implement
    }


    public Collection<Roadmap> findRoadmapsByAxisReference(EntityReference ref) {
        throw new UnsupportedOperationException("TODO"); //TODO: implement
    }

    public Collection<Roadmap> findRoadmapsBySelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = roadmapIdSelectorFactory.apply(selectionOptions);
        return roadmapDao.findRoadmapsBySelector(selector);
    }


}
