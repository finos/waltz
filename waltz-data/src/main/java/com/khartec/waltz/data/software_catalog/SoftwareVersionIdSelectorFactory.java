package com.khartec.waltz.data.software_catalog;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.SoftwareVersion;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;


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

}
