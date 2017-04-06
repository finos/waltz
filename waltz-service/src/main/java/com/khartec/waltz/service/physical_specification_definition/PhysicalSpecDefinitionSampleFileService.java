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
