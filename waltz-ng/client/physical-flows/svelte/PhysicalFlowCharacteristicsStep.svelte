<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import EnumSelect from "./EnumSelect.svelte";
    import {
        basisOffsetDefaultOptions,
        determineExpandedSections,
        sections, toCriticalityName, toFrequencyKindName,
        toOptions, toTransportKindName
    } from "./physical-flow-registration-utils";
    import BasisOffsetSelect from "./BasisOffsetSelect.svelte";
    import {expandedSections, nestedEnums, physicalFlow} from "./physical-flow-editor-store";
    import Icon from "../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";

    let workingCopy = {
        basisOffset: "0",
        description: ""
    };

    let transportKinds = [];
    let frequencyKinds = [];
    let physicalFlowCriticalityKinds = [];

    let enumsCall = enumValueStore.load();

    $: basisOffsetByCode = _.keyBy(basisOffsetDefaultOptions, k => k.code)
    $: enumsByType = _.groupBy($enumsCall.data, d => d.type);

    $: transportKinds = toOptions(enumsByType, "TransportKind");
    $: frequencyKinds = toOptions(enumsByType, "Frequency");
    $: physicalFlowCriticalityKinds = toOptions(enumsByType, "physicalFlowCriticality");


    onMount(() => {
        if ($physicalFlow) {
            const offsetString = $physicalFlow.basisOffset.toString();
            const offset = _.find(basisOffsetDefaultOptions, d => d.code === offsetString);

            const offsetInfo = offset
                ? {basisOffset: offset.code}
                : {basisOffset: "OTHER", customBasisOffset: offsetString}

            workingCopy = Object.assign({}, $physicalFlow, offsetInfo)
        }
    })

    $: done = workingCopy.transport
        && workingCopy.criticality
        && workingCopy.frequency

    function save() {

        const basisOffset = workingCopy.basisOffset === "OTHER"
            ? Number(workingCopy.customBasisOffset)
            : Number(workingCopy.basisOffset);

        $physicalFlow = Object.assign(
            {},
            {
                transport: workingCopy.transport,
                frequency: workingCopy.frequency,
                basisOffset,
                criticality: workingCopy.criticality
            });
    }

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.FLOW);
    }

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
                        Transport: {toTransportKindName($nestedEnums, $physicalFlow.transport)}</li>
                    <li>
                        Frequency: {toFrequencyKindName($nestedEnums, $physicalFlow.frequency)}</li>
                    <li>
                        Criticality: {toCriticalityName($nestedEnums, $physicalFlow.criticality)}</li>
                    <li>Basis
                        Offset: {_.get(basisOffsetByCode, [$physicalFlow.basisOffset, "name"], $physicalFlow.basisOffset)}</li>
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
                        How important ?!
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

                <button class="btn btn-success"
                        disabled={!done}
                        on:click={() => save()}>
                    Save
                </button>
            </form>
        {/if}
    </div>
{/if}


<style>
    .step-body {
        padding-left: 2em;
    }
</style>