package com.khartec.waltz.data;

import com.khartec.waltz.model.entity_search.EntitySearchOptions;

import java.util.List;

public interface SearchDao<T> {

    List<T> search(EntitySearchOptions options);

}
