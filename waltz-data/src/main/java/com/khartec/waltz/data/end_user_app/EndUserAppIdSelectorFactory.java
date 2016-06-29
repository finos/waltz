package com.khartec.waltz.data.end_user_app;

import com.khartec.waltz.data.EntityIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.schema.tables.EndUserApplication;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

@Service
public class EndUserAppIdSelectorFactory extends EntityIdSelectorFactory  {

    private final OrganisationalUnitDao organisationalUnitDao;

    private final EndUserApplication eua = END_USER_APPLICATION.as("eua");


    @Autowired
    public EndUserAppIdSelectorFactory(DSLContext dsl, OrganisationalUnitDao organisationalUnitDao) {
        super(dsl);
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");

        this.organisationalUnitDao = organisationalUnitDao;
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
        Set<Long> orgUnitIds = new HashSet<>();
        orgUnitIds.add(ref.id());

        switch (scope) {
            case EXACT:
                break;
            case CHILDREN:
                List<OrganisationalUnit> subUnits = organisationalUnitDao.findDescendants(ref.id());
                orgUnitIds.addAll(IdUtilities.toIds(subUnits));
                break;
            case PARENTS:
                List<OrganisationalUnit> parentUnits = organisationalUnitDao.findAncestors(ref.id());
                orgUnitIds.addAll(IdUtilities.toIds(parentUnits));
                break;
        }

        return dsl
                .selectDistinct(eua.ID)
                .from(eua)
                .where(dsl.renderInlined(eua.ORGANISATIONAL_UNIT_ID.in(orgUnitIds)));
    }

}
