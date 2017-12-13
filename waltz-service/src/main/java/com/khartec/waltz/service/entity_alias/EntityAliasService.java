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

package com.khartec.waltz.service.entity_alias;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.entity_alias.EntityAliasDao;
import com.khartec.waltz.model.EntityReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class EntityAliasService {

    private final EntityAliasDao entityAliasDao;

    @Autowired
    public EntityAliasService(EntityAliasDao entityAliasDao) {
        Checks.checkNotNull(entityAliasDao, "entityAliasDao cannot be null");
        this.entityAliasDao = entityAliasDao;
    }


    public List<String> findAliasesForEntityReference(EntityReference ref) {
        return entityAliasDao.findAliasesForEntityReference(ref);
    }


    public int[] updateAliases(EntityReference ref, Collection<String> aliases) {
        return entityAliasDao.updateAliases(ref, aliases);
    }
}
