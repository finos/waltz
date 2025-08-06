<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import EnumSelect from "./EnumSelect.svelte";
    import {
        basisOffsetDefaultOptions,
        determineExpandedSections,
        sections, toCriticalityName, toFrequencyKindName,
        toOptions, toTransportKindName
    } from "./propose-data-flow-utils";
    import BasisOffsetSelect from "./BasisOffsetSelect.svelte";
    import {expandedSections, nestedEnums, physicalFlow} from "./propose-data-flow-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";

    let workingCopy = {};
    let transportKinds = [];
    let frequencyKinds = [];
    let physicalFlowCriticalityKinds = [];
    let enumsCall = enumValueStore.load();


    onMount(() => {
        if ($physicalFlow) {
            const offsetString = $physicalFlow.basisOffset.toString();
            const offset = _.find(basisOffsetDefaultOptions, d => d.code === offsetString);

            const offsetInfo = offset
                ? {basisOffset: offset.code}
                : {basisOffset: "OTHER", customBasisOffset: offsetString}

            workingCopy = Object.assign({}, $physicalFlow, offsetInfo);

        } else {
            workingCopy = {
                name: null,
                transport: null,
                frequency: null,
                criticality: null,
                basisOffset: "0",
                description: "",
                externalId: null
            };
        }
    })

    function save() {

        const basisOffset = workingCopy.basisOffset === "OTHER"
            ? Number(workingCopy.customBasisOffset)
            : Number(workingCopy.basisOffset);

        $physicalFlow = Object.assign(
            {},
            {
                name: workingCopy.name,
                transport: workingCopy.transport,
                frequency: workingCopy.frequency,
                basisOffset,
                criticality: workingCopy.criticality,
                description: workingCopy.description,
                externalId: workingCopy.externalId
            });

        openNextSection()
    }

    function openNextSection() {
        const flowSectionOpen = _.includes($expandedSections, sections.DATA_TYPE);
        if (!flowSectionOpen) {
            $expandedSections = _.concat($expandedSections, sections.DATA_TYPE)
        }
    }

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.FLOW);
    }

    $: basisOffsetByCode = _.keyBy(basisOffsetDefaultOptions, k => k.code);
    $: enumsByType = _.groupBy($enumsCall.data, d => d.type);

    $: transportKinds = toOptions(enumsByType, "TransportKind");
    $: frequencyKinds = toOptions(enumsByType, "Frequency");
    $: physicalFlowCriticalityKinds = toOptions(enumsByType, "physicalFlowCriticality");

    $: done = workingCopy.transport
        && workingCopy.criticality
        && workingCopy.frequency;

    $: expanded = _.includes($expandedSections, sections.FLOW);

</script>

<StepHeader label="Delivery Characteristics"
            icon="envelope-o"
            checked={$physicalFlow}
            {expanded}
            onToggleExpanded={toggleSection}/>

{#if expanded}
    <div class="step-body">

        {#if $physicalFlow}

            <div style="font-weight: lighter">
                <div>Selected Characteristics:</div>
                <ul>
                    <li>
                        Name: <span class:text-muted={!$physicalFlow.name}>{$physicalFlow.name || "Not provided"}</span></li>
                    <li>
                        Transport: {toTransportKindName($nestedEnums, $physicalFlow.transport)}</li>
                    <li>
                        Frequency: {toFrequencyKindName($nestedEnums, $physicalFlow.frequency)}</li>
                    <li>
                        Criticality: {toCriticalityName($nestedEnums, $physicalFlow.criticality)}</li>
                    <li>
                        Basis
                        Offset: {_.get(basisOffsetByCode, [$physicalFlow.basisOffset, "name"], $physicalFlow.basisOffset)}
                    </li>
                    <li>
                        External ID: <span
                        class:text-muted={!$physicalFlow.externalId}>{$physicalFlow.externalId || "Not provided"}</span>
                    </li>
                    <li>
                        Description:
                        <Markdown text={$physicalFlow.description}/>
                    </li>
                </ul>
            </div>

            <button class="btn btn-skinny"
                    style="padding-top: 1em"
                    on:click={() => $physicalFlow = null}>
                <Icon name="times"/>
                Pick different characteristics
            </button>

        {:else}
            <form autocomplete="off"
                  on:submit|preventDefault={save}>

                <div class="form-group">
                    <label for="name">
                        Name
                    </label>
                    <input class="form-control"
                           id="name"
                           bind:value={workingCopy.name}/>
                </div>
                <div class="help-block">
                    Name to describe this physical flow.
                </div>

                <EnumSelect options={transportKinds}
                            bind:value={workingCopy.transport}
                            mandatory="true"
                            name="Transport">
                    <div slot="help">
                        Describes how that data is transferred between the source and target.
                    </div>
                </EnumSelect>

                <EnumSelect options={frequencyKinds}
                            bind:value={workingCopy.frequency}
                            mandatory="true"
                            name="Frequency">
                    <div slot="help">
                        Describes how often (on average) the data is transferred between the source and target.
                    </div>
                </EnumSelect>

                <EnumSelect options={physicalFlowCriticalityKinds}
                            bind:value={workingCopy.criticality}
                            mandatory="true"
                            name="Criticality">
                    <div slot="help">
                        An indicator of the importance of this flow
                    </div>
                </EnumSelect>


                <!--Basis offset select-->
                <BasisOffsetSelect bind:value={workingCopy.basisOffset}
                                   bind:customValue={workingCopy.customBasisOffset}>
                    <div slot="help">
                        Describes the time from a trigger event for the data transfer to take place. Defaults to T.
                    </div>
                </BasisOffsetSelect>

                <div class="form-group">
                    <label for="description">
                        Description
                    </label>
                    <textarea class="form-control"
                              id="description"
                              rows="2"
                              bind:value={workingCopy.description}/>
                </div>
                <div class="help-block">Description of the flow</div>

                <div class="form-group">
                    <label for="external-id">
                        External Id
                    </label>
                    <input class="form-control"
                           id="external-id"
                           bind:value={workingCopy.externalId}/>
                </div>
                <div class="help-block">
                    External identifier for this flow
                </div>

                <button class="btn btn-skinny"
                        disabled={!done}
                        on:click={() => save()}>
                    Done
                </button>
            </form>
        {/if}
    </div>
{/if}


<style>
    .step-body {
        padding-left: 1em;
    }
</style>