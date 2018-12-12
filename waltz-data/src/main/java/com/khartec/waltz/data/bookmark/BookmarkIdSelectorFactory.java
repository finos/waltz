package com.khartec.waltz.data.bookmark;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Bookmark;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class BookmarkIdSelectorFactory implements IdSelectorFactory {
    private static final Bookmark bk = Bookmark.BOOKMARK.as("bk");

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
                .select(bk.ID)
                .from(bk)
                .where(bk.PARENT_KIND.eq(genericSelector.kind().name()))
                .and(bk.PARENT_ID.in(genericSelector.selector()));
    }
}
