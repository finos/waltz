<script>
    import _ from "lodash";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import { proposeDataFlowRemoteStore } from "../../../../svelte-stores/propose-data-flow-remote-store";
    import {
        dataTypes,
        logicalFlow,
        physicalFlow,
        physicalSpecification,
        skipDataTypes,
        proposalReason, duplicateFlowMessage, existingDuplicateFlow
    } from "./propose-data-flow-store";
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
    import {DATAFLOW_PROPOSAL_SETTING_NAME, PROPOSAL_OUTCOMES, PROPOSAL_TYPES} from "../../../../common/constants"
    import {getDataFlowProposalsRatingScheme, isDataFlowProposalsEnabled} from "../../../../common/utils/settings-util";
    import {displayError} from "../../../../common/error-utils";
    import {buildProposalFlowCommand} from "../../../../common/utils/propose-flow-command-util";
    import {handleProposalValidation} from "../../../../common/utils/proposalValidation";

    export let primaryEntityRef;
    export let targetLogicalFlowId;

    let settingsCall = settingsStore.loadAll();
    let commandLaunched = false;

    $: dataFlowProposalSetting = $settingsCall.data
        .filter(t => t.name === DATAFLOW_PROPOSAL_SETTING_NAME)
        [0];
    $: dataFlowProposalsEnabled = isDataFlowProposalsEnabled($settingsCall.data);
    $: dataFlowProposalsRatingSchemeExtId = getDataFlowProposalsRatingScheme($settingsCall.data);

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

        const command = buildProposalFlowCommand({
            physicalFlow: $physicalFlow,
            specification: $physicalSpecification,
            parentEntityRef: toEntityRef(primaryEntityRef),
            logicalFlow: $logicalFlow,
            dataType: $dataTypes,
            selectedReason: $proposalReason,
            proposalType: PROPOSAL_TYPES.CREATE
        });
        proposeDataFlowRemoteStore.proposeDataFlow(command)
            .then(r => {
                const response = r.data;
                commandLaunched=handleProposalValidation(response,false,resetStore,true,goToWorkflow,PROPOSAL_TYPES.CREATE);
            })
            .catch(e => {
                displayError("Error proposing data flow", e);
                commandLaunched = false; // reset in case of error so that user is able to re-submit
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
                {#if $duplicateFlowMessage}
                <div style="margin:20px 0px">
                    <NoData type="error" >
                        {$duplicateFlowMessage}
                        <br>
                        <a href={$existingDuplicateFlow} target="_blank" rel="noreferrer">Go to Flow</a>
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