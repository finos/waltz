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

import com.khartec.waltz.data.trait.TraitUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.model.trait.TraitUsageKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraitUsageService {

    private final TraitUsageDao dao;


    @Autowired
    public TraitUsageService(TraitUsageDao dao) {
        this.dao = dao;
    }


    public List<TraitUsage> findAll() {
        return dao.findAll();
    }


    public List<TraitUsage> findByEntityKind(EntityKind kind) {
        return dao.findByEntityKind(kind);
    }

    public List<TraitUsage> findByTraitId(long id) {
        return dao.findByTraitId(id);
    }

    public List<TraitUsage> findByEntityReference(EntityReference reference) {
        return dao.findByEntityReference(reference);
    }

    public List<TraitUsage> addTraitUsage(EntityReference entityReference, Long traitId) {
        dao.addTraitUsage(entityReference, traitId, TraitUsageKind.EXHIBITS);
        return findByEntityReference(entityReference);
    }

    public List<TraitUsage> removeTraitUsage(EntityReference entityReference, long traitId) {
        dao.removeTraitUsage(entityReference, traitId);
        return findByEntityReference(entityReference);
    }
}
