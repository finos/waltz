package org.finos.waltz.service.system_version;

import org.finos.waltz.model.system_version.ImmutableSystemVersionInfo;
import org.finos.waltz.model.system_version.SystemVersionInfo;
import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.schema.Tables.DATABASECHANGELOG;

@Service
public class SystemVersionService {

    private final DSLContext dsl;

    @Autowired
    public SystemVersionService(DSLContext dsl) {
        this.dsl = dsl;
    }


    public SystemVersionInfo getVersionInfo() {
        return ImmutableSystemVersionInfo
                .builder()
                .packageVersion(packageVersion())
                .databaseVersions(databaseVersions())
                .build();
    }

    private String packageVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        String title = getClass().getPackage().getImplementationTitle();
        String vendor = getClass().getPackage().getImplementationVendor();

        return String.format("%s - %s - %s", version, title, vendor);
    }


    private List<String> databaseVersions() {
        Field<String> filenameField = DSL.field("filename", String.class);
        Field<Integer> rowNumField = DSL.field("rn", Integer.class);

        CommonTableExpression<Record2<String, Integer>> versionCTE = DSL
                .name("raw")
                .fields(filenameField.getName(), rowNumField.getName())
                .as(DSL
                        .select(
                                DATABASECHANGELOG.FILENAME,
                                DSL.rowNumber()
                                        .over(DSL
                                                .partitionBy(DATABASECHANGELOG.FILENAME)
                                                .orderBy(DATABASECHANGELOG.DATEEXECUTED)))
                        .from(DATABASECHANGELOG)
                        .orderBy(DATABASECHANGELOG.DATEEXECUTED.desc()));

        return dsl
                .with(versionCTE)
                .select(filenameField)
                .from(versionCTE)
                .where(rowNumField.eq(1))
                .fetch(filenameField);
    }

}
