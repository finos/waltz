<script>
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import RouteSelector from "./RouteSelector.svelte";
    import EntityLabel from "../../common/svelte/EntityLabel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import Check from "./Check.svelte";

    import {CORE_API} from "../../common/services/core-api-utils";

    import {logicalFlow, physicalSpecification, reset} from "./physical-flow-editor-store";

    import _ from "lodash";
    import PhysicalSpecificationSelector from "./PhysicalSpecificationSelector.svelte";

    export let primaryEntityRef = {};
    export let serviceBroker;

    function parseLogicalFlowId() {
        const idAsStr = new URLSearchParams(window.location.search).get("LOGICAL_DATA_FLOW");
        return idAsStr
            ? Number(idAsStr)
            : null;
    }

    const givenLogicalFlowId = parseLogicalFlowId();

    let logicalFlows = [];
    let specifications = [];
    let application;

    reset();

    $: {
        primaryEntityRef && serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.getById,
                [primaryEntityRef.id])
            .then(r => application = r.data);
        primaryEntityRef && serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [primaryEntityRef])
            .then(r => logicalFlows = r.data);
    }

    $: $logicalFlow = givenLogicalFlowId
        ? _.find(logicalFlows, d => d.id === givenLogicalFlowId)
        : null;

    $: $logicalFlow && serviceBroker
        .loadViewData(
            CORE_API.PhysicalSpecificationStore.findByEntityReference,
            [$logicalFlow.source])
        .then(r => specifications = r.data);

</script>


{#if application}
<PageHeader name="Register new Physical Flow"
            icon="dot-circle-o"
            small={_.get(application, ["name"], "-")}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><EntityLink ref={application}/></li>
            <li>Register Physical Flow</li>
        </ol>
    </div>

    <div slot="summary">
        <h3>
            <Check selected={$logicalFlow !== null}/>
            Route
        </h3>
        <div class="step-body">
            {#if !$logicalFlow}
                <div class="help-block">
                    Select which nodes this physical flow is between.
                    <br>
                    If the route is not listed add a new logical flow using the <em>Add new route</em> option.
                </div>
                <RouteSelector node={application}
                               flows={logicalFlows}/>
            {:else}
                <EntityLabel ref={$logicalFlow.source}/>
                <span class="flow-arrow">
                <Icon name="arrow-right"/>
            </span>
                <EntityLabel ref={$logicalFlow.target}/>

                <button class="btn btn-link"
                        on:click={() => $logicalFlow = null}>
                    <Icon name="times"/>
                    Select different route
                </button>
            {/if}
        </div>

        <h3>
            <Check selected={false}/>
            Delivery Characteristics
        </h3>

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