package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.ChangeDirection;
import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.finos.waltz.model.aggregate_overlay_diagram.overlay.QuarterDetail.mkQuarterDetail;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationChangeWidgetDatum.class)
public abstract class ApplicationChangeWidgetDatum implements CellExternalIdProvider {


    @Value.Derived
    public Set<DateChangeInformation> inboundCounts() {
        return applicationChanges()
                .stream()
                .filter(e -> e.changeDirection().equals(ChangeDirection.INBOUND))
                .map(e -> tuple(mkQuarterDetail(e.date()), e.appId()))
                .collect(groupingBy(Tuple2::v1, Collectors.toSet()))
                .entrySet()
                .stream()
                .map(d -> ImmutableDateChangeInformation
                        .builder()
                        .quarter(d.getKey())
                        .count(d.getValue().size())
                        .build())
                .collect(Collectors.toSet());
    }


    @Value.Derived
    public Set<DateChangeInformation> outboundCounts() {
        return applicationChanges()
                .stream()
                .filter(e -> e.changeDirection().equals(ChangeDirection.OUTBOUND))
                .map(e -> tuple(mkQuarterDetail(e.date()), e.appId()))
                .collect(groupingBy(Tuple2::v1, Collectors.toSet()))
                .entrySet()
                .stream()
                .map(d -> ImmutableDateChangeInformation
                        .builder()
                        .quarter(d.getKey())
                        .count(d.getValue().size())
                        .build())
                .collect(Collectors.toSet());
    }


    public abstract Set<AppChangeEntry> applicationChanges();
    public abstract int currentAppCount();

}
