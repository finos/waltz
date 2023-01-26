package org.finos.waltz.data.legal_entity.search;

import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.legal_entity.LegalEntityDao;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.data.JooqUtilities.mkBasicTermSearch;
import static org.finos.waltz.data.JooqUtilities.mkStartsWithTermSearch;
import static org.finos.waltz.data.SearchUtilities.mkTerms;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY;


@Service
public class LegalEntitySearchDao implements SearchDao<LegalEntity> {

    private final DSLContext dsl;


    @Autowired
    public LegalEntitySearchDao(DSLContext dsl) {
            this.dsl = dsl;
    }


    /**
     * Searches by <code>name</code> and <code>external_id</code>
     * @param options
     * @return List of matching legal entities,
     *   matches on name are given precedence over external_id matches
     */
    @Override
    public List<LegalEntity> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition nameCondition = mkBasicTermSearch(LEGAL_ENTITY.NAME, terms);
        Condition externalIdCondition = mkStartsWithTermSearch(LEGAL_ENTITY.EXTERNAL_ID, terms);

        return concat(
                mkQuery(nameCondition, options),
                mkQuery(externalIdCondition, options));
    }


    private List<LegalEntity> mkQuery(Condition nameCondition, EntitySearchOptions options) {
        return dsl
                .select(LEGAL_ENTITY.fields())
                .from(LEGAL_ENTITY)
                .where(nameCondition)
                .orderBy(LEGAL_ENTITY.NAME)
                .limit(options.limit())
                .fetch(LegalEntityDao.TO_DOMAIN_MAPPER);
    }

}