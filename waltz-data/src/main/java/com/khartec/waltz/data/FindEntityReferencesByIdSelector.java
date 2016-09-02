package com.khartec.waltz.data;

import com.khartec.waltz.model.EntityReference;
import org.jooq.Record1;
import org.jooq.Select;

import java.util.List;

public interface FindEntityReferencesByIdSelector {

    List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector);

}
