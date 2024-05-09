package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.datatype.FlowDataType;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchy;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.immutables.value.Value;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.orderedGroupBy;
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

        Function2<FlowClassificationRuleVantagePoint, FlowDataType, MatchOutcome> matcher = determineMatcherFn(direction);

        Map<Long, Tuple2<Long, MatchOutcome>> lfdIdToRuleAndOutcomeMap = new HashMap<>();

        Set<Long> ruleDataTypes = population
                .stream()
                .flatMap(d -> dtHierarchy.findAncestors(d.dtId()).stream())
                .collect(Collectors.toSet());

        List<FlowClassificationRuleVantagePoint> filteredRules = ruleVantagePoints
                .stream()
                .filter(rvp -> rvp.dataTypeId() == null || ruleDataTypes.contains(rvp.dataTypeId()))
                .collect(Collectors.toList());

        TreeMap<BucketKey, Collection<FlowClassificationRuleVantagePoint>> bucketedRules = bucketRules(filteredRules);

        bucketedRules
                .entrySet()
                .forEach(kv -> {

                    BucketKey bucketKey = kv.getKey();

                    Set<Long> childOUs = ouHierarchy.findChildren(bucketKey.vantagePoint().id());
                    Set<Long> childDTs = dtHierarchy.findChildren(bucketKey.dataTypeId());

                    Predicate<FlowDataType> bucketMatcher = mkBucketMatcher(direction, bucketKey, childOUs, childDTs);

                    population.forEach(p -> {

                        //Can skip the entire population if another bucket has resolved the decorator
                        Tuple2<Long, MatchOutcome> previousOutcome = lfdIdToRuleAndOutcomeMap.get(p.lfdId());
                        if (previousOutcome != null && previousOutcome.v2 == MatchOutcome.POSITIVE_MATCH) {
                            return; // skip, already got a good match
                        }

                    //    System.out.printf("Testing bucketKey: %s against flow: %s - outcome: %s\n", bucketKey, p, bucketMatcher.test(p));
                        if (bucketMatcher.test(p)) {
                            Collection<FlowClassificationRuleVantagePoint> rvps = kv.getValue();
                            rvps
                                .forEach(rvp -> {
                                    Tuple2<Long, MatchOutcome> currentRuleAndOutcome = lfdIdToRuleAndOutcomeMap.get(p.lfdId());
                                    if (currentRuleAndOutcome != null && currentRuleAndOutcome.v2 == MatchOutcome.POSITIVE_MATCH) {
                                        return; // skip, already got a good match
                                    }
                                    MatchOutcome outcome = matcher.apply(rvp, p);
                                    if (currentRuleAndOutcome == null) {
                                        lfdIdToRuleAndOutcomeMap.put(p.lfdId(), tuple(rvp.ruleId(), outcome));
                                    } else if (currentRuleAndOutcome.v2 == MatchOutcome.NEGATIVE_MATCH && outcome == MatchOutcome.POSITIVE_MATCH) {
                                        // override result as we have a positive match
                                        lfdIdToRuleAndOutcomeMap.put(p.lfdId(), tuple(rvp.ruleId(), MatchOutcome.POSITIVE_MATCH));
                                    } else {
                                        // skip, leave the map alone as a more specific negative rule id already exists
                                    }
                                });
                        }
                    });
                });

        return lfdIdToRuleAndOutcomeMap;
    }

    private static Predicate<FlowDataType> mkBucketMatcher(FlowDirection direction, BucketKey bucketKey, Set<Long> childOUs, Set<Long> childDTs) {

        if (direction.equals(FlowDirection.INBOUND)) {
            return p -> {
                boolean dtMatches = bucketKey.dataTypeId() == null || childDTs.contains(p.dtId());
                return dtMatches && checkScopeMatches(bucketKey, childOUs, p.source(), p.sourceOuId());
            };
        } else {
            return p -> {
                boolean dtMatches = bucketKey.dataTypeId() == null || childDTs.contains(p.dtId());
                return dtMatches && checkScopeMatches(bucketKey, childOUs, p.target(), p.targetOuId());
            };
        }
    }


    private static TreeMap<BucketKey, Collection<FlowClassificationRuleVantagePoint>> bucketRules(List<FlowClassificationRuleVantagePoint> targetedPopRules) {


        Comparator<BucketKey> kindComparator = Comparator.comparing(t -> t.vantagePoint().kind().name());
        Comparator<BucketKey> vpRankComparator = Comparator.comparingInt(BucketKey::vantagePointRank);
        Comparator<BucketKey> dtRankComparator = Comparator.comparingInt(BucketKey::dataTypeRank);

        TreeMap<BucketKey, Collection<FlowClassificationRuleVantagePoint>> groups = orderedGroupBy(
                targetedPopRules,
                d -> ImmutableBucketKey.builder()
                        .vantagePoint(d.vantagePoint())
                        .dataTypeId(d.dataTypeId())
                        .vantagePointRank(d.vantagePointRank())
                        .dataTypeRank(d.dataTypeRank())
                        .build(),
                d -> d,
                kindComparator
                        .thenComparing(vpRankComparator.reversed())
                        .thenComparing(dtRankComparator.reversed())
                        .thenComparingLong(d -> d.vantagePoint().id())
                        .thenComparingLong(d -> d.dataTypeId() == null ? Long.MAX_VALUE : d.dataTypeId()));

        return groups;
    }

    @Value.Immutable
    public interface BucketKey {

        EntityReference vantagePoint();

        @Nullable
        Long dataTypeId();

        @Value.Auxiliary
        Integer vantagePointRank();

        @Value.Auxiliary
        Integer dataTypeRank();

    }

    enum MatchOutcome {
        NOT_APPLICABLE,
        NEGATIVE_MATCH,
        POSITIVE_MATCH

    }

    private static Function2<FlowClassificationRuleVantagePoint, FlowDataType, MatchOutcome> determineMatcherFn(FlowDirection direction) {
        Function2<FlowClassificationRuleVantagePoint, FlowDataType, MatchOutcome> inboundMatcher =
                (rvp, p) -> {
                    boolean subjectMatches = p.target().equals(rvp.subjectReference());
                    if (subjectMatches) {
                        return MatchOutcome.POSITIVE_MATCH;
                    } else {
                        return MatchOutcome.NEGATIVE_MATCH;
                    }
                };

        Function2<FlowClassificationRuleVantagePoint, FlowDataType, MatchOutcome> outboundMatcher =
                (rvp, p) -> {
                    boolean subjectMatches = p.source().equals(rvp.subjectReference());
                    if (subjectMatches) {
                        return MatchOutcome.POSITIVE_MATCH;
                    } else {
                        return MatchOutcome.NEGATIVE_MATCH;
                    }
                };

        return direction == FlowDirection.INBOUND
                ? inboundMatcher
                : outboundMatcher;
    }


    private static Function4<FlowClassificationRuleVantagePoint, Set<Long>, Set<Long>, FlowDataType, MatchOutcome> determineMatcherFnOld(FlowDirection direction) {
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
                                             EntityReference scopeEntity,
                                             Long scopeEntityOuId) {
        if (rvp.vantagePoint().kind() == EntityKind.ORG_UNIT) {
            return scopeEntityOuId != null && childOUs.contains(scopeEntityOuId);
        } else {
            // point-to-point flows e.g. ACTOR or APPLICATION
            return scopeEntity.equals(rvp.vantagePoint());
        }
    }

    private static boolean checkScopeMatches(BucketKey bucketKey,
                                             Set<Long> childOUs,
                                             EntityReference scopeEntity,
                                             Long scopeEntityOuId) {
        if (bucketKey.vantagePoint().kind() == EntityKind.ORG_UNIT) {
            return scopeEntityOuId != null && childOUs.contains(scopeEntityOuId);
        } else {
            // point-to-point flows e.g. ACTOR or APPLICATION
            return scopeEntity.equals(bucketKey.vantagePoint());
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
