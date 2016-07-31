package com.khartec.waltz.data.end_user_app;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.EntityIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrgUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableIdSelectionOptions;
import com.khartec.waltz.schema.tables.EndUserApplication;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

@Service
public class EndUserAppIdSelectorFactory extends EntityIdSelectorFactory  {


    private final EndUserApplication eua = END_USER_APPLICATION.as("eua");
    private final OrgUnitIdSelectorFactory orgUnitIdSelectorFactory;


    @Autowired
    public EndUserAppIdSelectorFactory(DSLContext dsl, OrgUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        super(dsl);
        Checks.checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    protected Select<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope) {
        throw new IllegalArgumentException("App Group selectors not supported for End User Applications");
    }


    @Override
    protected Select<Record1<Long>> mkForCapability(EntityReference ref, HierarchyQueryScope scope) {
        throw new IllegalArgumentException("App Capability selectors not supported for End User Applications");
    }


    @Override
    protected Select<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope) {

        IdSelectionOptions ouSelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(ouSelectorOptions);

        return dsl
                .selectDistinct(eua.ID)
                .from(eua)
                .where(dsl.renderInlined(eua.ORGANISATIONAL_UNIT_ID.in(ouSelector)));
    }

}
