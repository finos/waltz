package com.khartec.waltz.service.usage_info;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.usage_info.ImmutableUsageInfo;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import com.khartec.waltz.service.data_flow.DataFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;

@Service
public class DataTypeUsageService {

    private final DataTypeUsageDao dataTypeUsageDao;
    private final DataFlowService dataFlowService;
    private final ApplicationIdSelectorFactory selectorFactory;


    @Autowired
    public DataTypeUsageService(DataTypeUsageDao dataTypeUsageDao,
                                DataFlowService dataFlowService,
                                ApplicationIdSelectorFactory selectorFactory) {
        checkNotNull(dataTypeUsageDao, "dataTypeUsageDao cannot be null");
        checkNotNull(dataFlowService, "dataFlowService cannot be null");
        checkNotNull(selectorFactory, "selectorFactory cannot be null");
        
        this.dataTypeUsageDao = dataTypeUsageDao;
        this.dataFlowService = dataFlowService;
        this.selectorFactory = selectorFactory;
    }


    public List<DataTypeUsage> findForIdSelector(EntityKind kind, IdSelectionOptions options) {
        return dataTypeUsageDao.findForIdSelector(kind, selectorFactory.apply(options));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        return dataTypeUsageDao.findForEntity(ref);
    }


    public List<DataTypeUsage> findForDataType(String dataTypeCode) {
        return dataTypeUsageDao.findForDataType(dataTypeCode);
    }

    public List<DataTypeUsage> findForEntityAndDataType(EntityReference entityReference, String dataTypeCode) {
        return dataTypeUsageDao.findForEntityAndDataType(entityReference, dataTypeCode);
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

        List<DataFlow> flows = dataFlowService.findByEntityReference(ref);

        Set<String> incomingTypes = flows.stream()
                .filter(f -> f.target().equals(ref)) // only incoming
                .map(f -> f.dataType())
                .collect(Collectors.toSet());

        Set<String> outgoingTypes = flows.stream()
                .filter(f -> f.source().equals(ref)) // only outgoing
                .map(f -> f.dataType())
                .collect(Collectors.toSet());

        recalculateUsage(UsageKind.DISTRIBUTOR, ref, outgoingTypes);
        recalculateUsage(UsageKind.CONSUMER, ref, incomingTypes);

    }

    private void recalculateUsage(UsageKind kind, EntityReference ref, Set<String> types) {
        List<DataTypeUsage> currentUsages = findForEntity(ref);

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


        currentUsages.stream()
                .filter(u -> ! types.contains(u.dataTypeCode()))
                .filter(u -> u.usage().kind().equals(kind))
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
