package org.finos.waltz.model;

import org.immutables.value.Value;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;

@Value.Immutable
public interface CommonTableFields<T extends Record> {
    EntityKind entityKind();

    default boolean hierarchical() {
        return parentIdField() != null;
    }

    Table<? extends T> table();

    TableField<? extends T, Long> idField();

    @Nullable
    TableField<? extends T, String> nameField();

    @Nullable
    TableField<? extends T, String> descriptionField();

    @Nullable
    TableField<? extends T, Long> parentIdField();

    @Nullable
    TableField<? extends T, String> externalIdField();

    @Value.Default
    default Condition isActiveCondition() {
        return DSL.trueCondition();
    }
}