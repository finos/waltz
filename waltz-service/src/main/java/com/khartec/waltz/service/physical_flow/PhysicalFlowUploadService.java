package com.khartec.waltz.service.physical_flow;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static java.util.stream.Collectors.toList;


@Service
public class PhysicalFlowUploadService {

    private final ActorDao actorDao;
    private final ApplicationDao applicationDao;
    private final PhysicalFlowDao physicalFlowDao;
    private final LogicalFlowDao logicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;

    private final Pattern basisOffsetRegex = Pattern.compile("T?(?<offset>[\\+\\-]?\\d+)");


    public PhysicalFlowUploadService(ActorDao actorDao,
                                     ApplicationDao applicationDao,
                                     LogicalFlowDao logicalFlowDao,
                                     PhysicalFlowDao physicalFlowDao,
                                     PhysicalSpecificationDao physicalSpecificationDao) {
        checkNotNull(actorDao, "actorDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");

        this.actorDao = actorDao;
        this.applicationDao = applicationDao;
        this.logicalFlowDao = logicalFlowDao;
        this.physicalFlowDao = physicalFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
    }


    /**
     * Validate upload
     * resolve entity refs - source, target, owner
     * resolve enums - format, frequency, transport, criticality
     * check for nulls or duplicates in other fields
     * check for duplicates, circular refs
     * @param cmds
     * @return
     */
    public List<PhysicalFlowUploadCommandResponse> validate(List<PhysicalFlowUploadCommand> cmds) {
        checkNotNull(cmds, "cmds cannot be empty");

        // load application and actor maps
        Map<String, Application> applicationsByAssetCode = loadApplicationsByAssetCode();
        Map<String, Actor> actorsByNameMap = loadActorsByName();

        // parse flows and resolve strings into entities or enums
        List<PhysicalFlowUploadCommandResponse> parsedFlows = cmds.stream()
                .map(cmd -> validateCommand(actorsByNameMap, applicationsByAssetCode, cmd))
                .collect(toList());

        // enumerate and locate an existing physical flows that exist - iff no parse errors
        if(parsedFlows.stream().anyMatch(f -> f.outcome() == CommandOutcome.FAILURE)) {
            return parsedFlows;
        }

        // no parse errors - check for duplicates
        List<PhysicalFlowUploadCommandResponse> responses = parsedFlows.stream()
                .map(f -> Optional.ofNullable(physicalFlowDao.getByParsedFlow(f.parsedFlow()))
                    .map(m -> (PhysicalFlowUploadCommandResponse) ImmutablePhysicalFlowUploadCommandResponse
                            .copyOf(f)
                            .withEntityReference(m.entityReference()))
                    .orElse(f))
                .collect(toList());

        return responses;
    }


    public List<PhysicalFlowUploadCommandResponse> upload(String username,
                                                          List<PhysicalFlowUploadCommand> cmds) throws Exception {
        checkNotNull(cmds, "cmds cannot be empty");

        // load application and actor maps
        List<PhysicalFlowUploadCommandResponse> validated = validate(cmds);

        if(validated.stream().anyMatch(v -> v.outcome() == CommandOutcome.FAILURE)) {
            throw new Exception("Cannot upload flows which contain parse errors, please validate");
        }

        List<PhysicalFlowUploadCommandResponse> newFlowCmds = validated.stream()
                .filter(v -> v.outcome() == CommandOutcome.SUCCESS && v.entityReference() == null)
                .collect(toList());

        List<PhysicalFlowParsed> newFlows = newFlowCmds.stream()
                .map(v -> v.parsedFlow())
                .collect(toList());

        // check or create logical flow
        Map<Tuple2<EntityReference, EntityReference>, LogicalFlow> logicalFlowsBySourceAndTarget = new HashMap<>();

        // check or create physical spec
        Map<Tuple3<EntityReference, DataFormatKind, String>, PhysicalSpecification> physicalSpecMap = new HashMap<>();

        // create physical flow with ids from the above two
        List<PhysicalFlowUploadCommandResponse> created = newFlowCmds.stream()
                .map(v -> {
                    if (!(v.outcome() == CommandOutcome.SUCCESS && v.entityReference() == null)) {
                        return v;
                    }

                    PhysicalFlowParsed flow = v.parsedFlow();
                    LogicalFlow logicalFlow = getOrCreateLogicalFlow(flow.source(), flow.target(), logicalFlowsBySourceAndTarget, username);
                    PhysicalSpecification specification = getOrCreatePhysicalSpec(flow, physicalSpecMap, username);

                    PhysicalFlow newFlow = ImmutablePhysicalFlow.builder()
                            .logicalFlowId(logicalFlow.id().get())
                            .specificationId(specification.id().get())
                            .basisOffset(flow.basisOffset())
                            .frequency(flow.frequency())
                            .transport(flow.transport())
                            .criticality(flow.criticality())
                            .description(flow.description())
                            .lastUpdatedBy(username)
                            .lastUpdatedAt(nowUtc())
                            .build();

                    long id = physicalFlowDao.create(newFlow);

                    return ImmutablePhysicalFlowUploadCommandResponse.copyOf(v)
                            .withEntityReference(EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, id));
                })
                .collect(toList());

