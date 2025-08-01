<script>
    import _ from "lodash";
    import PageHeader from "../../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import { loadSvelteEntity } from "../../../../../common/entity-utils";
    import NoData from "../../../../../common/svelte/NoData.svelte";
    import ApplicationInfoPanel from "../../../../../common/svelte/info-panels/ApplicationInfoPanel.svelte";
    import RouteSelector from "../../../../../physical-flows/svelte/RouteSelector.svelte";
    import LogicalFlowSelectionStep from "../../../../../physical-flows/svelte/LogicalFlowSelectionStep.svelte";
    import PhysicalFlowCharacteristicsStep
        from "../../../../../physical-flows/svelte/PhysicalFlowCharacteristicsStep.svelte";
    import PhysicalSpecificationStep from "../../../../../physical-flows/svelte/PhysicalSpecificationStep.svelte";
    import DataTypeSelectionStep from "../../../../../physical-flows/svelte/DataTypeSelectionStep.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    export let parentEntityRef;

    $: sourceEntityCall = loadSvelteEntity(parentEntityRef);
    $: sourceEntity = $sourceEntityCall.data ?
        $sourceEntityCall.data
        : {};

</script>


<PageHeader name="Propose Data Flow"
            icon="code-pull-request"
            small={_.get(sourceEntity, ["name"], "-")}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><EntityLink ref={sourceEntity}/></li>
            <li>Propose Data Flow</li>
        </ol>
    </div>
    <div slot="summary">
        {#if !sourceEntity.name}
            <NoData>
                No data found for {parentEntityRef.kind} {parentEntityRef.id}
            </NoData>
        {:else}
            <div class="selection-step">
                <LogicalFlowSelectionStep primaryEntityRef={parentEntityRef}/>
            </div>

            <div class="selection-step">
                <PhysicalSpecificationStep primaryEntityRef={parentEntityRef}/>
            </div>

            <div class="selection-step">
                <PhysicalFlowCharacteristicsStep primaryEntityRef={parentEntityRef}/>
            </div>

            <div class="selection-step">
                <DataTypeSelectionStep primaryEntityRef={parentEntityRef}/>
            </div>
            <br>

            <span>
                <button class="btn btn-success"
                        disabled={true}
                        on:click={() => true}>
                    Create
                </button>

                {#if true}
                    <span class="incomplete-warning">
                        <Icon name="exclamation-triangle"/>You must complete all sections
                    </span>
                {/if}
            </span>

        {/if}
    </div>
</PageHeader>

<style type="text/scss">
    @import "../../../../../../style/_variables.scss";

    .incomplete-warning {
        color: $waltz-amber;
    }

    .selection-step {
        border: #EEEEEE 1px solid;
        padding-bottom: 1em;
        padding-left: 1em;
        padding-right: 1em;
        margin-bottom: 0.25em;
    }
</style>