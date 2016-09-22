package com.khartec.waltz.service.tour;

import com.khartec.waltz.data.tour.TourDao;
import com.khartec.waltz.model.tour.TourStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

/**
 * The tour service is used to provide guided tours through application functionality.
 * A tour is comprised of multiple steps, which are ordered by id and grouped together
 * using a key.  This key is typically the name of a page.
 */
@Service
public class TourService {

    private final TourDao tourDao;


    @Autowired
    public TourService(TourDao tourDao) {
        checkNotNull(tourDao, "tourDao cannot be null");
        this.tourDao = tourDao;
    }

    
    public List<TourStep> findByKey(String key) {
        return tourDao.findByKey(key);
    }
}
