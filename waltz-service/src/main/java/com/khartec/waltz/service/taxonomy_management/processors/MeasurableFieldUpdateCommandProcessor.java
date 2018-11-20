package com.khartec.waltz.service.taxonomy_management.processors;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangePreview;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;

public abstract class MeasurableFieldUpdateCommandProcessor<T> implements TaxonomyCommandProcessor {

    @Override
    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        return null;
    }

    @Override
    public TaxonomyChangeCommand apply(TaxonomyChangeCommand command, String userId) {
        return null;
    }

    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }
}