        return created;
    }


    ////////////////////// PRIVATE //////////////////////
    /////////////////////////////////////////////////////

    private PhysicalFlowUploadCommandResponse validateCommand(Map<String, Actor> actorsByName,
                                                              Map<String, Application> applicationsByAssetCode,
                                                              PhysicalFlowUploadCommand cmd) {
        checkNotNull(cmd, "cmd cannot be null");

        Map<String, String> errors = new HashMap<>();

        // resolve entity refs - source, target, owner
        EntityReference source = getEntityRefByString(actorsByName, applicationsByAssetCode, cmd.source());
        EntityReference target = getEntityRefByString(actorsByName, applicationsByAssetCode, cmd.target());
        EntityReference owner = getEntityRefByString(actorsByName, applicationsByAssetCode, cmd.owner());

        if (source == null) {
            errors.put("source", String.format("%s not found", cmd.source()));
        }

        if (target == null) {
            errors.put("target", String.format("%s not found", cmd.target()));
        }

        if (owner == null) {
            errors.put("owner", String.format("%s not found", cmd.owner()));
        }


        // resolve enums - format, frequency, transport, criticality
        DataFormatKind format = DataFormatKind.parse(cmd.format(), (s) -> null);
        if (format == null) {
            errors.put("format", String.format("%s is not a recognised value", cmd.format()));
        }

        FrequencyKind frequency = FrequencyKind.parse(cmd.frequency(), (s) -> null);
        if (frequency == null) {
            errors.put("frequency", String.format("%s is not a recognised value", cmd.frequency()));
        }

        TransportKind transport = TransportKind.parse(cmd.transport(), (s) -> null);
        if (transport == null) {
            errors.put("transport", String.format("%s is not a recognised value", cmd.transport()));
        }

        Criticality criticality = Criticality.parse(cmd.criticality(), (s) -> null);
        if (criticality == null) {
            errors.put("criticality", String.format("%s is not a recognised value", cmd.criticality()));
        }

        // check for nulls or duplicates in other fields
        if(isEmpty(cmd.name())) {
            errors.put("name", "name not provided");
        }

        Integer basisOffset = parseBasisOffset(cmd.basisOffset());
        if (basisOffset == null) {
            errors.put("basisOffset", String.format("%s is not a recognised value, expect this to be a number", cmd.basisOffset()));
        }

        ImmutablePhysicalFlowParsed parsedFlow = ImmutablePhysicalFlowParsed.builder()
                .source(source)
                .target(target)
                .owner(owner)
                .name(cmd.name())
                .format(format)
                .specDescription(cmd.specDescription())
                .specExternalId(cmd.specExternalId())
                .frequency(frequency)
                .transport(transport)
                .criticality(criticality)
                .description(cmd.description())
                .externalId(cmd.externalId())
                .basisOffset(basisOffset)
                .build();

        return ImmutablePhysicalFlowUploadCommandResponse.builder()
                .parsedFlow(parsedFlow)
                .errors(errors)
                .originalCommand(cmd)
                .outcome(errors.size() > 0 ? CommandOutcome.FAILURE : CommandOutcome.SUCCESS)
                .build();
    }


    private Integer parseBasisOffset(String basisOffset) {
        Matcher matcher = basisOffsetRegex.matcher(basisOffset);
        if(matcher.matches()) {
            String offset = matcher.group("offset");
            return Integer.valueOf(offset);
        } else {
            return null;
        }
    }


    /**
     * Retrieve Entity Reference by string input (can either be asset code if application or name of an actor)
     * @param input
     * @return
     */
    private EntityReference getEntityRefByString(Map<String, Actor> actorsByName,
                                                 Map<String, Application> applicationsByAssetCode,
                                                 String input) {
        checkNotNull(input, "input cannot be null");
        input = input.trim();

        return Optional.ofNullable(getActorRefByName(actorsByName, input))
                .orElse(getAppRefByAssetCode(applicationsByAssetCode, input));
    }


    private EntityReference getActorRefByName(Map<String, Actor> actorsByName, String name) {
        return Optional.ofNullable(actorsByName.get(name))
                .map(a -> a.entityReference())
                .orElse(null);
    }


    private EntityReference getAppRefByAssetCode(Map<String, Application> applicationsByAssetCode, String source) {
        return Optional.ofNullable(applicationsByAssetCode.get(source))
                .map(a -> a.entityReference())
                .orElse(null);
    }


    private Map<String, Application> loadApplicationsByAssetCode() {
        return MapUtilities.indexBy(
                a -> a.assetCode().get(),
                applicationDao.getAll());
    }


    private Map<String, Actor> loadActorsByName() {
        return  MapUtilities.indexBy(
                a -> a.name(),
                actorDao.findAll());
    }


    private LogicalFlow getOrCreateLogicalFlow(EntityReference source,
                                               EntityReference target,
                                               Map<Tuple2<EntityReference, EntityReference>, LogicalFlow> logicalFlowsBySourceAndTarget,
                                               String username) {

        return logicalFlowsBySourceAndTarget.computeIfAbsent(
                Tuple.tuple(source, target),
                t4 -> {
                    LogicalFlow existing = logicalFlowDao.findBySourceAndTarget(source, target);
                    if(existing != null) return existing;

                    LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                            .source(source)
                            .target(target)
                            .lastUpdatedBy(username)
                            .provenance("waltz")
                            .build();

                    LogicalFlow logicalFlow = logicalFlowDao.addFlow(flowToAdd);
                    return logicalFlow;
                }
        );
    }


    private PhysicalSpecification getOrCreatePhysicalSpec(PhysicalFlowParsed flow,
                                                          Map<Tuple3<EntityReference, DataFormatKind, String>, PhysicalSpecification> physicalSpecMap,
                                                          String username) {
        EntityReference owner = flow.owner();
        DataFormatKind format = flow.format();
        String name = flow.name();

        return physicalSpecMap.computeIfAbsent(
                Tuple.tuple(owner, format, name),
                t -> {
                    // check database
                    PhysicalSpecification existing = physicalSpecificationDao.getByParsedFlow(flow);
                    if(existing != null) return existing;

                    // create
                    PhysicalSpecification specToAdd = ImmutablePhysicalSpecification.builder()
                            .owningEntity(owner)
                            .format(format)
                            .name(name)
                            .externalId(Optional.ofNullable(flow.specExternalId()).orElse(""))
                            .description(Optional.ofNullable(flow.specDescription()).orElse(""))
                            .lastUpdatedBy(username)
                            .lastUpdatedAt(nowUtc())
                            .provenance("waltz")
                            .build();

                    Long id = physicalSpecificationDao.create(specToAdd);
                    return ImmutablePhysicalSpecification
                            .copyOf(specToAdd)
                            .withId(id);
                });
    }
}
