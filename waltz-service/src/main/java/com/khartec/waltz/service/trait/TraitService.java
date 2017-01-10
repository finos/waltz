/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.service.trait;


import com.khartec.waltz.data.trait.TraitDao;
import com.khartec.waltz.model.trait.Trait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraitService {

    private final TraitDao dao;


    @Autowired
    public TraitService(TraitDao dao) {
        this.dao = dao;
    }


    public List<Trait> findAll() {
        return dao.findAll();
    }


    public Trait getById(long id) {
        return dao.getById(id);
    }


    public List<Trait> findByIds(List<Long> ids) {
        return dao.findByIds(ids);
    }

    public List<Trait> findApplicationDeclarableTraits() {
        return dao.findApplicationDeclarableTraits();
    }

}
