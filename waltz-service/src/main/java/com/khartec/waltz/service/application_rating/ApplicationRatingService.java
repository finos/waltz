package com.khartec.waltz.service.application_rating;

import com.khartec.waltz.data.application_rating.ApplicationRatingDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application_rating.ApplicationRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class ApplicationRatingService {

    private final ApplicationRatingDao applicationRatingDao;


    @Autowired
    public ApplicationRatingService(ApplicationRatingDao applicationRatingDao) {
        checkNotNull(applicationRatingDao, "applicationRatingDao cannot be null");
        this.applicationRatingDao = applicationRatingDao;
    }


    public List<ApplicationRating> findRatingsById(EntityKind perspective, long applicationId) {
        checkNotNull(perspective, "perspective cannot be null");
        return applicationRatingDao.findRatingsById(perspective, applicationId);
    }

}
