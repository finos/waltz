/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.rating_scheme;

import com.khartec.waltz.data.rating_scheme.RatingSchemeDAO;
import com.khartec.waltz.model.rating.RatingScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

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

}
