<script>
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import RouteSelector from "./RouteSelector.svelte";
    import EntityLabel from "../../common/svelte/EntityLabel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import Check from "./Check.svelte";

    import {logicalFlow, physicalSpecification, physicalFlow, reset} from "./physical-flow-editor-store";

    import _ from "lodash";
    import PhysicalSpecificationSelector from "./PhysicalSpecificationSelector.svelte";
    import {applicationStore} from "../../svelte-stores/application-store";
    import {logicalFlowStore} from "../../svelte-stores/logical-flow-store";
    import {physicalSpecStore} from "../../svelte-stores/physical-spec-store";
    import {onMount} from "svelte";
    import LogicalFlowLabel from "./LogicalFlowLabel.svelte";
    import LogicalFlowSelectionStep from "./LogicalFlowSelectionStep.svelte";
    import PhysicalFlowCharacteristicsStep from "./PhysicalFlowCharacteristicsStep.svelte";

    export let primaryEntityRef = {};

    $: application = primaryEntityRef;
    let logicalFlows = [];
    let specifications = [];


    $: console.log({application, logicalFlows, specifications})


</script>


{#if primaryEntityRef}
<PageHeader name="Register new Physical Flow"
            icon="dot-circle-o"
            small={_.get(primaryEntityRef, ["name"], "-")}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><EntityLink ref={primaryEntityRef}/></li>
            <li>Register Physical Flow</li>
        </ol>
    </div>

    <div slot="summary">
        <LogicalFlowSelectionStep {primaryEntityRef}/>

        <PhysicalFlowCharacteristicsStep {primaryEntityRef}/>

        <!-- SPEC -->
        <h3>
            <Check selected={$physicalSpecification}/>
            Specification (Payload)
        </h3>

        <div class="step-body">
            {#if $physicalSpecification}
                {$physicalSpecification.name}

                <button class="btn btn-link"
                        on:click={() => $physicalSpecification = null}>
                    <Icon name="times"/>
                    Select different specification
                </button>
            {:else if !$logicalFlow}
                Select a route first
            {:else}
                <div class="help-block">
                    Either reuse an existing specification or create a new one using the <em>Add new specification</em> option.
                </div>
                <PhysicalSpecificationSelector {specifications}/>
            {/if}
        </div>

        <hr>

        <EntityLink ref={application}>
            Cancel
        </EntityLink>
    </div>
</PageHeader>

{/if}


<style>
    .flow-arrow {
        padding-left: 1em;
        padding-right: 1em;
    }

    .step-body {
        margin-left: 1em;
    }
</style>