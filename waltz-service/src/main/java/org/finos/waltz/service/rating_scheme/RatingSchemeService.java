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

package org.finos.waltz.service.rating_scheme;

import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rating.RatingScheme;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.rating.RatingSchemeItemUsageCount;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RatingSchemeService {

    private final RatingSchemeDAO ratingSchemeDAO;

    @Autowired
    public RatingSchemeService(RatingSchemeDAO ratingSchemeDAO) {
        this.ratingSchemeDAO = ratingSchemeDAO;
    }

    public Collection<RatingScheme> findAll() {
        return ratingSchemeDAO.findAll();
    }

    public RatingScheme getById(long id) {
        return ratingSchemeDAO.getById(id);
    }

    public List<RatingSchemeItem> findAllRatingSchemeItems() {
        return ratingSchemeDAO.fetchItems(DSL.trueCondition());
    }

    public List<RatingSchemeItem> findRatingSchemeItemsByAssessmentDefinition(long assessmentDefinitionId) {
        return ratingSchemeDAO.findRatingSchemeItemsForAssessmentDefinition(assessmentDefinitionId);
    }

    public List<RatingSchemeItem> findRatingSchemeItemsForEntityAndCategory(EntityReference ref, long measurableCategoryId) {
        return ratingSchemeDAO.findRatingSchemeItemsForEntityAndCategory(ref, measurableCategoryId);
    }

    public Set<RatingSchemeItem> findRatingSchemeItemsByIds(Set<Long> ids) {
        return ratingSchemeDAO.findRatingSchemeItemsByIds(ids);
    }

    public RatingSchemeItem getRatingSchemeItemById(Long id) {
        return ratingSchemeDAO.getRatingSchemeItemById(id);
    }


    public Boolean save(RatingScheme scheme) {
        return ratingSchemeDAO.save(scheme);
    }


    public Long saveRatingItem(long schemeId, RatingSchemeItem item) {
        return ratingSchemeDAO.saveRatingItem(schemeId, item);
    }


    public Boolean removeRatingItem(long itemId) {
        return ratingSchemeDAO.removeRatingItem(itemId);
    }


    public List<RatingSchemeItemUsageCount> calcRatingUsageStats() {
        return ratingSchemeDAO.calcRatingUsageStats();
    }

    public Boolean removeRatingScheme(long id) {
        return ratingSchemeDAO.removeRatingScheme(id);
    }

    public Set<RatingSchemeItem> findRatingSchemeItemsBySchemeIds(Set<Long> schemeIds) {
        return ratingSchemeDAO.findRatingSchemeItemsForSchemeIds(schemeIds);
    }
}
