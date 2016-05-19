package com.khartec.waltz.service.source_data_rating;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.source_data_rating.SourceDataRatingDao;
import com.khartec.waltz.model.source_data_rating.SourceDataRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SourceDataRatingService {

    private final SourceDataRatingDao dao;

    @Autowired
    public SourceDataRatingService(SourceDataRatingDao dao) {
        Checks.checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }

    public Collection<SourceDataRating> findAll() {
        return dao.findAll();
    }
}
