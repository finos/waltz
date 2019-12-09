package com.khartec.waltz.data.attestation;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;

public class AttestationIdSelectorFactory implements Function<IdSelectionOptions, Select<Record1<Long>>> {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case PERSON:
            case ORG_UNIT:
            case APP_GROUP:
                return mkForRelatedApps(options);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }

    private Select<Record1<Long>> mkForRelatedApps(IdSelectionOptions options) {
        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(options);
        return DSL
                .select(ATTESTATION_INSTANCE.ID)
                .from(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.in(appIds));
    }
}
