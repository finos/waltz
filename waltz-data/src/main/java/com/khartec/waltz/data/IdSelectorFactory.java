package com.khartec.waltz.data;

import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import java.util.function.Function;

public interface IdSelectorFactory extends Function<IdSelectionOptions, Select<Record1<Long>>> {
}
