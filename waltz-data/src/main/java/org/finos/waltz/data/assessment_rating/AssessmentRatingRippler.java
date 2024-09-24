package org.finos.waltz.data.assessment_rating;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.common.Checks;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.settings.SettingsDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.PairDiffResult;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobStep;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentRipplerJobConfiguration;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Actor;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.PhysicalFlow;
import org.finos.waltz.schema.tables.PhysicalSpecDataType;
import org.finos.waltz.schema.tables.PhysicalSpecification;
import org.finos.waltz.schema.tables.RatingScheme;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.StringUtilities.safeEq;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.PairDiffResult.mkPairDiff;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class AssessmentRatingRippler {

    private static final Logger LOG = LoggerFactory.getLogger(AssessmentRatingRippler.class);

    private static final PhysicalFlow pf = Tables.PHYSICAL_FLOW;
    private static final PhysicalSpecification ps = Tables.PHYSICAL_SPECIFICATION;
    private static final PhysicalSpecDataType psdt = Tables.PHYSICAL_SPEC_DATA_TYPE;
    private static final AssessmentDefinition ad = Tables.ASSESSMENT_DEFINITION;
    private static final AssessmentRating ar = Tables.ASSESSMENT_RATING;
    private static final Application app = Tables.APPLICATION;
    private static final Actor act = Tables.ACTOR;
    private static final LogicalFlow lf = Tables.LOGICAL_FLOW;
    private static final RatingSchemeItem rsi = Tables.RATING_SCHEME_ITEM;
    private static final RatingScheme rs = Tables.RATING_SCHEME;
    private static final MeasurableRating mr = Tables.MEASURABLE_RATING;
    private static final Measurable m = Tables.MEASURABLE;

    private final DSLContext dsl;
    private final SettingsDao settingsDao;

    @Autowired
    public AssessmentRatingRippler(DSLContext dsl,
                                   SettingsDao settingsDao) {
        this.dsl = dsl;
        this.settingsDao = settingsDao;
    }


    public static AssessmentRipplerJobConfiguration parseConfig(String name,
                                                                String value) throws JsonProcessingException {
        ObjectMapper jsonMapper = getJsonMapper();
        AssessmentRipplerJobStep[] steps = jsonMapper.readValue(value, AssessmentRipplerJobStep[].class);

        return ImmutableAssessmentRipplerJobConfiguration
                .builder()
                .name(name)
                .steps(asList(steps))
                .build();
    }


    public final void rippleAssessments() {
        Map<String, String> configEntries = settingsDao
                .indexByPrefix("job.RIPPLE_ASSESSMENTS.");

        dsl.transaction(ctx -> {
            DSLContext tx = ctx.dsl();
            configEntries
                    .entrySet()
                    .stream()
                    .flatMap(kv -> {
                        String key = kv.getKey();
                        String value = kv.getValue();

                        String rippleName = key.replaceAll("^job.RIPPLE_ASSESSMENTS.", "");
                        LOG.debug("Parsing config ripple : {} , json: {}", rippleName, value);

                        try {
                            AssessmentRipplerJobConfiguration config = parseConfig(rippleName, value);
                            return config.steps().stream();
                        } catch (JsonProcessingException e) {
                            LOG.error("Could not process assessment rippler job: " + rippleName, e);
                            return Stream.empty();
                        }
                    })
                    .forEach(step -> rippleAssessment(
                            tx,
                            "waltz",
                            "waltz-assessment-rippler",
                            step.fromDef(),
                            step.toDef()));
        });
    }


    public static void rippleAssessment(DSLContext tx,
                                        String userId,
                                        String provenance,
                                        String from,
                                        String to) {

        Map<String, AssessmentDefinitionRecord> defs = tx
                .selectFrom(ad)
                .where(ad.EXTERNAL_ID.in(from, to))
                .fetchMap(r -> r.get(ad.EXTERNAL_ID));

        AssessmentDefinitionRecord fromDef = defs.get(from);
        Checks.checkNotNull(fromDef, "Cannot ripple assessment as definition: %s not found", from);
        AssessmentDefinitionRecord toDef = defs.get(to);
        Checks.checkNotNull(toDef, "Cannot ripple assessment as definition: %s not found", toDef);
        rippleAssessment(tx, userId, provenance, fromDef, toDef);
    }


    private static void rippleAssessment(DSLContext tx,
                                         String userId,
                                         String provenance,
                                         AssessmentDefinitionRecord from,
                                         AssessmentDefinitionRecord to) {
        checkTrue(
                from.getRatingSchemeId().equals(to.getRatingSchemeId()),
                "Assessments must share a rating scheme when rippling (%s -> %s)",
                from.getName(),
                to.getName());

        Tuple2<EntityKind, EntityKind> kinds = tuple(
                EntityKind.valueOf(from.getEntityKind()),
                EntityKind.valueOf(to.getEntityKind()));

        if (kinds.equals(tuple(EntityKind.PHYSICAL_SPECIFICATION, EntityKind.PHYSICAL_FLOW))) {
            // PHYSICAL_SPEC -> PHYSICAL_FLOW
            rippleAssessments(
                    tx,
                    userId,
                    provenance,
                    from,
                    to,
                    tx.select(pf.ID, ar.RATING_ID, ps.ID, ps.NAME)
                            .from(ar)
                            .innerJoin(ps)
                            .on(ps.ID.eq(ar.ENTITY_ID))
                            .innerJoin(pf)
                            .on(pf.SPECIFICATION_ID.eq(ps.ID))
                            .where(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId())
                                    .and(ps.IS_REMOVED.isFalse())));
        } else if (kinds.equals(tuple(EntityKind.PHYSICAL_FLOW, EntityKind.LOGICAL_DATA_FLOW))) {
            // PHYSICAL_FLOW -> LOGICAL
            rippleAssessments(
                    tx,
                    userId,
                    provenance,
                    from,
                    to,
                    tx.select(lf.ID, ar.RATING_ID, pf.ID, pf.NAME)
                            .from(ar)
                            .innerJoin(pf).on(pf.ID.eq(ar.ENTITY_ID))
                            .innerJoin(lf).on(lf.ID.eq(pf.LOGICAL_FLOW_ID))
                            .where(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId())
                                    .and(pf.IS_REMOVED.isFalse())
                                    .and(pf.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))));
        } else if (kinds.v1 == EntityKind.LOGICAL_DATA_FLOW && (kinds.v2 == EntityKind.APPLICATION || kinds.v2 == EntityKind.ACTOR)) {
            // LOGICAL -> APP | ACTOR
            Condition lfIsActiveCondition = lf.IS_REMOVED.isFalse()
                    .and(lf.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()));
            Actor sourceActor = act.as("source_actor");
            Actor targetActor = act.as("target_actor");
            Application sourceApp = app.as("source_app");
            Application targetApp = app.as("target_app");
            Condition sourceActorJoinCondition = lf.SOURCE_ENTITY_KIND.eq(EntityKind.ACTOR.name()).and(sourceActor.ID.eq(lf.SOURCE_ENTITY_ID));
            Condition targetActorJoinCondition = lf.TARGET_ENTITY_KIND.eq(EntityKind.ACTOR.name()).and(targetActor.ID.eq(lf.TARGET_ENTITY_ID));
            Condition sourceAppJoinCondition = lf.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(sourceApp.ID.eq(lf.SOURCE_ENTITY_ID));
            Condition targetAppJoinCondition = lf.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(targetApp.ID.eq(lf.TARGET_ENTITY_ID));
            Field<String> sourceName = DSL.coalesce(sourceApp.NAME, sourceActor.NAME, DSL.value("??"));
            Field<String> targetName = DSL.coalesce(targetApp.NAME, targetActor.NAME, DSL.value("??"));
            Field<String> flowDesc = DSL.concat(
                    DSL.value("Flow: "),
                    sourceName,
                    DSL.value(" -> "),
                    targetName);
            rippleAssessments(
                    tx,
                    userId,
                    provenance,
                    from,
                    to,
                    tx.select(lf.SOURCE_ENTITY_ID, ar.RATING_ID, lf.ID, flowDesc)
                            .from(ar)
                            .innerJoin(lf).on(lf.ID.eq(ar.ENTITY_ID))
                            .leftJoin(sourceApp).on(sourceAppJoinCondition)
                            .leftJoin(targetApp).on(targetAppJoinCondition)
                            .leftJoin(sourceActor).on(sourceActorJoinCondition)
                            .leftJoin(targetActor).on(targetActorJoinCondition)
                            .where(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId())
                                    .and(lf.SOURCE_ENTITY_KIND.eq(kinds.v2.name()))
                                    .and(lfIsActiveCondition))
                            .union(DSL
                                    .select(lf.TARGET_ENTITY_ID, ar.RATING_ID, lf.ID, flowDesc)
                                    .from(ar)
                                    .innerJoin(lf).on(lf.ID.eq(ar.ENTITY_ID))
                                    .leftJoin(sourceApp).on(sourceAppJoinCondition)
                                    .leftJoin(targetApp).on(targetAppJoinCondition)
                                    .leftJoin(sourceActor).on(sourceActorJoinCondition)
                                    .leftJoin(targetActor).on(targetActorJoinCondition)
                                    .where(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId())
                                            .and(lf.TARGET_ENTITY_KIND.eq(kinds.v2.name()))
                                            .and(lfIsActiveCondition))));
        } else if (kinds.v1 == EntityKind.MEASURABLE && kinds.v2 == EntityKind.APPLICATION) {
            // MEASURABLE -> APPLICTION
            rippleAssessments(
                    tx,
                    userId,
                    provenance,
                    from,
                    to,
                    tx.select(mr.ENTITY_ID, ar.RATING_ID, m.ID, m.NAME)
                            .from(ar)
                            .innerJoin(mr).on(mr.MEASURABLE_ID.eq(ar.ENTITY_ID))
                            .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                            .where(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId()))
                            .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
        } else if (kinds.v1 == EntityKind.MEASURABLE && kinds.v2 == EntityKind.MEASURABLE_RATING) {
            // MEASURABLE -> MEASURABLE_RATING
            rippleAssessments(
                    tx,
                    userId,
                    provenance,
                    from,
                    to,
                    tx.select(mr.ID, ar.RATING_ID, m.ID, m.NAME)
                            .from(ar)
                            .innerJoin(mr).on(mr.MEASURABLE_ID.eq(ar.ENTITY_ID))
                            .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                            .where(ar.ASSESSMENT_DEFINITION_ID.eq(from.getId()))
                            .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
        } else {
            throw new UnsupportedOperationException(format(
                    "Cannot ripple assessment from kind: %s to kind: %s",
                    kinds.v1,
                    kinds.v2));
        }
    }


    private static void rippleAssessments(DSLContext tx,
                                          String userId,
                                          String provenance,
                                          AssessmentDefinitionRecord from,
                                          AssessmentDefinitionRecord to,
                                          Select<Record4<Long, Long, Long, String>> targetAndRatingProvider) {
        Timestamp now = nowUtcTimestamp();
        Set<AssessmentRatingRecord> required = MapUtilities
                .groupAndThen(
                        targetAndRatingProvider.fetch(),
                        r -> tuple(r.get(0, Long.class), r.get(1, Long.class)),
                        xs -> xs.stream()
                                .map(x -> mkRef(
                                        EntityKind.valueOf(from.getEntityKind()),
                                        x.get(2, Long.class),
                                        x.get(3, String.class)))
                                .sorted(Comparator.comparing(d -> d.name().orElse("??")))
                                .collect(Collectors.toList()))
                .entrySet()
                .stream()
                .map(kv -> {
                    String desc = calcDescription(from, kv.getValue());

                    AssessmentRatingRecord record = tx.newRecord(ar);
                    record.setAssessmentDefinitionId(to.getId());
                    record.setEntityId(kv.getKey().v1);
                    record.setEntityKind(to.getEntityKind());
                    record.setRatingId(kv.getKey().v2);
                    record.setLastUpdatedAt(now);
                    record.setIsReadonly(true);
                    record.setLastUpdatedBy(userId);
                    record.setProvenance(provenance);
                    record.setDescription(desc);
                    return record;
                })
                .collect(toSet());

        Result<AssessmentRatingRecord> existing = tx
                .selectFrom(ar)
                .where(ar.ASSESSMENT_DEFINITION_ID.eq(to.getId()))
                .fetch();

        PairDiffResult<AssessmentRatingRecord, AssessmentRatingRecord> diff = mkPairDiff(
                existing,
                required,
                a -> tuple(a.getEntityId(), a.getRatingId()),
                b -> tuple(b.getEntityId(), b.getRatingId()),
                (a, b) -> safeEq(a.getDescription(), b.getDescription()));

        int insertCount = summarizeResults(tx.batchInsert(diff.otherOnly()).execute());
        int rmCount = summarizeResults(tx.batchDelete(diff.waltzOnly()).execute());
        int updCount = summarizeResults(diff.differingIntersection()
                .stream()
                .map(t -> tx.update(ar).set(ar.DESCRIPTION, t.v2.getDescription()).where(ar.ID.eq(t.v1.getId())))
                .collect(Collectors.collectingAndThen(toSet(), tx::batch))
                .execute());

        LOG.info(format(
                "Assessment Rippler: %s -> %s, created: %d ratings, removed: %d ratings, updated: %d ratings",
                from.getExternalId(),
                to.getExternalId(),
                insertCount,
                rmCount,
                updCount));
    }


    private static String calcDescription(AssessmentDefinitionRecord from,
                                          List<EntityReference> sourceRefs) {
        String fullDesc = format(
                "Rating derived from %s assessment/s on:\n\n%s",
                from.getName(),
                sourceRefs
                        .stream()
                        .map(ref -> format(
                                "- %s",
                                toLink(ref)))
                        .collect(joining("\n")));

        String cutoffText = "\n ...";
        int maxDescLength = ar.DESCRIPTION.getDataType().length() - cutoffText.length();

        if (fullDesc.length() > maxDescLength) {
            String forcedCutoff = fullDesc.substring(0, maxDescLength);
            return forcedCutoff
                    .substring(0, forcedCutoff.lastIndexOf("\n"))
                    .concat(cutoffText);
        } else {
            return fullDesc;
        }
    }


    private static String toLink(EntityReference ref) {
        String path = toPathSegment(ref.kind());
        return format(
                "[%s](%s/%d)",
                ref.name().orElse("??"),
                path,
                ref.id());
    }


    private static String toPathSegment(EntityKind kind) {
        switch (kind) {
            case ACTOR:
                return "actor";
            case APPLICATION:
                return "application";
            case CHANGE_INITIATIVE:
                return "change-initiative";
            case DATA_TYPE:
                return "data-types";
            case FLOW_CLASSIFICATION_RULE:
                return "flow-classification-rule";
            case LICENCE:
                return "licence";
            case LOGICAL_DATA_FLOW:
                return "logical-flow";
            case MEASURABLE:
                return "measurable";
            case MEASURABLE_RATING:
                return "measurable-rating";
            case PHYSICAL_FLOW:
                return "physical-flow";
            case PHYSICAL_SPECIFICATION:
                return "physical-specification";
            default:
                throw new IllegalArgumentException(format("Cannot convert kind: %s to a path segment", kind));
        }
    }

}
