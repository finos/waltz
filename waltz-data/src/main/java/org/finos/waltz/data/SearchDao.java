package org.finos.waltz.data;

import org.finos.waltz.model.entity_search.EntitySearchOptions;

import java.util.List;

public interface SearchDao<T> {

    List<T> search(EntitySearchOptions options);

}
