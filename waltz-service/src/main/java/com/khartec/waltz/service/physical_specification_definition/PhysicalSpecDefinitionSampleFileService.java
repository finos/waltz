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

package com.khartec.waltz.service.physical_specification_definition;

import com.khartec.waltz.data.physical_specification_definition.PhysicalSpecDefinitionSampleFileDao;
import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFileCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class PhysicalSpecDefinitionSampleFileService {

    private final PhysicalSpecDefinitionSampleFileDao dao;


    @Autowired
    public PhysicalSpecDefinitionSampleFileService(PhysicalSpecDefinitionSampleFileDao dao) {
        checkNotNull(dao, "dao cannot be null");

        this.dao = dao;
    }


    public long create(long specDefinitionId,
                       PhysicalSpecDefinitionSampleFileCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        return dao.create(ImmutablePhysicalSpecDefinitionSampleFile.builder()
                .specDefinitionId(specDefinitionId)
                .name(command.name())
                .fileData(command.fileData())
                .build());
    }


    public int delete(long specDefinitionSampleFileId) {
        return dao.delete(specDefinitionSampleFileId);
    }


    public int deleteForSpecDefinition(long specDefinitionId) {
        return dao.deleteForSpecDefinition(specDefinitionId);
    }


    public Optional<PhysicalSpecDefinitionSampleFile> findForSpecDefinition(long specDefinitionId) {
        return dao.findForSpecDefinition(specDefinitionId);
    }
}
