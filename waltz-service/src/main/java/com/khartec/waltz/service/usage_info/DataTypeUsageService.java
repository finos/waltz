package com.khartec.waltz.service.usage_info;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.data_flow_decorator.DataFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.usage_info.ImmutableUsageInfo;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import com.khartec.waltz.service.data_type.DataTypeService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;
import static com.khartec.waltz.model.utils.IdUtilities.toIds;

@Service
public class DataTypeUsageService {

    private final DataTypeUsageDao dataTypeUsageDao;
    private final DataTypeService dataTypeService;
    private final ApplicationIdSelectorFactory appIdSelectorFactor;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final DataFlowDecoratorDao dataFlowDecoratorDao;
    private DataFlowDao dataFlowDao;


    @Autowired
    public DataTypeUsageService(DataTypeUsageDao dataTypeUsageDao,
                                DataTypeService dataTypeService,
                                ApplicationIdSelectorFactory selectorFactory,
                                DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                DataFlowDao dataFlowDao,
                                DataFlowDecoratorDao dataFlowDecoratorDao) {
        checkNotNull(dataTypeUsageDao, "dataTypeUsageDao cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");
        checkNotNull(selectorFactory, "appIdSelectorFactor cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(dataFlowDao, "dataFlowDao cannot be null");
        checkNotNull(dataFlowDecoratorDao, "dataFlowDecoratorDao cannot be null");

        this.dataTypeUsageDao = dataTypeUsageDao;
        this.dataTypeService = dataTypeService;
        this.appIdSelectorFactor = selectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.dataFlowDao = dataFlowDao;
        this.dataFlowDecoratorDao = dataFlowDecoratorDao;
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


    public int deleteForEntity(EntityReference ref) {
        return dataTypeUsageDao.deleteForEntity(ref);
    }


    public void recalculateForApplications(EntityReference... refs) {
        checkNotNull(refs, "refs cannot be null");
        for (EntityReference ref : refs) {
            recalculateForApplication(ref);
        }
    }


    private void recalculateForApplication(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        List<DataFlow> flows = dataFlowDao.findByEntityReference(ref);
        List<DataFlowDecorator> dataTypeDecorators = dataFlowDecoratorDao.findByFlowIdsAndKind(
                toIds(flows),
                EntityKind.DATA_TYPE);
        Map<Long, Collection<Long>> flowToDataTypeIds = groupBy(
                d -> d.dataFlowId(),
                d -> d.decoratorEntity().id(),
                dataTypeDecorators);
        Map<Long, String> dataTypeIdToCode = indexBy(
                dt -> dt.id().get(),
                dt -> dt.code(),
                dataTypeService.getAll());

        Set<String> incomingTypes = flows.stream()
                .filter(f -> f.target().equals(ref)) // only incoming
                .map(f -> f.id().get())
                .flatMap(fid -> flowToDataTypeIds.get(fid).stream())
                .map(dtId -> dataTypeIdToCode.get(dtId))
                .collect(Collectors.toSet());

        Set<String> outgoingTypes = flows.stream()
                .filter(f -> f.source().equals(ref)) // only outgoing
                .map(f -> f.id().get())
                .flatMap(fid -> flowToDataTypeIds.get(fid).stream())
                .map(dtId -> dataTypeIdToCode.get(dtId))
                .collect(Collectors.toSet());

        recalculateUsage(UsageKind.DISTRIBUTOR, ref, outgoingTypes);
        recalculateUsage(UsageKind.CONSUMER, ref, incomingTypes);

    }

    private void recalculateUsage(UsageKind kind, EntityReference ref, Set<String> types) {
        List<DataTypeUsage> currentUsages = findForEntity(ref);

        // Insert case
        for (String type: types) {
            Optional<DataTypeUsage> maybeExistingUsage = currentUsages.stream()
                    .filter(u -> u.dataTypeCode().equals(type))
                    .filter(u -> u.usage().kind().equals(kind))
                    .findFirst();

            maybeExistingUsage
                    .filter(u -> ! u.usage().isSelected())
                    .ifPresent(u -> dataTypeUsageDao.updateUsageInfo(ref, type, newArrayList(
                            ImmutableUsageInfo.builder()
                                    .isSelected(true)
                                    .description(u.usage().description())
                                    .kind(kind)
                                    .build())));

            if (! maybeExistingUsage.isPresent()) {
                dataTypeUsageDao.insertUsageInfo(ref, type, newArrayList(
                        ImmutableUsageInfo.builder()
                                .isSelected(true)
                                .kind(kind)
                                .build()));
            }
        }


        // Delete case
        currentUsages.stream()
                .filter(u -> ! types.contains(u.dataTypeCode()))
                .filter(u -> u.usage().kind().equals(kind))
                .filter(u -> StringUtilities.isEmpty(u.usage().description()))
                .forEach(u -> dataTypeUsageDao.deleteUsageInfo(ref, u.dataTypeCode(), newArrayList(kind)));


        currentUsages.stream()
                .filter(u -> ! types.contains(u.dataTypeCode()))
                .filter(u -> u.usage().kind().equals(kind))
                .filter(u -> StringUtilities.notEmpty(u.usage().description()))
                .forEach(u ->  dataTypeUsageDao.updateUsageInfo(ref, u.dataTypeCode(), newArrayList(
                        ImmutableUsageInfo.builder()
                                .isSelected(false)
                                .description(u.usage().description())
                                .kind(kind)
                                .build())));
    }
}
