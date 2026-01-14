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

package org.finos.waltz.service.architecture_required_change;

import org.finos.waltz.data.architecture_required_change.ArchitectureRequiredChangeDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.architecture_required_change.ArchitectureRequiredChange;
import org.finos.waltz.service.change_initiative.ChangeInitiativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchitectureRequiredChangeService {

    private final ArchitectureRequiredChangeDao architectureRequiredChangeDao;

    private final ChangeInitiativeService changeInitiativeService;

    @Autowired
    public ArchitectureRequiredChangeService(ArchitectureRequiredChangeDao architectureRequiredChangeDao, ChangeInitiativeService changeInitiativeService) {
        this.architectureRequiredChangeDao = architectureRequiredChangeDao;
        this.changeInitiativeService = changeInitiativeService;
    }

    public ArchitectureRequiredChange getById(long id) {
        return architectureRequiredChangeDao.getById(id);
    }

    public ArchitectureRequiredChange getByExternalId(String externalId) {
        return architectureRequiredChangeDao.getByExternalId(externalId);
    }

    public List<ArchitectureRequiredChange> findForLinkedEntity(EntityReference ref) {
            return architectureRequiredChangeDao.findForLinkedEntity(ref);
    }

    public List<ArchitectureRequiredChange> findForLinkedEntityHierarchy(EntityReference ref) {
        switch (ref.kind()) {
            case CHANGE_INITIATIVE:
                return findForChangeInitiativeHierarchy(ref.id());
            default:
                throw new UnsupportedOperationException(String.format("Cannot find for %s kind", ref.kind()));
        }
    }

    private List<ArchitectureRequiredChange> findForChangeInitiativeHierarchy(Long id) {
        if(id != null) {
            IdSelectionOptions options = IdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.CHANGE_INITIATIVE, id));
            List<EntityReference> changeInitiatives = changeInitiativeService
                    .findHierarchyForSelector(options)
                    .stream()
                    .map(t -> EntityReference.mkRef(EntityKind.CHANGE_INITIATIVE, t.id().get()))
                    .toList();
            return architectureRequiredChangeDao.findForLinkedEntities(changeInitiatives);
        } else {
            return List.of();
        }
    }
}
