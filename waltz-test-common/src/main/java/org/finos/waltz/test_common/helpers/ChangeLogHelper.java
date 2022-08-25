package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.find;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Service
public class ChangeLogHelper {

    @Autowired
    private ChangeLogService changeLogService;


    public void assertChangeLogContainsAtLeastOneMatchingOperation(EntityReference ref, Operation operation) {

        List<ChangeLog> changeLogEntries = changeLogService.findByParentReference(
                ref,
                Optional.empty(),
                Optional.empty());

        assertNotNull(changeLogEntries);
        assertNotNull(find(
                c -> c.operation() == operation,
                changeLogEntries));
    }
}
