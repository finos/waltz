package org.finos.waltz.service.assessment_rating;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.common.Checks;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.settings.SettingsDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.PairDiffResult;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobStep;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentRipplerJobConfiguration;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.StringUtilities.safeEq;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.service.assessment_rating.RipplerUtils.getTargetAndRatingProvider;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.PairDiffResult.mkPairDiff;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class OneOffRippler {

    private static final Logger LOG = LoggerFactory.getLogger(OneOffRippler.class);
    private static final AssessmentDefinition ad = Tables.ASSESSMENT_DEFINITION;
    private static final AssessmentRating ar = Tables.ASSESSMENT_RATING;
    private final DSLContext dsl;
    private final SettingsDao settingsDao;

    @Autowired
    public OneOffRippler(DSLContext dsl,
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


    /**
     * Ripple all assessments configured in the settings table, for the given entity reference
     * Currently only supports assessment rippling from Physical Flows to Logical Flows
     * @return  the number of steps taken, where a step is a source assessment def and a target assessment def
     */
    public final Long rippleAssessments(org.finos.waltz.model.assessment_definition.AssessmentDefinition assessmentDefinition, EntityReference parentEntityRef) {

        // make sure that the assessment is being modified from the correct parent
        // (i.e. an application assessment is not being modified by a flow)
        checkTrue(assessmentDefinition.entityKind().equals(parentEntityRef.kind()),
                "Assessment not related to kind: %s", parentEntityRef.kind());

        // the ripplerConfig selected should only be the one where the 'from' entity is the parent
        Set<AssessmentRipplerJobStep> rippleConfig = findRippleConfig()
                .stream()
                .flatMap(config -> config.steps().stream())
                .filter(step -> safeEq(step.fromDef(), assessmentDefinition.externalId().get()))
                .collect(toSet());

        if(!rippleConfig.isEmpty()) {
            return dsl.transactionResult(ctx -> {
                DSLContext tx = ctx.dsl();
                return rippleConfig
                        .stream()
                        .map(step -> {
                            rippleAssessment(
                                    tx,
                                    "waltz",
                                    "waltz-assessment-rippler",
                                    step.fromDef(),
                                    step.toDef(),
                                    parentEntityRef);
                            return 1;
                        })
                        .count();
            });
        }
        return 0L;
    }


    public static void rippleAssessment(DSLContext tx,
                                        String userId,
                                        String provenance,
                                        String from,
                                        String to,
                                        EntityReference parentEntityRef) {

        Map<String, AssessmentDefinitionRecord> defs = tx
                .selectFrom(ad)
                .where(ad.EXTERNAL_ID.in(from, to))
                .fetchMap(r -> r.get(ad.EXTERNAL_ID));

        AssessmentDefinitionRecord fromDef = defs.get(from);
        Checks.checkNotNull(fromDef, "Cannot ripple assessment as definition: %s not found", from);
        AssessmentDefinitionRecord toDef = defs.get(to);
        Checks.checkNotNull(toDef, "Cannot ripple assessment as definition: %s not found", toDef);
        rippleAssessment(tx, userId, provenance, fromDef, toDef, parentEntityRef);
    }


    private static void rippleAssessment(DSLContext tx,
                                         String userId,
                                         String provenance,
                                         AssessmentDefinitionRecord from,
                                         AssessmentDefinitionRecord to,
                                         EntityReference parentEntityRef) {
        checkTrue(
                from.getRatingSchemeId().equals(to.getRatingSchemeId()),
                "Assessments must share a rating scheme when rippling (%s -> %s)",
                from.getName(),
                to.getName());

        Tuple2<EntityKind, EntityKind> kinds = tuple(
                EntityKind.valueOf(from.getEntityKind()),
                EntityKind.valueOf(to.getEntityKind()));

        rippleAssessments(
                tx,
                userId,
                provenance,
                from,
                to,
                getTargetAndRatingProvider(tx, kinds, from, parentEntityRef)
        );
    }


    private static void rippleAssessments(DSLContext tx,
                                          String userId,
                                          String provenance,
                                          AssessmentDefinitionRecord from,
                                          AssessmentDefinitionRecord to,
                                          Select<Record4<Long, Long, Long, String>> targetAndRatingProvider) {
        Timestamp now = nowUtcTimestamp();

        Set<Record4<Long, Long, Long, String>> requiredRecords = targetAndRatingProvider
                .fetch()
                .stream()
                .collect(toSet());

        Set<Long> targetIds = requiredRecords.stream()
                .map(t -> t.value1())
                .collect(toSet());

        Set<AssessmentRatingRecord> required = MapUtilities
                .groupAndThen(
                        requiredRecords,
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
                .filter(kv -> kv.getKey().v2 != null) // assessment rating on the source should not be null
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
                .and(ar.ENTITY_ID.in(targetIds))
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


    public Set<AssessmentRipplerJobConfiguration> findRippleConfig() {
        Map<String, String> configEntries = settingsDao
                .indexByPrefix("job.RIPPLE_ASSESSMENTS.");

        return configEntries
                .entrySet()
                .stream()
                .map(kv -> {
                    String key = kv.getKey();
                    String value = kv.getValue();
                    String rippleName = key.replaceAll("^job.RIPPLE_ASSESSMENTS.", "");
                    LOG.debug("Parsing config ripple : {} , json: {}", rippleName, value);

                    try {
                        return parseConfig(rippleName, value);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}

