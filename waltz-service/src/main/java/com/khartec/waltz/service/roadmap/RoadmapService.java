package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.roadmap.Roadmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class RoadmapService {

    private final RoadmapDao roadmapDao;


    @Autowired
    public RoadmapService(RoadmapDao roadmapDao) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        this.roadmapDao = roadmapDao;
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

}
