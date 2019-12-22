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

package com.khartec.waltz.service.entity_svg_diagram;

import com.khartec.waltz.data.entity_svg_diagram.EntitySvgDiagramDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntitySvgDiagramService {

    private final EntitySvgDiagramDao entitySvgDiagramDao;


    @Autowired
    public EntitySvgDiagramService(EntitySvgDiagramDao entitySvgDiagramDao) {
        checkNotNull(entitySvgDiagramDao, "entitySvgDiagramDao cannot be null");
        this.entitySvgDiagramDao = entitySvgDiagramDao;
    }


    public List<EntitySvgDiagram> findForEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return entitySvgDiagramDao.findForEntityReference(ref);
    }

}
