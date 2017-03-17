package com.khartec.waltz.service.entity_search;

import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.WaltzEntity;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.service.actor.ActorService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.change_initiative.ChangeInitiativeService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.person.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Unchecked.supplier;

@Service
public class EntitySearchService {

    private final DBExecutorPoolInterface dbExecutorPool;
    private final ActorService actorService;
    private final ApplicationService applicationService;
    private final ChangeInitiativeService changeInitiativeService;
    private final MeasurableService measurableService;
    private final OrganisationalUnitService organisationalUnitService;
    private final PersonService personService;


    @Autowired
    public EntitySearchService(DBExecutorPoolInterface dbExecutorPool,
                               ActorService actorService,
                               ApplicationService applicationService,
                               ChangeInitiativeService changeInitiativeService,
                               MeasurableService measurableService,
                               OrganisationalUnitService organisationalUnitService,
                               PersonService personService) {
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(actorService, "actorService cannot be null");
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(changeInitiativeService, "changeInitiativeService cannot be null");
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService cannot be null");
        checkNotNull(personService, "personService cannot be null");

        this.actorService = actorService;
        this.dbExecutorPool = dbExecutorPool;
        this.applicationService = applicationService;
        this.changeInitiativeService = changeInitiativeService;
        this.measurableService = measurableService;
        this.organisationalUnitService = organisationalUnitService;
        this.personService = personService;
    }


    public List<EntityReference> search(String terms, EntitySearchOptions options) {
        checkNotNull(terms, "terms cannot be null");
        checkNotNull(options, "options cannot be null");

        List<Future<Collection<? extends WaltzEntity>>> futures = options.entityKinds().stream()
                .map(ek -> dbExecutorPool.submit(mkCallable(ek, terms, options)))
                .collect(toList());

        return futures.stream()
                .flatMap(f -> supplier(f::get).get().stream())
                .map(WaltzEntity::entityReference)
                .collect(toList());
    }


    private Callable<Collection<? extends WaltzEntity>> mkCallable(EntityKind entityKind,
                                                                   String terms,
                                                                   EntitySearchOptions options) {
        switch (entityKind) {
            case ACTOR:
                return () -> actorService.search(terms, options);
            case APPLICATION:
                return () -> applicationService.search(terms, options);
            case CHANGE_INITIATIVE:
                return () -> changeInitiativeService.search(terms, options);
            case MEASURABLE:
                return () -> measurableService.search(terms, options);
            case ORG_UNIT:
                return () -> organisationalUnitService.search(terms, options);
            case PERSON:
                return () -> personService.search(terms, options);
            default:
                throw new UnsupportedOperationException("no search service available for: " + entityKind);
        }
    }
}
