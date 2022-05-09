<script>
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import _ from "lodash";
    import toasts from "../../../../svelte-stores/toast-store";
    import {displayError} from "../../../../common/error-utils";
    import {missingMandatoryQuestionIds, questions} from "./survey-detail-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {createEventDispatcher} from "svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import StaticPanels from "../../../../common/svelte/StaticPanels.svelte";

    export let survey;
    export let additionalLinkActions = [];

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

        const updateCmd = {action: name, reason};

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

    function mkConfirmationKey(templateExtId, surveyAction) {
        return `CONFIRMATION.SURVEY_ACTION.${templateExtId}.${surveyAction}`;
    }

    function hasPermission(permissions, action) {
        const requiredPermission = action.requiredPermission;

        return _.isEmpty(requiredPermission) ||
            _.get(permissions, [action.requiredPermission], false);
    }


    const dispatch = createEventDispatcher();

    let mode = Modes.LIST;
    let reason = "";
    let activeAction = null;

    let findPossibleActionsCall, permissionsCall;

    $: {
        if (instanceId) {
            findPossibleActionsCall = surveyInstanceStore.findPossibleActions(instanceId, true);
            permissionsCall = surveyInstanceStore.getPermissions(instanceId);
        }
    }

    $: instanceId = survey?.surveyInstance?.id;
    $: possibleActions = $findPossibleActionsCall?.data;
    $: permissions = $permissionsCall?.data;

    $: actionList = _.isNull(survey?.surveyInstance?.originalInstanceId)
        ? _.filter(
            possibleActions,
            pa => pa.availability === "VIEW_ONLY"
                || pa.availability === "EDIT_AND_VIEW")
        : [];

    $: hasMandatoryQuestionsWithoutResponse = ! _.isEmpty($missingMandatoryQuestionIds);

    $: confirmationGroupKey = mkConfirmationKey(survey?.surveyTemplateRef?.externalId, activeAction?.name);


</script>


{#if mode === Modes.LIST}
    <!-- ACTION LIST -->
    {#if !_.isEmpty(actionList)}
        <h5>
            <Icon name="cogs"/>
            Actions
        </h5>
        <div class="actions">
            <ul class="list-inline">
                {#each additionalLinkActions as action}
                    {#if hasPermission(permissions, action)}
                        <li>
                            <ViewLink state={action.state}
                                      ctx={{id: instanceId}}>
                                <button class="btn btn-xs btn-primary"
                                        title="Edit survey responses">
                                    <Icon name={action.icon}/>
                                    {action.name}
                                </button>
                            </ViewLink>
                        </li>
                    {/if}
                {/each}
                {#each actionList as action}
                    <li>
                        <button class={mkButtonClasses(action)}
                                title={action.description}
                                disabled={action.completionRequirement === "REQUIRE_FULL_COMPLETION" && hasMandatoryQuestionsWithoutResponse}
                                on:click={() => initiateAction(action, instanceId)}>
                            <Icon name={action.icon}/>
                            {action.display}
                        </button>
                    </li>
                {/each}
            </ul>
        </div>
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
    <StaticPanels key={confirmationGroupKey}
                  showTitle={false}/>

    <div>
        Are you sure you want to {_.toLower(activeAction.display)} this survey?
    </div>

    <form autocomplete="off"
          on:submit|preventDefault={() => invokeAction(activeAction, instanceId, reason)}>

        <!-- CONFIRMATION REASON ? -->
        {#if activeAction.confirmationRequirement === "CONFIRM_AND_COMMENT_REQUIRED"}
            <span class="small">Please enter a reason below (mandatory):</span>
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