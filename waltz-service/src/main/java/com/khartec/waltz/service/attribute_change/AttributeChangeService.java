/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.service.attribute_change;

import com.khartec.waltz.data.attribute_change.AttributeChangeDao;
import com.khartec.waltz.model.attribute_change.AttributeChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AttributeChangeService {

    private final AttributeChangeDao dao;


    @Autowired
    public AttributeChangeService(AttributeChangeDao dao) {
        checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }


    public AttributeChange getById(long id) {
        return dao.getById(id);
    }


    public List<AttributeChange> findByChangeUnitId(long id) {
        return dao.findByChangeUnitId(id);
    }
}
