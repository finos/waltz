package org.finos.waltz.data.end_user_app.search;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.end_user_app.EndUserAppDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.enduserapp.EndUserApplication;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.SetUtilities.orderedUnion;
import static org.finos.waltz.data.EntityLifecycleStatusUtils.convertToLifecyclePhases;
import static org.finos.waltz.data.JooqUtilities.mkBasicTermSearch;
import static org.finos.waltz.data.JooqUtilities.mkStartsWithTermSearch;
import static org.finos.waltz.data.SearchUtilities.mkTerms;
import static org.finos.waltz.schema.Tables.END_USER_APPLICATION;
import static org.finos.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;


@Repository
public class EndUserAppSearchDao implements SearchDao<EndUserApplication> {

    private final DSLContext dsl;


    @Autowired
    public EndUserAppSearchDao(DSLContext dsl) {
            this.dsl = dsl;
    }


    /**
     * Searches by <code>name</code> and <code>external_id</code>
     * @param options
     * @return List of matching end user applications,
     *   matches on name are given precedence over external_id matches
     */
    @Override
    public List<EndUserApplication> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition nameCondition = mkBasicTermSearch(END_USER_APPLICATION.NAME, terms);
        Condition externalIdCondition = mkStartsWithTermSearch(END_USER_APPLICATION.EXTERNAL_ID, terms);
        Condition aliasCondition = ENTITY_ALIAS.KIND.eq(EntityKind.END_USER_APPLICATION.name())
                .and(JooqUtilities.mkBasicTermSearch(ENTITY_ALIAS.ALIAS, terms));

        return concat(
                mkQuery(nameCondition, options),
                mkQuery(externalIdCondition, options),
                mkAliasQuery(aliasCondition, options));
    }


    private List<EndUserApplication>  mkAliasQuery(Condition aliasCondition,
                                                   EntitySearchOptions options) {
        return dsl
                .selectDistinct(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .innerJoin(ENTITY_ALIAS)
                .on(ENTITY_ALIAS.ID.eq(END_USER_APPLICATION.ID)
                        .and(ENTITY_ALIAS.KIND.eq(EntityKind.END_USER_APPLICATION.name())))
                .where(aliasCondition)
                .orderBy(END_USER_APPLICATION.NAME)
                .limit(options.limit())
                .fetch(EndUserAppDao.TO_DOMAIN_MAPPER);
    }


    private List<EndUserApplication> mkQuery(Condition nameCondition, EntitySearchOptions options) {

        Condition lifecycleCondition = END_USER_APPLICATION.LIFECYCLE_PHASE.in(convertToLifecyclePhases(options.entityLifecycleStatuses()));

        return dsl
                .select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(nameCondition.and(lifecycleCondition))
                .orderBy(END_USER_APPLICATION.NAME)
                .limit(options.limit())
                .fetch(EndUserAppDao.TO_DOMAIN_MAPPER);
    }

}