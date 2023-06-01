package org.finos.waltz.jobs.tools.lineage;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.immutables.value.Value;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public abstract class RouteLookup {
    public abstract ExternalIdValue source();
    public abstract  ExternalIdValue target();

    public abstract  Optional<Application> sourceApp();
    public abstract  Optional<Application> targetApp();

    public abstract Optional<Tuple2<Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>>> nonStrictRoute();
    public abstract List<Tuple3<String, Graph<EntityReference, DataTypeEdge>, GraphPath<EntityReference, DataTypeEdge>>> strictRoutes();

    public abstract List<String> processingMessages();

}
