/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
