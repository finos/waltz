package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.CollectionUtilities.find;
import static org.junit.Assert.assertNotNull;

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
