<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import EnumSelect from "./EnumSelect.svelte";
    import {basisOffsetDefaultOptions, determineExpandedSections, sections} from "./physical-flow-registration-utils";
    import BasisOffsetSelect from "./BasisOffsetSelect.svelte";
    import {expandedSections, physicalFlow} from "./physical-flow-editor-store";
    import Icon from "../../common/svelte/Icon.svelte";
    import {nestEnums} from "../../common/svelte/enum-utils";

    export let primaryEntityRef;

    let workingCopy = {
        basisOffset: "0",
        description: ""
    };
    let transportKinds = [];

    let enumsCall = enumValueStore.load();

    function toOptions(enumsByType, kind) {
        return _
            .chain(enumsByType)
            .get([kind], [])
            .orderBy([d => d.position, d => d.name])
            .value();
    }

    $: basisOffsetByCode = _.keyBy(basisOffsetDefaultOptions, k => k.code)
    $: enumsByType = _.groupBy($enumsCall.data, d => d.type);

    $: transportKinds = toOptions(enumsByType, "TransportKind");
    $: frequencyKinds = toOptions(enumsByType, "Frequency");
    $: physicalFlowCriticalityKinds = toOptions(enumsByType, "physicalFlowCriticality");

    $: nestedEnums = nestEnums($enumsCall.data);

    $: done = workingCopy.transport
        && workingCopy.physicalFlowCriticality
        && workingCopy.frequencyKind

    function save() {

        const basisOffset = workingCopy.basisOffset === "OTHER"
            ? Number(workingCopy.customBasisOffset)
            : Number(workingCopy.basisOffset);

        $physicalFlow = Object.assign(
            {},
            {
                transport: workingCopy.transport,
                frequency: workingCopy.frequencyKind,
                basisOffset,
                criticality: workingCopy.physicalFlowCriticality
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
                        Transport: {_.get(nestedEnums, ["TransportKind", `${$physicalFlow.transport}`, "name"], $physicalFlow.transport)}</li>
                    <li>
                        Frequency: {_.get(nestedEnums, ["Frequency", `${$physicalFlow.frequency}`, "name"], $physicalFlow.frequency)}</li>
                    <li>
                        Criticality: {_.get(nestedEnums, ["physicalFlowCriticality", `${$physicalFlow.criticality}`, "name"], $physicalFlow.criticality)}</li>
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
                            bind:value={workingCopy.frequencyKind}
                            mandatory="true"
                            name="Frequency">
                    <div slot="help">
                        Describes how often (on average) the data is transferred between the source and target.
                    </div>
                </EnumSelect>

                <EnumSelect options={physicalFlowCriticalityKinds}
                            bind:value={workingCopy.physicalFlowCriticality}
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
        padding-left: 1em;
    }
</style>