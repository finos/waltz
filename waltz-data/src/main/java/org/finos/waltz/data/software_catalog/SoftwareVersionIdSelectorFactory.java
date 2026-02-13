package org.finos.waltz.data.software_catalog;

import org.finos.waltz.schema.tables.SoftwareVersion;
import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.model.HierarchyQueryScope.EXACT;


@Service
public class SoftwareVersionIdSelectorFactory implements IdSelectorFactory {
    private static final SoftwareVersion sv = SOFTWARE_VERSION.as("sv");

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case SOFTWARE:
                return mkForSoftwarePackage(options);
            case SOFTWARE_VERSION:
                return mkForSelf(options);
            case APPLICATION:
                return mkForApplication(options);
            default:
                throw new UnsupportedOperationException("Cannot create software version selector from options: " + options);
        }
    }


    private Select<Record1<Long>> mkForSoftwarePackage(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given an SOFTWARE ref");
        return DSL.select(SOFTWARE_VERSION.ID)
                .where(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForSelf(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given an SOFTWARE_VERSION ref");
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForApplication(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given an APPLICATION ref");
        return DSL.selectDistinct(SOFTWARE_USAGE.SOFTWARE_VERSION_ID)
                .from(SOFTWARE_USAGE)
                .where(SOFTWARE_USAGE.APPLICATION_ID.eq(options.entityReference().id()));
    }

}
