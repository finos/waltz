package org.finos.waltz.model;

import org.immutables.value.Value;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Param;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Value.Immutable
public interface CommonTableFields<T extends Record> {

    Param<String> NA_FIELD_VAL = DSL.val("n/a");
    Param<String> ALWAYS_ACTIVE_FIELD_VAL = DSL.val(EntityLifecycleStatus.ACTIVE.name());

    EntityKind entityKind();

    default boolean hierarchical() {
        return parentIdField() != null;
    }

    Table<? extends T> table();

    Field<Long> idField();

    @Nullable
    Field<String> nameField();

    @Nullable
    Field<String> descriptionField();

    @Nullable
    Field<Long> parentIdField();

    @Nullable
    Field<String> externalIdField();

    @Value.Default
    default Field<String> lifecycleField() { return ALWAYS_ACTIVE_FIELD_VAL; };

    @Value.Default
    default Condition isActiveCondition() {
        return DSL.trueCondition();
    }
}