<script>
    import {logicalFlow} from "./physical-flow-editor-store";
    import {logicalFlowStore} from "../../svelte-stores/logical-flow-store";
    import _ from "lodash";
    import RouteSelector from "./RouteSelector.svelte";
    import LogicalFlowLabel from "./LogicalFlowLabel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import StepHeader from "./StepHeader.svelte";

    export let primaryEntityRef;

    let logicalFlowsCall = null;

    $: {
        if (primaryEntityRef) {
            logicalFlowsCall = logicalFlowStore.findByEntityReference(primaryEntityRef);
        }
    }

    $: logicalFlows = _
        .chain($logicalFlowsCall?.data)
        .orderBy([
            d => d.source.name.toLowerCase(),
            d => d.target.name.toLowerCase()
        ])
        .value();

</script>

<StepHeader label="Route"
            icon="map-o"
            checked={$logicalFlow !== null}/>

<div class="step-body">
    {#if !$logicalFlow}

        <div class="help-block">
            Select which nodes this physical flow is between.
            <br>
            If the route is not listed add a new logical flow using the <em>Add new route</em> option.
        </div>

        <RouteSelector node={primaryEntityRef}
                       flows={logicalFlows}/>

    {:else}

        <div>
            <span style="font-weight: lighter">Selected Route:</span>
            <LogicalFlowLabel logicalFlow={$logicalFlow}
                              {primaryEntityRef}/>
        </div>

        <button class="btn btn-skinny"
                on:click={() => $logicalFlow = null}>
            <Icon name="times"/>
            Pick a different route
        </button>

    {/if}
</div>


<style>
    .step-body {
        padding-left: 1em;
    }
</style>