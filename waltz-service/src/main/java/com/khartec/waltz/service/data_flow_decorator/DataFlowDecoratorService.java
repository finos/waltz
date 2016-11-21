package com.khartec.waltz.service.data_flow_decorator;


import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.data_flow_decorator.DataFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.model.EntityKind.DATA_TYPE;

@Service
public class DataFlowDecoratorService {

    private final DataFlowDecoratorDao dataFlowDecoratorDao;
    private final DataFlowDecoratorRatingsService ratingsService;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final DataTypeUsageService dataTypeUsageService;
    private final LogicalFlowDao dataFlowDao;
    private final ChangeLogService changeLogService;

    @Autowired
    public DataFlowDecoratorService(DataFlowDecoratorDao dataFlowDecoratorDao,
                                    DataFlowDecoratorRatingsService ratingsService,
                                    ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                    DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                    DataTypeUsageService dataTypeUsageService,
                                    LogicalFlowDao dataFlowDao,
                                    ChangeLogService changeLogService) {

        checkNotNull(dataFlowDecoratorDao, "dataFlowDecoratorDao cannot be null");
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(ratingsService, "ratingsService cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(dataFlowDao, "dataFlowDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.dataFlowDecoratorDao = dataFlowDecoratorDao;
        this.ratingsService = ratingsService;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataFlowDao = dataFlowDao;
        this.changeLogService = changeLogService;
    }


    // --- FINDERS ---

    public List<DataFlowDecorator> findByFlowIds(Collection<Long> flowIds) {
        checkNotNull(flowIds, "flowIds cannot be null");
        return dataFlowDecoratorDao.findByFlowIds(flowIds);
    }


    public List<DataFlowDecorator> findByIdSelectorAndKind(IdSelectionOptions options,
                                                           EntityKind decoratorEntityKind) {
        checkNotNull(options, "options cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        switch (options.entityReference().kind()) {
            case APPLICATION:
            case APP_GROUP:
            case CAPABILITY:
            case ORG_UNIT:
            case PROCESS:
            case PERSON:
                Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
                return dataFlowDecoratorDao.findByAppIdSelectorAndKind(selector, decoratorEntityKind);
            default:
                throw new UnsupportedOperationException("Cannot find decorators for selector kind: " + options.entityReference().kind());
        }
    }


    /**
     * Find decorators by selector. Supported desiredKinds:
     * <ul>
     *     <li>DATA_TYPE</li>
     *     <li>APPLICATION</li>
     * </ul>
     * @param options
     * @return
     */
    public Collection<DataFlowDecorator> findBySelector(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
            case CAPABILITY:
            case ORG_UNIT:
            case PROCESS:
            case PERSON:
                return findByAppIdSelector(options);
            case DATA_TYPE:
                return findByDataTypeIdSelector(options);
            default:
                throw new UnsupportedOperationException("Cannot find decorators for selector kind: "+ options.entityReference().kind());
        }
    }


    // --- UPDATERS ---

    public int deleteAllDecoratorsForFlowIds(List<Long> flowIds) {
        return dataFlowDecoratorDao.removeAllDecoratorsForFlowIds(flowIds);
    }


    public int[] deleteDecorators(long flowId,
                                  Collection<EntityReference> decoratorReferences,
                                  String username) {
        checkNotNull(decoratorReferences, "decoratorReferences cannot be null");
        LogicalFlow flow = dataFlowDao.findByFlowId(flowId);
        int[] deleted = dataFlowDecoratorDao.deleteDecorators(flowId, decoratorReferences);
        dataTypeUsageService.recalculateForApplications(flow.source(), flow.target());
        audit("Removed", decoratorReferences, flow, username);
        return deleted;
    }



    public int[] addDecorators(long flowId,
                               Set<EntityReference> decoratorReferences,
                               String username) {
        checkNotNull(decoratorReferences, "decoratorReferences cannot be null");
        if (decoratorReferences.isEmpty()) return new int[0];

        Collection<DataFlowDecorator> unrated = map(
                decoratorReferences,
                ref -> ImmutableDataFlowDecorator.builder()
                        .rating(Rating.NO_OPINION)
                        .provenance("waltz")
                        .dataFlowId(flowId)
                        .decoratorEntity(ref)
                        .build());

        Collection<DataFlowDecorator> ratedDecorators = ratingsService
                .calculateRatings(unrated);

        int[] added = dataFlowDecoratorDao.addDecorators(ratedDecorators);
        LogicalFlow flow = dataFlowDao.findByFlowId(flowId);
        dataTypeUsageService.recalculateForApplications(flow.source(), flow.target());
        audit("Added", decoratorReferences, flow, username);
        return added;
    }


    public List<DecoratorRatingSummary> summarizeForSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return dataFlowDecoratorDao.summarizeForSelector(selector);
    }


    // --- HELPERS ---

    private Collection<DataFlowDecorator> findByDataTypeIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(options);
        return dataFlowDecoratorDao.findByDecoratorEntityIdSelectorAndKind(selector, DATA_TYPE);
    }


    private Collection<DataFlowDecorator> findByAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return dataFlowDecoratorDao.findByAppIdSelector(selector);
    }


    private void audit(String verb,
                       Collection<EntityReference> decorators,
                       LogicalFlow flow,
                       String username) {

        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(flow.source())
                .userId(username)
                .severity(Severity.INFORMATION)
                .message(String.format(
                        "%s characteristics: %s, for flow between %s and %s",
                        verb,
                        decorators.toString(),
                        flow.source().name().orElse(Long.toString(flow.source().id())),
                        flow.target().name().orElse(Long.toString(flow.target().id()))))
                .build();

        changeLogService.write(logEntry);
        changeLogService.write(logEntry.withParentReference(flow.target()));

    }

}
