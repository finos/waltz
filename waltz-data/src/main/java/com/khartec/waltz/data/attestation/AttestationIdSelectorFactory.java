package com.khartec.waltz.data.attestation;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static com.khartec.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;
import static com.khartec.waltz.schema.tables.Person.PERSON;

public class AttestationIdSelectorFactory implements Function<IdSelectionOptions, Select<Record1<Long>>> {

    private static final Condition ATTESTATION_INSTANCE_CONDITION = ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID);

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case PERSON:
                return mkForPerson(options);
//            case APP_GROUP:
//                return mkForAppGroup(options);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }

    private Select<Record1<Long>> mkForPerson(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        return DSL.select(ATTESTATION_RUN.ID)
                .from(ATTESTATION_RUN)
                .join(ATTESTATION_INSTANCE)
                .on(ATTESTATION_INSTANCE_CONDITION)
                .join(PERSON)
                .on(PERSON.EMAIL.eq(ATTESTATION_INSTANCE.ATTESTED_BY))
                .where(PERSON.ID.eq(options.entityReference().id()));
    }
}
