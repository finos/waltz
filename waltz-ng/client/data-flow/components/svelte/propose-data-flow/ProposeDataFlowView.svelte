<script>
    import _ from "lodash";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import { proposeDataFlowRemoteStore } from "../../../../svelte-stores/propose-data-flow-remote-store";
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
    import { settingsStore } from "../../../../svelte-stores/settings-store";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";

    export let primaryEntityRef;
    export let targetLogicalFlowId;

    const DATAFLOW_PROPOSAL_SETTING_NAME = "feature.data-flow-proposals.enabled";
    const DATAFLOW_PROPOSAL_RATINGSCHEME_SETTING_NAME = "feature.data-flow-proposals.rating-scheme";

    const PROPOSAL_OUTCOMES = {
        SUCCESS: "SUCCESS",
        FAILURE: "FAILURE"
    }

    const PROPOSAL_TYPES = {
        CREATE: "CREATE",
        EDIT: "EDIT",
        DELETE: "DELETE"
    }

    let settingsCall = settingsStore.loadAll();
    let commandLaunched = false;
    let responseMessage="";
    let existingProposedFlow="";

    $: dataFlowProposalSetting = $settingsCall.data
        .filter(t => t.name === DATAFLOW_PROPOSAL_SETTING_NAME)
        [0];
    $: dataFlowProposalsEnabled = dataFlowProposalSetting && dataFlowProposalSetting.value && dataFlowProposalSetting.value === 'true';
    $: dataFlowProposalsRatingSchemeSetting = $settingsCall.data
        .filter(t => t.name === DATAFLOW_PROPOSAL_RATINGSCHEME_SETTING_NAME)[0];
    $: dataFlowProposalsRatingSchemeExtId = dataFlowProposalsRatingSchemeSetting?.value;

    $: sourceEntityCall = loadSvelteEntity(primaryEntityRef);
    $: sourceEntity = $sourceEntityCall.data ?
        $sourceEntityCall.data
        : {};

    $: logicalFlowCall = targetLogicalFlowId ? logicalFlowStore.getById(targetLogicalFlowId) : null;
    $: $logicalFlow = logicalFlowCall ? $logicalFlowCall.data : null;

    function goToWorkflow(proposedFlowId) {
        $pageInfo = {
            state: "main.proposed-flow.view",
            params: {
                id: proposedFlowId
            }
        }
    }

    function resetStore() {
        $dataTypes = [];
        $logicalFlow = null;
        $physicalFlow = null;
        $physicalSpecification = null
        $skipDataTypes = false;
        $proposalReason = null;
    }

    function launchCommand() {
        // as soon as the user launches the command, the button gets disabled
        commandLaunched = true;

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
            reason: $proposalReason.rating[0] ? mkReason($proposalReason.rating[0]) : null,
            source: $logicalFlow.source ?? null,
            target: $logicalFlow.target ?? null,
            proposalType: PROPOSAL_TYPES.CREATE
        }

        proposeDataFlowRemoteStore.proposeDataFlow(command)
            .then(r => {
                const response = r.data;
                switch (response.outcome) {
                    case PROPOSAL_OUTCOMES.FAILURE:
                        responseMessage = response.message;
                        toasts.error("Error proposing data flow");
                        commandLaunched = false; // reset so user can re-submit
                        if (response.proposedFlowId) {
                            existingProposedFlow = "proposed-flow/" + response.proposedFlowId;
                        } else if (response.physicalFlowId) {
                            existingProposedFlow = "physical-flow/" + response.physicalFlowId;
                        }
                        break;

                    case PROPOSAL_OUTCOMES.SUCCESS:
                        if (response.proposedFlowId) {
                            toasts.success("Data Flow Proposed");
                            resetStore();
                            setTimeout(goToWorkflow, 500, response.proposedFlowId);
                        }
                        else {
                            toasts.error("Error proposing data flow");
                            commandLaunched = false;
                        }
                        break;

                    default:
                        toasts.error("Error proposing data flow");
                        commandLaunched = false;
                        break;
                }
            })
            .catch(e => {
                displayError("Error proposing data flow", e);
                commandLaunched = false;
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
                <ReasonSelectionStep ratingSchemeExtId={dataFlowProposalsRatingSchemeExtId}/>
            </div>
            <br>

            <span>
                <button class="btn btn-success"
                        disabled={incompleteRecord || commandLaunched}
                        on:click={() => launchCommand()}>
                    Propose
                </button>
                {#if responseMessage}
                <div style="margin:20px 0px">
                    <NoData type="error" >
                        {responseMessage}
                        <br>
                        <a href={existingProposedFlow} target="_blank" rel="noreferrer">Go to Flow</a>
                    </NoData>
                </div>
                {/if}
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