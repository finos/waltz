package com.khartec.waltz.service.data_flow_decorator;


import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.data_flow_decorator.DataFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.model.EntityKind.APPLICATION;
import static com.khartec.waltz.model.EntityKind.DATA_TYPE;

@Service
public class DataFlowDecoratorService {

    private final DataFlowDecoratorDao dataFlowDecoratorDao;
    private final DataFlowDecoratorRatingsService ratingsService;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final DataTypeUsageService dataTypeUsageService;
    private final DataFlowDao dataFlowDao;

    @Autowired
    public DataFlowDecoratorService(DataFlowDecoratorDao dataFlowDecoratorDao,
                                    DataFlowDecoratorRatingsService ratingsService,
                                    ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                    DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                    DataTypeUsageService dataTypeUsageService,
                                    DataFlowDao dataFlowDao) {
        checkNotNull(dataFlowDecoratorDao, "dataFlowDecoratorDao cannot be null");
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(ratingsService, "ratingsService cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(dataFlowDao, "dataFlowDao cannot be null");

        this.dataFlowDecoratorDao = dataFlowDecoratorDao;
        this.ratingsService = ratingsService;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataFlowDao = dataFlowDao;
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

        checkTrue(
                options.desiredKind() == APPLICATION,
                "Cannot find with desired kind " + options.desiredKind());

        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return dataFlowDecoratorDao.findByAppIdSelectorAndKind(selector, decoratorEntityKind);
    }


    public List<DataFlowDecorator> findByFlowIdsAndKind(Collection<Long> flowIds,
                                                        EntityKind decoratorEntityKind) {
        checkNotNull(flowIds, "flowIds cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        return dataFlowDecoratorDao.findByFlowIdsAndKind(flowIds, decoratorEntityKind);
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
        if (options.desiredKind() == DATA_TYPE) {
            return findByDataTypeIdSelector(options);
        }
        if (options.desiredKind() == APPLICATION) {
            return findByAppIdSelector(options);
        }
        String message = String.format("Cannot find decorators for selector desiredKind: %s", options.desiredKind());
        throw new UnsupportedOperationException(message);
    }


    // --- UPDATERS ---

    public int deleteAllDecoratorsForFlowIds(List<Long> flowIds) {
        return dataFlowDecoratorDao.removeAllDecoratorsForFlowIds(flowIds);
    }


    public int[] deleteDecorators(long flowId,
                                  Collection<EntityReference> decoratorReferences) {
        checkNotNull(decoratorReferences, "decoratorReferences cannot be null");
        DataFlow flow = dataFlowDao.findByFlowId(flowId);
        int[] deleted = dataFlowDecoratorDao.deleteDecorators(flowId, decoratorReferences);
        dataTypeUsageService.recalculateForApplications(flow.source(), flow.target());
        return deleted;
    }


    public int[] addDecorators(long flowId, Set<EntityReference> decoratorReferences) {
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
        DataFlow flow = dataFlowDao.findByFlowId(flowId);
        dataTypeUsageService.recalculateForApplications(flow.source(), flow.target());
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


}
