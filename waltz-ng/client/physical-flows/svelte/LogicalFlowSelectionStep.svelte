<script>
    import {expandedSections, logicalFlow} from "../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";
    import {logicalFlowStore} from "../../svelte-stores/logical-flow-store";
    import _ from "lodash";
    import RouteSelector from "./RouteSelector.svelte";
    import LogicalFlowLabel from "./LogicalFlowLabel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import StepHeader from "./StepHeader.svelte";
    import {determineExpandedSections, Direction, sections} from "../../data-flow/components/svelte/propose-data-flow/propose-data-flow-utils";
    import {toEntityRef} from "../../common/entity-utils";
    import FlowCreator from "./FlowCreator.svelte";
    import LogicalFlowMocker from "../../data-flow/components/svelte/propose-data-flow/LogicalFlowMocker.svelte";

    const Modes = {
        CREATE: "CREATE",
        LIST: "LIST"
    }

    export let primaryEntityRef;
    export let dataFlowProposalSetting;

    let logicalFlowsCall = null;
    let editableFlowsCall = null;
    let source;
    let target;
    let direction;
    let activeMode = Modes.LIST;

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.ROUTE);
    }

    function createDownstream() {
        source = toEntityRef(primaryEntityRef);
        direction = Direction.DOWNSTREAM;
        activeMode = Modes.CREATE;
    }

    function createUpstream() {
        target = toEntityRef(primaryEntityRef);
        direction = Direction.UPSTREAM;
        activeMode = Modes.CREATE;
    }

    function cancel() {
        source = null;
        target = null;
        activeMode = Modes.LIST
    }

    function selectFlow(flow) {
        $logicalFlow = flow;
        const specSectionOpen = _.includes($expandedSections, sections.SPECIFICATION);
        if (!specSectionOpen) {
            $expandedSections = _.concat($expandedSections, sections.SPECIFICATION)
        }
    }

    $: {
        if (primaryEntityRef) {
            logicalFlowsCall = logicalFlowStore.findByEntityReference(primaryEntityRef);
            editableFlowsCall = logicalFlowStore.findEditableFlowIdsForParentReference(primaryEntityRef);
        }
    }

    $: dataFlowProposalsEnabled = dataFlowProposalSetting && dataFlowProposalSetting.value && dataFlowProposalSetting.value === "true";

    $: logicalFlows = _
        .chain($logicalFlowsCall?.data)
        // if proposals enabled show all non-read-only flows
        .filter(f => (dataFlowProposalsEnabled && !f.isReadOnly) || _.includes(editableFlows, f.id))
        .orderBy([
            d => d.source.name.toLowerCase(),
            d => d.target.name.toLowerCase()
        ])
        .value();

    $: editableFlows = $editableFlowsCall?.data;

    $: expanded = _.includes($expandedSections, sections.ROUTE);

</script>

<StepHeader label="Route"
            icon="map-o"
            checked={$logicalFlow !== null}
            {expanded}
            onToggleExpanded={toggleSection}/>

{#if expanded}
    <div class="step-body">
        {#if !$logicalFlow}
            {#if activeMode === Modes.LIST}
                <div class="help-block">
                    <Icon name="info-circle"/>
                    Select which nodes this physical flow is between.
                    <br>
                    If the route is not listed you can
                    <button class="btn btn-skinny"
                            on:click={() => createUpstream()}>
                        create a new upstream
                    </button>
                    flow or
                    <button class="btn btn-skinny"
                            on:click={() => createDownstream()}>
                        create a new downstream
                    </button>
                    flow
                </div>

                <RouteSelector node={primaryEntityRef}
                               flows={logicalFlows}
                               on:select={(evt) => selectFlow(evt.detail)}/>

            {:else if activeMode === Modes.CREATE}
                <!--setting can be undefined if the setting is not created-->
                {#if dataFlowProposalsEnabled}
                    <LogicalFlowMocker {primaryEntityRef}
                                       bind:source
                                       bind:target
                                       {direction}
                                       on:cancel={cancel}
                                       on:select={(evt) => selectFlow(evt.detail)}/>
                {:else}
                    <FlowCreator {primaryEntityRef}
                                 bind:source
                                 bind:target
                                 {direction}
                                 on:cancel={cancel}
                                 on:select={(evt) => selectFlow(evt.detail)}/>
                {/if}
            {/if}

        {:else}

            <div>
                <span style="font-weight: lighter">
                    Selected Route:</span>
                <LogicalFlowLabel logicalFlow={$logicalFlow}
                                  {primaryEntityRef}/>
            </div>

            <button class="btn btn-skinny"
                    style="padding-top: 1em"
                    on:click={() => $logicalFlow = null}>
                <Icon name="times"/>
                Pick a different route
            </button>

        {/if}
    </div>
{/if}


<style>
    .step-body {
        padding-left: 1em;
    }
</style>