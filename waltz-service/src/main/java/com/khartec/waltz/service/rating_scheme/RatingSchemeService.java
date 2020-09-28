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

package com.khartec.waltz.service.rating_scheme;

import com.khartec.waltz.data.rating_scheme.RatingSchemeDAO;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RatingScheme;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class RatingSchemeService {

    private final RatingSchemeDAO ratingSchemeDAO;

    @Autowired
    public RatingSchemeService(RatingSchemeDAO ratingSchemeDAO) {
        this.ratingSchemeDAO = ratingSchemeDAO;
    }

    public Collection<RatingScheme>  findAll() {
        return ratingSchemeDAO.findAll();
    }

    public RatingScheme getById(long id) {
        return ratingSchemeDAO.getById(id);
    }

    public List<RagName> getAllRatingSchemeItems() {return ratingSchemeDAO.fetchItems(DSL.trueCondition()); }


    public List<RagName> findRatingSchemeItemsForEntityAndCategory(EntityReference ref, long measurableCategoryId) {
        return ratingSchemeDAO.findRatingSchemeItemsForEntityAndCategory(ref, measurableCategoryId);
    }

    public Set<RagName> findRatingSchemeItemsByIds(Set<Long> ids) {
        return ratingSchemeDAO.findRatingSchemeItemsByIds(ids);
    }





}
