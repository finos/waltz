package com.khartec.waltz.service.data_flow_decorator;


import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow_decorator.DataFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
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


    @Autowired
    public DataFlowDecoratorService(DataFlowDecoratorDao dataFlowDecoratorDao,
                                    DataFlowDecoratorRatingsService ratingsService,
                                    ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                    DataTypeIdSelectorFactory dataTypeIdSelectorFactory) {
        checkNotNull(dataFlowDecoratorDao, "dataFlowDecoratorDao cannot be null");
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(ratingsService, "ratingsService cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");

        this.dataFlowDecoratorDao = dataFlowDecoratorDao;
        this.ratingsService = ratingsService;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
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
        throw new UnsupportedOperationException("Cannot find decorators for selector desiredKind: "+options.desiredKind());
    }


    // --- UPDATERS ---

    public int[] deleteDecorators(long flowId,
                                  Collection<EntityReference> decoratorReferences) {
        checkNotNull(decoratorReferences, "decoratorReferences cannot be null");
        return dataFlowDecoratorDao.deleteDecorators(flowId, decoratorReferences);
    }


    public int[] addDecorators(Long flowId, Set<EntityReference> decoratorReferences) {
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

        return dataFlowDecoratorDao
                .addDecorators(ratedDecorators);
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
