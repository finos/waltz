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

package com.khartec.waltz.service.entity_named_note;

import com.khartec.waltz.data.entity_named_note.EntityNamedNodeDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_named_note.EntityNamedNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityNamedNoteService {

    private final EntityNamedNodeDao entityNamedNodeDao;


    @Autowired
    public EntityNamedNoteService(EntityNamedNodeDao entityNamedNodeDao) {
        checkNotNull(entityNamedNodeDao, "entityNamedNodeDao cannot be null");
        this.entityNamedNodeDao = entityNamedNodeDao;
    }


    public List<EntityNamedNote> findByEntityReference(EntityReference ref) {
        return entityNamedNodeDao.findByEntityReference(ref);
    }

}
