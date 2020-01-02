/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.data.bookmark;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Bookmark;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

public class BookmarkIdSelectorFactory implements IdSelectorFactory {
    private static final Bookmark bk = Bookmark.BOOKMARK.as("bk");

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.apply(selectionOptions);
        return DSL
                .select(bk.ID)
                .from(bk)
                .where(bk.PARENT_KIND.eq(genericSelector.kind().name()))
                .and(bk.PARENT_ID.in(genericSelector.selector()));
    }
}
