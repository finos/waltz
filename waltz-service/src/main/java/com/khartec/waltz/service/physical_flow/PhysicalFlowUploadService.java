package com.khartec.waltz.service.physical_flow;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.CriticalityKindParser;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.model.physical_specification.DataFormatKindParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.common.StringUtilities.isEmpty;


@Service
public class PhysicalFlowUploadService {

    private final ActorDao actorDao;
    private final ApplicationDao applicationDao;
    private final PhysicalFlowDao physicalFlowDao;

    private Map<String, Application> applicationsByAssetCode;
    private Map<String, Actor> actorsByNameMap;

    private final TransportKindParser transportKindKindParser = new TransportKindParser();
    private final FrequencyKindParser frequencyKindParser = new FrequencyKindParser();
    private final DataFormatKindParser dataFormatKindParser = new DataFormatKindParser();
    private final CriticalityKindParser criticalityKindParser = new CriticalityKindParser();
    private final Pattern basisOffsetRegex = Pattern.compile("T?(?<offset>[\\+\\-]?\\d+)");


    public PhysicalFlowUploadService(ActorDao actorDao,
                                     ApplicationDao applicationDao,
                                     PhysicalFlowDao physicalFlowDao) {
        checkNotNull(actorDao, "actorDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");

        this.actorDao = actorDao;
        this.applicationDao = applicationDao;
        this.physicalFlowDao = physicalFlowDao;
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
    public List<PhysicalFlowValidateCommandResponse> validate(List<PhysicalFlowValidateCommand> cmds) {
        Checks.checkNotNull(cmds, "cmds cannot be empty");

        // load application and actor maps
        time("PFUS.loadMaps", () -> {
            loadMaps();
            return null;
        });

        // parse flows and resolve strings into entities or enums
        List<PhysicalFlowValidateCommandResponse> parsedFlows = cmds.stream()
                .map(cmd -> validate(cmd))
                .collect(Collectors.toList());

        // enumerate and locate an existing physical flows that exist - iff no parse errors
        if(parsedFlows.stream().anyMatch(f -> f.outcome() == CommandOutcome.FAILURE)){
            return parsedFlows;
        }

        // no parse errors - check for duplicates
        List<PhysicalFlowValidateCommandResponse> responses = parsedFlows.stream()
                .map(f -> {
                    PhysicalFlow match = physicalFlowDao.getByParsedFlow(f.parsedFlow());
                    return match != null
                            ? ImmutablePhysicalFlowValidateCommandResponse
                            .copyOf(f)
                            .withEntityReference(match.entityReference())
                            : f;
                })
                .collect(Collectors.toList());

        return responses;
    }


    private PhysicalFlowValidateCommandResponse validate(PhysicalFlowValidateCommand cmd) {
        checkNotNull(cmd, "cmd cannot be null");

        Map<String, String> errors = new HashMap<>();

        // resolve entity refs - source, target, owner
        EntityReference source = getEntityRefByString(cmd.source());
        EntityReference target = getEntityRefByString(cmd.target());
        EntityReference owner = getEntityRefByString(cmd.owner());

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
        DataFormatKind format = dataFormatKindParser.parse(cmd.format(), null);
        if (format == null) {
            errors.put("format", String.format("%s is not a recognised value", cmd.format()));
        }

        FrequencyKind frequency = frequencyKindParser.parse(cmd.frequency(), null);
        if (frequency == null) {
            errors.put("frequency", String.format("%s is not a recognised value", cmd.frequency()));
        }

        TransportKind transport = transportKindKindParser.parse(cmd.transport(), null);
        if (transport == null) {
            errors.put("transport", String.format("%s is not a recognised value", cmd.transport()));
        }

        Criticality criticality = criticalityKindParser.parse(cmd.criticality(), null);
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

        return ImmutablePhysicalFlowValidateCommandResponse.builder()
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
    private EntityReference getEntityRefByString(String input) {
        checkNotNull(input, "input cannot be null");
        input = input.trim();

        return Optional.ofNullable(getActorRefByName(input))
                .orElse(getAppRefByAssetCode(input));
    }


    private EntityReference getActorRefByName(String name) {
        return Optional.ofNullable(actorsByNameMap.get(name))
                .map(a -> a.entityReference())
                .orElse(null);
    }


    private EntityReference getAppRefByAssetCode(String source) {
        return Optional.ofNullable(applicationsByAssetCode.get(source))
                .map(a -> a.entityReference())
                .orElse(null);
    }


    private void loadMaps() {
        actorsByNameMap = MapUtilities.indexBy(
                a -> a.name(),
                actorDao.findAll());

        applicationsByAssetCode = MapUtilities.indexBy(
                a -> a.assetCode().get(),
                applicationDao.getAll());
    }

}
