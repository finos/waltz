package org.finos.waltz.data.licence.search;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.licence.LicenceDao;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.licence.Licence;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.data.JooqUtilities.mkBasicTermSearch;
import static org.finos.waltz.data.JooqUtilities.mkStartsWithTermSearch;
import static org.finos.waltz.data.SearchUtilities.mkTerms;
import static org.finos.waltz.schema.Tables.LICENCE;

@Repository
public class LicenceSearchDao implements SearchDao<Licence> {

    private final DSLContext dsl;


    @Autowired
    public LicenceSearchDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    /**
     * Searches by <code>name</code> and <code>external_id</code>
     * @param options
     * @return List of matching legal entities,
     *   matches on name are given precedence over external_id matches
     */
    @Override
    public List<Licence> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition nameCondition = mkBasicTermSearch(LICENCE.NAME, terms);
        Condition externalIdCondition = mkStartsWithTermSearch(LICENCE.EXTERNAL_ID, terms);

        return ListUtilities.distinct(concat(
                mkQuery(nameCondition, options),
                mkQuery(externalIdCondition, options)));
    }


    private List<Licence> mkQuery(Condition nameCondition, EntitySearchOptions options) {
        return dsl
                .select(LICENCE.fields())
                .from(LICENCE)
                .where(nameCondition)
                .orderBy(LICENCE.NAME)
                .limit(options.limit())
                .fetch(LicenceDao.TO_DOMAIN_MAPPER);
    }

}
