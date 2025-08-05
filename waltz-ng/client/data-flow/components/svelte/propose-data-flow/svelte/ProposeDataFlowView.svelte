<script>
    import _ from "lodash";
    import PageHeader from "../../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import {dataTypes,
        expandedSections,
        logicalFlow,
        nestedEnums,
        physicalFlow,
        physicalSpecification,
        skipDataTypes,
        viewMode,
        ViewMode,
        proposalReason} from "./propose-data-flow-store";
    import {loadSvelteEntity, toEntityRef} from "../../../../../common/entity-utils";
    import NoData from "../../../../../common/svelte/NoData.svelte";
    import LogicalFlowSelectionStep from "./LogicalFlowSelectionStep.svelte";
    import PhysicalFlowCharacteristicsStep from "./PhysicalFlowCharacteristicsStep.svelte";
    import PhysicalSpecificationStep from "./PhysicalSpecificationStep.svelte";
    import DataTypeSelectionStep from "./DataTypeSelectionStep.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import ReasonSelectionStep from "./ReasonSelectionStep.svelte";
    import {logicalFlowStore} from "../../../../../svelte-stores/logical-flow-store";

    export let primaryEntityRef;
    export let targetLogicalFlowId;

    $: sourceEntityCall = loadSvelteEntity(primaryEntityRef);
    $: sourceEntity = $sourceEntityCall.data ?
        $sourceEntityCall.data
        : {};

    $: logicalFlowCall = targetLogicalFlowId ? logicalFlowStore.getById(targetLogicalFlowId) : null;
    $: $logicalFlow = logicalFlowCall ? $logicalFlowCall.data : null;
    $: console.log($logicalFlow);

    function launchCommand() {
        const specification = {
            owningEntity: toEntityRef(primaryEntityRef),
            name: $physicalSpecification.name,
            description: $physicalSpecification.description,
            format: $physicalSpecification.format,
            lastUpdatedBy: "waltz",
            externalId: !_.isEmpty($physicalSpecification.externalId) ? $physicalSpecification.externalId : null,
            id: $physicalSpecification.id ? $physicalSpecification.id : null
        }

        const flowAttributes = {
            name: $physicalFlow.name,
            transport: $physicalFlow.transport,
            frequency: $physicalFlow.frequency,
            basisOffset: $physicalFlow.basisOffset,
            criticality: $physicalFlow.criticality,
            description: $physicalFlow.description,
            externalId: !_.isEmpty($physicalFlow.externalId) ? $physicalFlow.externalId : null
        }

        const command = {
            specification,
            flowAttributes,
            logicalFlowId: $logicalFlow.id ?? null,
            dataTypeIds: $dataTypes,
            reasonCode: $proposalReason.rating[0] ? $proposalReason.rating[0].id : null
        }

        if(!$logicalFlow.id) {
            _.set(command, 'source', $logicalFlow.source ?? null);
            _.set(command, 'target', $logicalFlow.target ?? null);
        }

        console.log(command);
    }

    $: incompleteRecord = !($logicalFlow && $physicalFlow && $physicalSpecification && $proposalReason && (!_.isEmpty($dataTypes) || $skipDataTypes));
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
                No data found for {primaryEntityRef.kind} {primaryEntityRef.id}
            </NoData>
        {:else}
            <div class="selection-step">
                <LogicalFlowSelectionStep primaryEntityRef={sourceEntity}/>
            </div>

            <div class="selection-step">
                <PhysicalSpecificationStep primaryEntityRef={primaryEntityRef}/>
            </div>

            <div class="selection-step">
                <PhysicalFlowCharacteristicsStep primaryEntityRef={primaryEntityRef}/>
            </div>

            <div class="selection-step">
                <DataTypeSelectionStep primaryEntityRef={primaryEntityRef}/>
            </div>

            <div class="selection-step">
                <ReasonSelectionStep primaryEntityRef={primaryEntityRef}/>
            </div>
            <br>

            <span>
                <button class="btn btn-success"
                        disabled={incompleteRecord}
                        on:click={() => launchCommand()}>
                    Create
                </button>

                {#if incompleteRecord}
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