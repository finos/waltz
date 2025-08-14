<script>
    import _ from "lodash";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import { proposeDataFlowStore } from "../../../../svelte-stores/propose-data-flow-store";
    import { dataTypes,
        logicalFlow,
        physicalFlow,
        physicalSpecification,
        skipDataTypes,
        proposalReason } from "./propose-data-flow-store";
    import { loadSvelteEntity, toEntityRef } from "../../../../common/entity-utils";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import LogicalFlowSelectionStep from "../../../../physical-flows/svelte/LogicalFlowSelectionStep.svelte";
    import PhysicalFlowCharacteristicsStep from "../../../../physical-flows/svelte/PhysicalFlowCharacteristicsStep.svelte";
    import PhysicalSpecificationStep from "../../../../physical-flows/svelte/PhysicalSpecificationStep.svelte";
    import DataTypeSelectionStep from "../../../../physical-flows/svelte/DataTypeSelectionStep.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import ReasonSelectionStep from "./ReasonSelectionStep.svelte";
    import { logicalFlowStore } from "../../../../svelte-stores/logical-flow-store";
    import {settingsStore} from "../../../../svelte-stores/settings-store";

    export let primaryEntityRef;
    export let targetLogicalFlowId;

    const DATAFLOW_PROPOSAL_SETTING_NAME = "feature.data-flow-proposals.enabled";

    const PROPOSAL_OUTCOMES = {
        SUCCESS: "SUCCESS",
        FAILURE: "FAILURE"
    }

    const PROPOSAL_OUTCOME_MESSAGES = {
        PROPOSED_FLOW_CREATED_WITH_SUCCESS: "PROPOSED_FLOW_CREATED_WITH_SUCCESS",
        PROPOSED_FLOW_CREATED_WITH_FAILURE: "PROPOSED_FLOW_CREATED_WITH_FAILURE"
    };

    let settingsCall = settingsStore.loadAll();

    $: dataFlowProposalSetting = $settingsCall.data
        .filter(t => t.name === DATAFLOW_PROPOSAL_SETTING_NAME)
        [0];
    $: dataFlowProposalsEnabled = dataFlowProposalSetting && dataFlowProposalSetting.value && dataFlowProposalSetting.value === 'true';

    $: sourceEntityCall = loadSvelteEntity(primaryEntityRef);
    $: sourceEntity = $sourceEntityCall.data ?
        $sourceEntityCall.data
        : {};

    $: logicalFlowCall = targetLogicalFlowId ? logicalFlowStore.getById(targetLogicalFlowId) : null;
    $: $logicalFlow = logicalFlowCall ? $logicalFlowCall.data : null;

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

        const mkReason = (rating) => ({
            ratingId: rating.id,
            description: rating.description
        });


        const command = {
            specification,
            flowAttributes,
            logicalFlowId: $logicalFlow.id ?? null,
            physicalFlowId: null,
            dataTypeIds: $dataTypes,
            reason: $proposalReason.rating[0] ? mkReason($proposalReason.rating[0]) : null
        }

        _.set(command, 'source', $logicalFlow.source ?? null);
        _.set(command, 'target', $logicalFlow.target ?? null);

        proposeDataFlowStore.proposeDataFlow(command)
            .then(r => {
                const response = r.data;
                if(response.outcome === PROPOSAL_OUTCOMES.SUCCESS) {
                    toasts.success("Data Flow Proposed");
                    //TODO: route to the created workflow on success
                } else {
                    toasts.error("Error proposing data flow");
                }
            })
            .catch(e => {
                displayError("Error proposing data flow", e);
            });
    }

    $: incompleteRecord = !($logicalFlow && $physicalFlow && $physicalSpecification && $proposalReason && (!_.isEmpty($dataTypes) || $skipDataTypes));
</script>

{#if dataFlowProposalsEnabled && primaryEntityRef}
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
                <LogicalFlowSelectionStep primaryEntityRef={sourceEntity} {dataFlowProposalSetting}/>
            </div>

            <div class="selection-step">
                <PhysicalSpecificationStep primaryEntityRef={sourceEntity}/>
            </div>

            <div class="selection-step">
                <PhysicalFlowCharacteristicsStep primaryEntityRef={sourceEntity}/>
            </div>

            <div class="selection-step">
                <DataTypeSelectionStep primaryEntityRef={sourceEntity}/>
            </div>

            <div class="selection-step">
                <ReasonSelectionStep/>
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
{/if}

<style type="text/scss">
    @import "../../../../../style/variables";

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