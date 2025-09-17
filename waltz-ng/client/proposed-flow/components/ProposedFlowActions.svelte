<script>
    import _ from "lodash";
    import toasts from "../../svelte-stores/toast-store";
    import { displayError } from "../../common/error-utils";
    import Icon from "../../common/svelte/Icon.svelte";
    import {proposedFlowStore} from "../services/svelte-stores/proposed-flow-store";
    import NoData from "../../common/svelte/NoData.svelte";
    import { defaultPermissions, STATES } from "../utils";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    
    export let refreshState;
    const Modes = {
        LIST: "LIST",
        CONFIRMATION: "CONFIRMATION",
        LOADING: "LOADING",
    };

    let mode = Modes.LIST;
    let reason = "";
    let activeAction = null;
    let validationMessage = "";
    let notification = "You do not have permission to approve or reject this proposed flow.";
    export let proposedFlow = {};

    $: currentState = proposedFlow?.workflowState?.state || {};

    $: canApproveOrReject = function() {
        if(currentState === STATES.SOURCE_REJECTED || currentState === STATES.TARGET_REJECTED) {
            // No further actions possible
            notification = "This proposed flow has been rejected and can no longer be acted upon.";
            return false;
        }

        if (currentState === STATES.PENDING_APPROVALS) {
            // Either source or target approver can act
            return (permissions.sourceApprover && permissions.sourceApprover.length > 0) ||
                (permissions.targetApprover && permissions.targetApprover.length > 0);
        }
        if (currentState === STATES.SOURCE_APPROVED || currentState === STATES.SOURCE_REJECTED) { 
            // Only target approver can act
            return permissions.targetApprover && permissions.targetApprover.length > 0;
        }
        if (currentState === STATES.TARGET_APPROVED || currentState === STATES.TARGET_REJECTED) {
            // Only source approver can act
            return permissions.sourceApprover && permissions.sourceApprover.length > 0;
        }
        
        return false;
    }


    function mkButtonClasses(action) {
        return `btn btn-xs btn-${action.style}`;
    }

    function onCancelAction() {
        mode = Modes.LIST;
        activeAction = null;
        validationMessage = "";
    }

    function requiresConfirmation(action) {
        return (
            action.confirmationRequirement === "CONFIRM_REQUIRED" ||
            action.confirmationRequirement === "CONFIRM_AND_COMMENT_REQUIRED"
        );
    }

    function initiateAction(action) {
        if (requiresConfirmation(action)) {
            mode = Modes.CONFIRMATION;
            activeAction = action;
            reason = action.verb;
        } else {
            invokeAction(action, reason);
        }
    }

    function invokeAction(action, reason) {
        const verb = action.verb
        const name = action.name;

        if(!reason) {
            validationMessage = "Reason is required when rejecting a proposed flow";
            return;
        }
        
        // SHOW MESSAGE
        const updateCmd = { action: name, payload: { comment: reason } };
        
        mode = Modes.LOADING;
        return Promise
            .resolve(proposedFlowStore.transitionProposedFlow(proposedFlow.id, updateCmd))
            .then(() => {
                mode = Modes.LIST;
                toasts.success("Proposed flow " + verb + " successfully");
                if(refreshState) {
                    refreshState();
                }
                onCancelAction();
            })
            .catch(e => {
                mode = Modes.LIST;
                displayError("Failed to " + name + " proposed flow.")
            });
    }

    $: isFullyApproved = currentState === STATES.FULLY_APPROVED;
    $: canAct = proposedFlow && canApproveOrReject();
    
    $: actionList = [
        {
            display: "Approve",
            verb: "approved",
            icon: "thumbs-up",
            style: "success",
            description: "Approve and provision the survey",
            confirmationRequirement: "CONFIRM_REQUIRED",
            name: "approve",
            disabled: !canAct
        },
        {
            display: "Reject",
            verb: "",
            icon: "thumbs-down",
            style: "danger",
            description: "Reject the survey",
            confirmationRequirement: "CONFIRM_AND_COMMENT_REQUIRED",
            name: "reject",
            disabled: !canAct
        },
    ];

    let permissionsCall;

    $: {
        if (proposedFlow) {
            permissionsCall = proposedFlowStore.findFlowPermissions(proposedFlow.id, true);
        }
    }

    $: permissions = $permissionsCall?.data || defaultPermissions;
</script>

{#if mode === Modes.LIST}
    <!-- ACTION LIST -->
    {#if !_.isEmpty(actionList)}
        <h5>
            <Icon name="cogs" />
            Actions
        </h5>
        <div class="actions">
            <ul class="list-inline">
                {#each actionList as action}
                    <li>
                        <button
                            class={mkButtonClasses(action)}
                            title={action.description}
                            disabled={action.disabled}
                            on:click={() => initiateAction(action)}
                        >
                            <Icon name={action.icon} />
                            {action.display}
                        </button>
                    </li>
                {/each}
            </ul>
        </div>
        {#if !canAct && !isFullyApproved}
            <div style="padding-top: 0.5em" class="small">
                <NoData type="warning">
                    <Icon name="exclamation-triangle" />
                    {notification}
                </NoData>
            </div>
        {/if}
        {#if isFullyApproved}
            <div style="padding-top: 0.5em" class="small">
                <EntityLink 
                    ref={{
                    kind: 'LOGICAL_DATA_FLOW', 
                    id: proposedFlow?.flowDef?.logicalFlowId, 
                    name: 'Go to logical flow'}} />
            </div>
        {/if}
    {/if}
{:else if mode === Modes.CONFIRMATION}
    <div>
        Are you sure you want to {_.toLower(activeAction.display)} this proposed
        flow?
    </div>

    <form
        autocomplete="off"
        on:submit|preventDefault={() => invokeAction(activeAction, reason)}
    >
        <!-- CONFIRMATION REASON ? -->
        {#if activeAction.confirmationRequirement === "CONFIRM_AND_COMMENT_REQUIRED"}
            <span class="small">Please enter a reason below (mandatory):</span>
            <textarea class="form-control" bind:value={reason} />
        {/if}

        <!-- SUBMIT -->
        <button type="submit" class={mkButtonClasses(activeAction)}>
            <Icon name={activeAction.icon} />
            {activeAction.display}
        </button>

        <!-- CANCEL -->
        <button class="btn btn-link" on:click={onCancelAction}> Cancel </button>
    </form>
    {#if validationMessage}
        <div style="padding-top: 0.5em" class="small">
            <NoData type="warning">
                <Icon name="exclamation-triangle" />
                {validationMessage}
            </NoData>
        </div>
    {/if}
{:else if mode === Modes.LOADING}
    <div class="actions text-muted">
        <Icon spin="true" name="spinner"/>
        <span>Processing...</span>
    </div>
{:else}
    <h4>Unknown Mode: {mode}</h4>
{/if}

<style>
    .actions {
        padding: 0.4em;
        border-color: #ddd;
        border-width: 1px;
        border-style: solid;
        background: #fafafa;
    }

    .actions ul {
        margin-bottom: 0;
    }
</style>
