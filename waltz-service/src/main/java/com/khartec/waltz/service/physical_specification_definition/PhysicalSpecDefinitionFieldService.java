package com.khartec.waltz.service.physical_specification_definition;

import com.khartec.waltz.data.physical_specification_definition.PhysicalSpecDefinitionFieldDao;
import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionFieldChangeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class PhysicalSpecDefinitionFieldService {

    private final PhysicalSpecDefinitionFieldDao dao;


    @Autowired
    public PhysicalSpecDefinitionFieldService(PhysicalSpecDefinitionFieldDao dao) {
        checkNotNull(dao, "dao cannot be null");

        this.dao = dao;
    }


    public long create(String userName,
                       long specDefinitionId,
                       PhysicalSpecDefinitionFieldChangeCommand command) {

        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        return dao.create(ImmutablePhysicalSpecDefinitionField.builder()
                .specDefinitionId(specDefinitionId)
                .name(command.name())
                .position(command.position())
                .type(command.type())
                .description(command.description())
                .lastUpdatedBy(userName)
                .build());
    }


    public int delete(long specDefinitionFieldId) {
        return dao.delete(specDefinitionFieldId);
    }


    public int deleteForSpecDefinition(long specDefinitionId) {
        return dao.deleteForSpecDefinition(specDefinitionId);
    }


    public List<PhysicalSpecDefinitionField> findForSpecDefinition(long specDefinitionId) {
        return dao.findForSpecDefinition(specDefinitionId);
    }
}
