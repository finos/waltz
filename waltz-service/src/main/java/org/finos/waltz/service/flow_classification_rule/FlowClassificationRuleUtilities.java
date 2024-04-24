package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.datatype.FlowDataType;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchy;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static org.jooq.lambda.tuple.Tuple.tuple;

public class FlowClassificationRuleUtilities {

    public static final Function<FlowClassificationRuleVantagePoint, String> kindComparator = d -> d.vantagePoint().kind().name();
    public static final Comparator<FlowClassificationRuleVantagePoint> vantageComparator = Comparator.comparingInt(FlowClassificationRuleVantagePoint::vantagePointRank).reversed();
    public static final Comparator<FlowClassificationRuleVantagePoint> dataTypeComparator = Comparator.comparingInt(FlowClassificationRuleVantagePoint::dataTypeRank).reversed();
    public static final ToLongFunction<FlowClassificationRuleVantagePoint> vantagePointIdComparator = d -> d.vantagePoint().id();
    public static final ToLongFunction<FlowClassificationRuleVantagePoint> dataTypeIdComparator = FlowClassificationRuleVantagePoint::dataTypeId;
    public static final ToLongFunction<FlowClassificationRuleVantagePoint> subjectIdComparator = d -> d.subjectReference().id();
    public static final Comparator<FlowClassificationRuleVantagePoint> flowClassificationRuleVantagePointComparator = Comparator
            .comparing(kindComparator)
            .thenComparing(vantageComparator)
            .thenComparing(dataTypeComparator)
            .thenComparingLong(vantagePointIdComparator)
            .thenComparingLong(dataTypeIdComparator)
            .thenComparingLong(subjectIdComparator);


    protected static Map<Long, Tuple2<Long, MatchOutcome>> applyVantagePoints(FlowDirection direction,
                                                                              List<FlowClassificationRuleVantagePoint> ruleVantagePoints,
                                                                              Set<FlowDataType> population,
                                                                              EntityHierarchy ouHierarchy,
                                                                              EntityHierarchy dtHierarchy) {

        Function4<FlowClassificationRuleVantagePoint, Set<Long>, Set<Long>, FlowDataType, MatchOutcome> matcher = determineMatcherFn(direction);

        Map<Long, Tuple2<Long, MatchOutcome>> lfdIdToRuleAndOutcomeMap = new HashMap<>();

        Set<Long> ruleDataTypes = population
                .stream()
                .flatMap(d -> dtHierarchy.findAncestors(d.dtId()).stream())
                .collect(Collectors.toSet());

        Set<FlowClassificationRuleVantagePoint> filteredRules = ruleVantagePoints
                .stream()
                .filter(rvp -> rvp.dataTypeId() == null || ruleDataTypes.contains(rvp.dataTypeId()))
                .collect(Collectors.toSet());

        filteredRules
                .forEach(rvp -> {
                    Set<Long> childOUs = ouHierarchy.findChildren(rvp.vantagePoint().id());
                    Set<Long> childDTs = dtHierarchy.findChildren(rvp.dataTypeId());
                    population.forEach(p -> {
                        Tuple2<Long, MatchOutcome> currentRuleAndOutcome = lfdIdToRuleAndOutcomeMap.get(p.lfdId());
                        if (currentRuleAndOutcome != null && currentRuleAndOutcome.v2 == MatchOutcome.POSITIVE_MATCH) {
                            return; // skip, already got a good match
                        }
                        MatchOutcome outcome = matcher.apply(rvp, childOUs, childDTs, p);
                        if (outcome == MatchOutcome.NOT_APPLICABLE) {
                            // skip
                        } else if (currentRuleAndOutcome == null) {
                            lfdIdToRuleAndOutcomeMap.put(p.lfdId(), tuple(rvp.ruleId(), outcome));
                        } else if (currentRuleAndOutcome.v2 == MatchOutcome.NEGATIVE_MATCH && outcome == MatchOutcome.POSITIVE_MATCH) {
                            // override result as we have a positive match
                            lfdIdToRuleAndOutcomeMap.put(p.lfdId(), tuple(rvp.ruleId(), MatchOutcome.POSITIVE_MATCH));
                        } else {
                            // skip, leave the map alone as a more specific negative rule id already exists
                        }
                    });
                });

        return lfdIdToRuleAndOutcomeMap;
    }

    enum MatchOutcome {
        NOT_APPLICABLE,
        NEGATIVE_MATCH,
        POSITIVE_MATCH

    }

    private static Function4<FlowClassificationRuleVantagePoint, Set<Long>, Set<Long>, FlowDataType, MatchOutcome> determineMatcherFn(FlowDirection direction) {
        Function4<FlowClassificationRuleVantagePoint,  Set<Long>,  Set<Long>, FlowDataType, MatchOutcome> inboundMatcher =
                (rvp, childOUs, childDTs, p) -> {
                    boolean subjectMatches = p.target().equals(rvp.subjectReference());
                    boolean dtMatches = rvp.dataTypeId() == null || childDTs.contains(p.dtId());
                    boolean dtAndOuMatches = dtMatches && checkScopeMatches(rvp, childOUs, p.source(), p.sourceOuId());
                    return determineOutcome(subjectMatches, dtAndOuMatches);
                };

        Function4<FlowClassificationRuleVantagePoint,  Set<Long>,  Set<Long>, FlowDataType, MatchOutcome> outboundMatcher =
                (rvp, childOUs, childDTs, p) -> {
                    boolean subjectMatches = p.source().equals(rvp.subjectReference());
                    boolean dtMatches = rvp.dataTypeId() == null || childDTs.contains(p.dtId());
                    boolean dtAndOuMatches = dtMatches && checkScopeMatches(rvp, childOUs, p.target(), p.targetOuId());
                    return determineOutcome(subjectMatches, dtAndOuMatches);
                };

        return direction == FlowDirection.INBOUND
                ? inboundMatcher
                : outboundMatcher;
    }

    private static boolean checkScopeMatches(FlowClassificationRuleVantagePoint rvp,
                                             Set<Long> childOUs,
                                             EntityReference scopeEntity, Long scopeEntityOuId) {
        if (rvp.vantagePoint().kind() == EntityKind.ORG_UNIT) {
            return scopeEntityOuId != null && childOUs.contains(scopeEntityOuId);
        } else {
            // point-to-point flows e.g. ACTOR or APPLICATION
            return scopeEntity.equals(rvp.vantagePoint());
        }
    }


    private static MatchOutcome determineOutcome(boolean subjectMatches,
                                                 boolean dtAndOuMatches) {
        if (subjectMatches && dtAndOuMatches) {
            return MatchOutcome.POSITIVE_MATCH;
        } else if (dtAndOuMatches) {
            return MatchOutcome.NEGATIVE_MATCH;
        } else {
            return MatchOutcome.NOT_APPLICABLE;
        }
    }

}
