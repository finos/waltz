<script>
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import _ from "lodash";
    import toasts from "../../../../svelte-stores/toast-store";
    import {displayError} from "../../../../common/error-utils";
    import {questions} from "./survey-detail-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {createEventDispatcher} from "svelte";

    export let survey;
    export let questionsWithResponse;

    const Modes = {
        LIST: "LIST",
        CONFIRMATION: "CONFIRMATION"
    };


    function mkButtonClasses(action) {
        return `btn btn-xs btn-${action.style}`;
    }


    function onCancelAction() {
        mode = Modes.LIST;
        activeAction = null;
    }


    function requiresConfirmation(action) {
        return action.confirmationRequirement === "CONFIRM_REQUIRED"
            || action.confirmationRequirement === "CONFIRM_AND_COMMENT_REQUIRED";
    }


    function initiateAction(action, instanceId) {
        if (requiresConfirmation(action)) {
            mode = Modes.CONFIRMATION;
            activeAction = action;
            reason = action.verb;
        } else {
            invokeAction(action, instanceId);
        }
    }


    function invokeAction(action, surveyInstanceId, reason) {
        const display = action.display
        const verb = action.verb
        const name = action.name

        // SHOW MESSAGE

        const updateCmd = {action: name, reason: reason};

        return Promise
                .resolve(surveyInstanceStore.updateStatus(surveyInstanceId, updateCmd))
                .then(() => {
                    toasts.success("Survey response " + verb + " successfully");
                    findPossibleActionsCall = surveyInstanceStore.findPossibleActions(surveyInstanceId, true);
                    dispatch("action", surveyInstanceId);
                    onCancelAction();
                })
                .catch(e => displayError("Unable to update status of survey. " + e.error, e));
    }


    const dispatch = createEventDispatcher();

    let mode = Modes.LIST;
    let reason = "";
    let activeAction = null;

    $: instanceId = survey?.surveyInstance?.id;
    $: findPossibleActionsCall = instanceId && surveyInstanceStore.findPossibleActions(instanceId, true);
    $: possibleActions = $findPossibleActionsCall?.data;

    $: actionList = _.isNull(survey?.surveyInstance?.originalInstanceId)
        ? _.filter(
            possibleActions,
            pa => pa.availability === "VIEW_ONLY"
                || pa.availability === "EDIT_AND_VIEW")
        : [];

    $: hasMandatoryQuestionsWithoutResponse = _.some(
        $questions,
        q => q.isMandatory && !_.includes(questionsWithResponse, q.id));

</script>


{#if mode === Modes.LIST}
    <!-- ACTION LIST -->
    {#if !_.isEmpty(actionList)}
        <ul class="list-inline">
            {#each actionList as action}
                <li>
                    <button class={mkButtonClasses(action)}
                            disabled={action.actionName === 'SUBMITTING' && hasMandatoryQuestionsWithoutResponse}
                            on:click={() => initiateAction(action, instanceId)}>
                        <Icon name={action.icon}/>
                        {action.display}
                    </button>
                </li>
            {/each}
        </ul>
        {#if hasMandatoryQuestionsWithoutResponse}
            <div style="padding-top: 0.5em"
                 class="small">
                <NoData type="warning">
                    <Icon name="exclamation-triangle"/>
                    There are mandatory questions that have not been completed for this survey
                </NoData>
            </div>
        {/if}
    {/if}
{:else if mode === Modes.CONFIRMATION }
    <!-- ACTION CONFIRMATION -->
    <h4>Are you sure you want to {_.toLower(activeAction.display)} this survey?</h4>

    <form autocomplete="off"
          on:submit|preventDefault={() => invokeAction(activeAction, instanceId)}>

        <!-- CONFIRMATION REASON ? -->
        {#if activeAction.confirmationRequirement === "CONFIRM_AND_COMMENT_REQUIRED"}
            Please enter a reason below (mandatory):
            <textarea class="form-control"
                      bind:value={reason}/>
        {/if}

        <!-- SUBMIT -->
        <button type="submit"
                class={mkButtonClasses(activeAction)}>
            <Icon name={activeAction.icon}/>
            {activeAction.display}
        </button>

        <!-- CANCEL -->
        <button class="btn btn-link"
                on:click={onCancelAction}>
            Cancel
        </button>

    </form>


{:else}
    <h4>Unknown Mode: {mode}</h4>
{/if}