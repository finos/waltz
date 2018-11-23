package com.khartec.waltz.data.bookmark;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Bookmark.BOOKMARK;

@Service
public class BookmarkIdSelectorFactory implements IdSelectorFactory {
    private final GenericSelectorFactory genericSelectorFactory;

    @Autowired
    public BookmarkIdSelectorFactory(GenericSelectorFactory genericSelectorFactory) {
        checkNotNull(genericSelectorFactory, "genericSelectorFactory cannot be null");
        this.genericSelectorFactory = genericSelectorFactory;
    }

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.apply(selectionOptions);
        return DSL
                .select(BOOKMARK.ID)
                .from(BOOKMARK)
                .where(BOOKMARK.PARENT_KIND.eq(genericSelector.kind().name()))
                .and(BOOKMARK.PARENT_ID.in(genericSelector.selector()));
    }
}
