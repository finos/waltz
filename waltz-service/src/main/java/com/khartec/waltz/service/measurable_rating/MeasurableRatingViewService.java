/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.measurable_rating;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.measurable_rating.ImmutableMeasurableRatingGridView;
import com.khartec.waltz.model.measurable_rating.ImmutableMeasurableRatingGridViewRatingItem;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.measurable_rating.MeasurableRatingGridView;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.filter;
import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.SetUtilities.map;

@Service
public class MeasurableRatingViewService {

    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableDao measurableDao;
    private final MeasurableCategoryDao measurableCategoryDao;
    private final RatingSchemeService ratingSchemeService;
    private final ApplicationDao applicationDao;

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public MeasurableRatingViewService(MeasurableRatingDao measurableRatingDao,
                                       MeasurableDao measurableDao,
                                       MeasurableCategoryDao measurableCategoryDao,
                                       ApplicationDao applicationDao,
                                       RatingSchemeService ratingSchemeService) {
        checkNotNull(measurableRatingDao, "measurableRatingDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableCategoryDao, "measurableCategoryDao cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");

        this.measurableRatingDao = measurableRatingDao;
        this.measurableDao = measurableDao;
        this.measurableCategoryDao = measurableCategoryDao;
        this.applicationDao = applicationDao;
        this.ratingSchemeService = ratingSchemeService;
    }


    public MeasurableRatingGridView findByCategoryIdAndSelectionOptions(
            long categoryId,
            IdSelectionOptions idSelectionOptions) {

        MeasurableCategory category = measurableCategoryDao.getById(categoryId);
        return findByCategoryAndSelectionOptions(category, idSelectionOptions);
    }


    public MeasurableRatingGridView findByCategoryExtIdAndSelectionOptions(
            String categoryExtId,
            IdSelectionOptions idSelectionOptions) {

        Set<MeasurableCategory> categories = measurableCategoryDao.findByExternalId(categoryExtId);
        return findByCategoryAndSelectionOptions(first(categories), idSelectionOptions);
    }



    private MeasurableRatingGridView findByCategoryAndSelectionOptions(
            MeasurableCategory category,
            IdSelectionOptions idSelectionOptions) {

        List<Measurable> measurables = measurableDao.findByCategoryId(category.id().get());
        Set<Long> measurableIds = IdUtilities.toIds(measurables);


        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(idSelectionOptions);
        List<Application> apps = applicationDao.findByAppIdSelector(appIdSelector);
        Collection<MeasurableRating> allRatingsForSelector = measurableRatingDao
                .findByApplicationIdSelector(appIdSelector);

        Collection<MeasurableRating> relevantRatings = filter(
                allRatingsForSelector,
                r -> measurableIds.contains(r.measurableId()));

        return ImmutableMeasurableRatingGridView
                .builder()
                .category(category)
                .measurableReferences(map(measurables, Measurable::entityReference))
                .applications(apps)
                .ratingSchemeItems(ratingSchemeService.findRatingSchemeItemsForEntityAndCategory(category.entityReference(), category.id().get()))
                .ratings(map(relevantRatings, r -> ImmutableMeasurableRatingGridViewRatingItem
                        .builder()
                        .measurableId(r.measurableId())
                        .applicationId(r.entityReference().id())
                        .rating(String.valueOf(r.rating()))
                        .build()))
                .build();
    }

    // -- READ

}
