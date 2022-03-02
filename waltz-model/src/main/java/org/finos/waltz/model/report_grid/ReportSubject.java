package org.finos.waltz.model.report_grid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.LifecyclePhase;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReportSubject.class)
@JsonDeserialize(as = ImmutableReportSubject.class)
public abstract class ReportSubject {

    public abstract EntityReference entityReference();

    public abstract LifecyclePhase lifecyclePhase();

}
