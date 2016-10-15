package com.khartec.waltz.data;

import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkTrue;

public interface IdSelectorFactory extends Function<IdSelectionOptions, Select<Record1<Long>>> {


    default void ensureScopeIsExact(IdSelectionOptions options) {
        checkTrue(
                options.scope() == HierarchyQueryScope.EXACT,
                "Only EXACT scope supported");
    }
}
