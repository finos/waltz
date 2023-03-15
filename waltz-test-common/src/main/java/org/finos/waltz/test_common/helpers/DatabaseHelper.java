package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.LifecycleStatus;
import org.finos.waltz.model.database_information.ImmutableDatabaseInformation;
import org.finos.waltz.service.database_information.DatabaseInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class DatabaseHelper {

    @Autowired
    private DatabaseInformationService databaseInformationService;


    public EntityReference createNewDatabase(String databaseName, String instanceName) {
        Long id = databaseInformationService
                .createDatabase(
                        ImmutableDatabaseInformation.builder()
                                .instanceName(instanceName)
                                .databaseName(databaseName)
                                .dbmsName("MSSQL")
                                .dbmsVendor("Microsoft")
                                .dbmsVersion("2018")
                                .externalId(databaseName)
                                .lifecycleStatus(LifecycleStatus.ACTIVE)
                                .provenance("TEST")
                                .build());

        return mkRef(EntityKind.DATABASE, id, databaseName);
    }

}
