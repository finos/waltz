package com.khartec.waltz.service.usage_info;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import org.jooq.Record1;
import org.jooq.Row1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;

@Service
public class DataTypeUsageService {

    private final DataTypeUsageDao dataTypeUsageDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactor;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;


    @Autowired
    public DataTypeUsageService(DataTypeUsageDao dataTypeUsageDao,
                                ApplicationIdSelectorFactory selectorFactory,
                                DataTypeIdSelectorFactory dataTypeIdSelectorFactory) {
        checkNotNull(dataTypeUsageDao, "dataTypeUsageDao cannot be null");
        checkNotNull(selectorFactory, "appIdSelectorFactor cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");

        this.dataTypeUsageDao = dataTypeUsageDao;
        this.appIdSelectorFactor = selectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
    }


    public List<DataTypeUsage> findForAppIdSelector(EntityKind kind, IdSelectionOptions options) {
        return dataTypeUsageDao.findForIdSelector(kind, appIdSelectorFactor.apply(options));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        return dataTypeUsageDao.findForEntity(ref);
    }


    public List<DataTypeUsage> findForDataTypeSelector(IdSelectionOptions dataTypeOptions) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(dataTypeOptions);
        return dataTypeUsageDao.findForDataTypeSelector(selector);
    }


    public List<DataTypeUsage> findForEntityAndDataType(EntityReference entityReference, String dataTypeCode) {
        return dataTypeUsageDao.findForEntityAndDataType(entityReference, dataTypeCode);
    }


    public List<Tally<String>> findUsageStatsForDataTypeSelector(IdSelectionOptions dataTypeIdSelectionOptions) {
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(dataTypeIdSelectionOptions);
        return dataTypeUsageDao.findUsageStatsForDataTypeSelector(dataTypeIdSelector);
    }


    /**
     * Given a usage kind and a dt selector, this will return a map, keyed by datatype id,
     * of application references which exhibit that usage kind.
     *
     * (UsageKind, SelectionOptions) ->  { DataType.id -> [ EntityRef... ] }
     * @param usageKind
     * @param dataTypeIdSelectionOptions
     * @return
     */
    public Map<Long, Collection<EntityReference>> findForUsageKindByDataTypeIdSelector(UsageKind usageKind,
                                                                                       IdSelectionOptions dataTypeIdSelectionOptions) {
        checkNotNull(usageKind, "usageKind cannot be null");
        checkNotNull(dataTypeIdSelectionOptions, "dataTypeIdSelectionOptions cannot be null");

        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(dataTypeIdSelectionOptions);
        return dataTypeUsageDao.findForUsageKindByDataTypeIdSelector(usageKind, dataTypeIdSelector);
    }


    public SystemChangeSet<UsageInfo, UsageKind> save(
            EntityReference entityReference,
            String dataTypeCode,
            List<UsageInfo> usages) {

        Collection<UsageInfo> base = map(
                dataTypeUsageDao.findForEntityAndDataType(entityReference, dataTypeCode),
                dtu -> dtu.usage());

        SystemChangeSet<UsageInfo, UsageKind> changeSet = mkChangeSet(
                SetUtilities.fromCollection(base),
                SetUtilities.fromCollection(usages));

        dataTypeUsageDao.insertUsageInfo(entityReference, dataTypeCode, changeSet.inserts());
        dataTypeUsageDao.deleteUsageInfo(entityReference, dataTypeCode, changeSet.deletes());
        dataTypeUsageDao.updateUsageInfo(entityReference, dataTypeCode, changeSet.updates());

        return changeSet;
    }


    public boolean recalculateForAllApplications() {
        return dataTypeUsageDao.recalculateForAllApplications();
    }


    public boolean recalculateForApplications(EntityReference... refs) {
        checkNotNull(refs, "refs cannot be null");
        Select<Record1<Long>> idSelector = convertRefsToIdSelector(refs);
        return dataTypeUsageDao.recalculateForAppIdSelector(idSelector);
    }


    private Select<Record1<Long>> convertRefsToIdSelector(EntityReference[] refs) {
        Row1<Long>[] idsAsRows = new Row1[refs.length];
        ArrayUtilities.mapToList(refs, r -> DSL.row(r.id())).toArray(idsAsRows);

        return DSL.selectFrom(DSL.values(idsAsRows));
    }

}
