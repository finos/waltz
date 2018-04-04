package com.khartec.waltz.data.logical_data_element.search;


import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.logical_data_element.LogicalDataElementDao;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.sort;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.data.JooqUtilities.mkBasicTermSearch;
import static com.khartec.waltz.data.SearchUtilities.mkRelevancyComparator;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.LogicalDataElement.LOGICAL_DATA_ELEMENT;

@Repository
public class LogicalDataElementSearchDao {

    private final DSLContext dsl;


    @Autowired
    public LogicalDataElementSearchDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<LogicalDataElement> search(String termsStr, EntitySearchOptions options) {
        List<String> terms = mkTerms(termsStr);

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> validStatusNames = map(
                options.entityLifecycleStatuses(),
                s -> s.name());

        Condition statusCondition = LOGICAL_DATA_ELEMENT.ENTITY_LIFECYCLE_STATUS.in(validStatusNames);

        Condition likeName = mkBasicTermSearch(LOGICAL_DATA_ELEMENT.NAME, terms);
        Condition likeDesc = mkBasicTermSearch(LOGICAL_DATA_ELEMENT.DESCRIPTION, terms);

        List<LogicalDataElement> results = dsl
                .select(LOGICAL_DATA_ELEMENT.fields())
                .from(LOGICAL_DATA_ELEMENT)
                .where(likeName.and(statusCondition))
                .union(dsl
                    .select(LOGICAL_DATA_ELEMENT.fields())
                    .from(LOGICAL_DATA_ELEMENT)
                    .where(likeDesc.and(statusCondition)))
                .orderBy(LOGICAL_DATA_ELEMENT.NAME)
                .limit(options.limit())
                .fetch(LogicalDataElementDao.TO_DOMAIN_MAPPER);

        List<LogicalDataElement> sortedResults = sort(
                results,
                mkRelevancyComparator(a -> a.name(), terms.get(0)));

        return sortedResults;
    }
}
