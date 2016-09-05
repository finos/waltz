package com.khartec.waltz.service.data_flow_decorator;

import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.utils.IdUtilities.indexById;

@Service
public class DataFlowDecoratorRatingsService {

    private final ApplicationService applicationService;
    private final AuthoritativeSourceDao authoritativeSourceDao;
    private final DataFlowDao dataFlowDao;
    private final DataTypeDao dataTypeDao;


    @Autowired
    public DataFlowDecoratorRatingsService(ApplicationService applicationService,
                                           AuthoritativeSourceDao authoritativeSourceDao,
                                           DataFlowDao dataFlowDao, 
                                           DataTypeDao dataTypeDao) {
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao cannot be null");
        checkNotNull(dataFlowDao, "dataFlowDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");

        this.applicationService = applicationService;
        this.authoritativeSourceDao = authoritativeSourceDao;
        this.dataFlowDao = dataFlowDao;
        this.dataTypeDao = dataTypeDao;
    }


    public Collection<DataFlowDecorator> calculateRatings(Collection<DataFlowDecorator> decorators) {

        List<DataFlow> flows = loadFlows(decorators);
        List<Application> targetApps = loadTargetApplications(flows);
        List<DataType> dataTypes = dataTypeDao.getAll();

        Map<Long, DataType> typesById = indexById(dataTypes);
        Map<Long, DataFlow> flowsById = indexById(flows);
        Map<Long, Application> targetAppsById = indexById(targetApps);

        AuthoritativeSourceResolver resolver = createResolver(targetApps);

        return map(
                decorators,
                decorator -> {
                    if (decorator.decoratorEntity().kind() != EntityKind.DATA_TYPE) {
                        return decorator;
                    } else {
                        Rating rating = lookupRating(
                                typesById,
                                flowsById,
                                targetAppsById,
                                resolver,
                                decorator);
                        return ImmutableDataFlowDecorator
                                .copyOf(decorator)
                                .withRating(rating);
                    }
                });
    }


    private Rating lookupRating(Map<Long, DataType> typesById,
                                Map<Long, DataFlow> flowsById,
                                Map<Long, Application> targetAppsById,
                                AuthoritativeSourceResolver resolver,
                                DataFlowDecorator decorator) {
        DataFlow flow = flowsById.get(decorator.dataFlowId());

        EntityReference vantagePoint = lookupVantagePoint(targetAppsById, flow);
        EntityReference source = flow.source();
        String dataTypeCode = lookupDataTypeCode(typesById, decorator);

        return resolver.resolve(vantagePoint, source, dataTypeCode);
    }


    private EntityReference lookupVantagePoint(Map<Long, Application> targetAppsById, DataFlow flow) {
        long targetOrgUnitId = targetAppsById.get(flow.target().id()).organisationalUnitId();

        return EntityReference.mkRef(
                EntityKind.ORG_UNIT,
                targetOrgUnitId);
    }


    private String lookupDataTypeCode(Map<Long, DataType> typesById, DataFlowDecorator decorator) {
        long dataTypeId = decorator.decoratorEntity().id();
        return typesById.get(dataTypeId).code();
    }


    private List<Application> loadTargetApplications(List<DataFlow> flows) {
        Set<Long> targetApplicationIds = map(
                flows,
                df -> df.target().id());

        return applicationService
                .findByIds(targetApplicationIds);
    }


    private List<DataFlow> loadFlows(Collection<DataFlowDecorator> decorators) {
        Set<Long> dataFlowIds = map(decorators, d -> d.dataFlowId());
        return dataFlowDao.findByFlowIds(dataFlowIds);
    }


    private AuthoritativeSourceResolver createResolver(Collection<Application> targetApps) {
        Set<Long> orgIds = map(targetApps, app -> app.organisationalUnitId());

        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints =
                authoritativeSourceDao.findAuthoritativeRatingVantagePoints(orgIds);

        AuthoritativeSourceResolver resolver = new AuthoritativeSourceResolver(authoritativeRatingVantagePoints);
        return resolver;
    }

}