<script>


    import {createEventDispatcher} from "svelte";
    import {formDetails, surveyDetails} from "./survey-detail-store";
    import {surveyInstanceViewStore} from "../../../../svelte-stores/survey-instance-view-store";
    import _ from "lodash";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import {isEmpty, termSearch} from "../../../../common";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {truncate} from "../../../../common/string-utils";

    let dispatch = createEventDispatcher();

    let selectedSurveys = [];
    let selectedQuestions = [];
    let overrideExistingResponses = false;
    let surveysWithCopiedAnswers = [];
    let showRecentlyAltered = false;

    let qry = "";

    function cancel() {
        dispatch("cancel");
    }

    let userSurveysCall, questionsCall, responsesCall;

    $: {

        if ($surveyDetails) {
            userSurveysCall = surveyInstanceViewStore.findForUser(); // find for recipient or owner
            responsesCall = surveyInstanceStore.findResponses($surveyDetails?.surveyInstance?.id);
        }
    }

    $: responses = $responsesCall?.data;
    $: questionsWithResponse = _.map(responses, d => d.questionResponse.questionId)
    $: usersSurveys = $userSurveysCall?.data;

    $: templateId = $surveyDetails?.surveyTemplateRef?.id;

    $: incompleteSurveys = _
        .chain(usersSurveys)
        .flatMap(d => _.map(d.surveyInstances, r => Object.assign({}, r, { role: d.surveyInvolvementKind})))
        .filter(d => d.surveyInstance.status === 'NOT_STARTED' ||  d.surveyInstance.status === 'IN_PROGRESS')
        .filter(d => d.surveyTemplateRef?.id === templateId)
        .filter(d => d.surveyInstance?.id !== $surveyDetails?.surveyInstance?.id)
        .groupBy(d => d.surveyInstance.id)
        .map((v, k) => {
            let roles = _.map(v, d => d.role);
            return Object.assign({}, _.first(v), {roles});
        })
        .value()

    $: completedQuestions = _.filter($formDetails?.activeQuestions, q => _.includes(questionsWithResponse, q.id));

    function copyResponses() {

        const targetSurveyInstanceIds = _.map(selectedSurveys, s => s.surveyInstance.id);
        const questionIds = _.map(selectedQuestions, s => s.id);

        const copyCommand = {
            targetSurveyInstanceIds,
            overrideExistingResponses,
            questionIds
        }

        const copyPromise = surveyInstanceStore.copyResponses($surveyDetails?.surveyInstance?.id, copyCommand);

        Promise.resolve(copyPromise)
            .then(r => {
                surveysWithCopiedAnswers = selectedSurveys;
                toasts.success("Successfully copied responses");
            })
            .then(r => {
                overrideExistingResponses = false;
                selectedSurveys = [];
                selectedQuestions = [];
            })
            .catch(e => toasts.error("Failed to copy responses: "+ e.error));

    }

    function selectSurvey(survey) {
        if(_.includes(selectedSurveys, survey)){
            selectedSurveys = _.without(selectedSurveys, survey)
        } else {
            selectedSurveys = _.concat(selectedSurveys, survey);
        }
    }

    function selectQuestion(question) {
        if(_.includes(selectedQuestions, question)){
            selectedQuestions = _.without(selectedQuestions, question)
        } else {
            selectedQuestions = _.concat(selectedQuestions, question);
        }
    }

    function mkRoleTitle(survey) {
        const isRecipient = _.includes(survey?.roles, "RECIPIENT");
        const isOwner = _.includes(survey?.roles, "OWNER");

        if (isRecipient && isOwner) {
            return "You are both a recipient and owner of this survey";
        } else if (isRecipient) {
            return "You are a recipient of this survey";
        } else if (isOwner) {
            return "You are an owner of this survey";
        }
    }

    function clearSelectedSurveys() {
        selectedSurveys = [];
    }

    $: filteredSurveys = _.isEmpty(qry)
        ? incompleteSurveys
        : termSearch(
            incompleteSurveys,
            qry,
            [
                'surveyRun.name',
                'surveyInstance.surveyEntity.name',
                'surveyInstance.surveyEntityExternalId'
            ]);

</script>


{#if $formDetails && $surveyDetails}
<div>
    <h4><Icon name="copy"/> Copying survey responses</h4>

    <!--select questions-->
    <div class="small help-block" style="padding-top: 1em">
        <strong>Select the questions</strong> you would like to copy.
        To copy all of the question responses for this survey you can leave this blank.
    </div>

    {#if isEmpty(completedQuestions)}
        <NoData>There are no completed questions for you to copy from this survey</NoData>
    {:else}
        <div class:waltz-scroll-region-250={_.size(completedQuestions) > 10}>
            <table class="table table-condensed table-hover small">
                <colgroup>
                    <col width="20%">
                    <col width="60%">
                    <col width="20%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Section</th>
                        <th>Question</th>
                        <th>Label</th>
                    </tr>
                </thead>
                <tbody>
                {#each completedQuestions as question}
                    <tr class:selected={_.includes(selectedQuestions, question)}
                        class="clickable"
                        on:click={() => selectQuestion(question)}>
                        <td>{question.sectionName}</td>
                        <td>{question.questionText}</td>
                        <td>{question.label || '-'}</td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {/if}

    <!--select survey-->
    <div class="small help-block" style="padding-top: 1em">
        <strong>Select the surveys</strong> you would like to copy these responses to. You can only copy responses to a survey sharing the
        same template, and that you are a recipient of or that you own.
    </div>
    {#if isEmpty(incompleteSurveys)}
        <NoData>
            <div class="help-block small">
                There are no open surveys you can copy these questions to
            </div>
        </NoData>
    {:else}
        {#if _.size(incompleteSurveys) > 10}
            <SearchInput bind:value={qry}/>
        {/if}

        <div class:waltz-scroll-region-250={_.size(incompleteSurveys) > 10}>
            <table class="table table-condensed table-hover small">
                <colgroup>
                    <col width="40%">
                    <col width="40%">
                    <col width="20%">
                </colgroup>
                <thead>
                <tr>
                    <th>Survey Run</th>
                    <th>Entity</th>
                    <th>External Id</th>
                </tr>
                </thead>
                <tbody>
                {#each filteredSurveys as survey}
                    <tr class="clickable"
                        class:selected={_.includes(selectedSurveys, survey)}
                        on:click={() => selectSurvey(survey)}
                        title={mkRoleTitle(survey)}>
                        <td title={survey.surveyRun.name}>{truncate(survey.surveyRun.name, 30)}</td>
                        <td title={survey.surveyInstance.surveyEntity.name}>{truncate(survey.surveyInstance.surveyEntity.name || '-', 30)}</td>
                        <td>{survey.surveyInstance.surveyEntityExternalId || '-'}</td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {/if}

    {#if !_.isEmpty(selectedSurveys)}
        <div class="small help-block" style="color: red">
            You have selected {_.size(selectedSurveys)} survey/s.
            <button class="btn btn-skinny"
                    on:click={() => clearSelectedSurveys()}>
                <span class="small">Clear all <Icon name="times"/></span>
            </button>
        </div>
    {/if}

    <!--select questions-->
    <div class="small help-block" style="padding-top: 1em">
        Do you want to <strong>override any existing responses</strong> on the selected surveys?
    </div>

    <div>
        <label for="override">Override existing responses</label>
        <input type="checkbox"
               id="override"
               on:change={() => overrideExistingResponses = !overrideExistingResponses}
               checked={overrideExistingResponses}>
    </div>

    <div style="padding-top: 1em">
        <span>
            <button class="btn btn-success"
                    disabled={_.size(selectedSurveys) === 0}
                    on:click={() => copyResponses()}>
                Save
            </button>
            <button class="btn btn-skinny"
                    on:click={cancel}>
                Cancel
            </button>
        </span>
    </div>

    {#if _.size(surveysWithCopiedAnswers) > 0}
        <div style="padding-top: 1em">

            <NoData type="info">
                <div class="help-block">
                    You have copied responses to {_.size(surveysWithCopiedAnswers)} survey/s.
                    {#if !showRecentlyAltered}
                        <button class="btn btn-skinny"
                                on:click={() => showRecentlyAltered = true}>
                            Show <Icon name="caret-down"/>
                        </button>
                    {:else}
                        <button class="btn btn-skinny"
                                on:click={() => showRecentlyAltered = false}>
                            Hide <Icon name="caret-up"/>
                        </button>
                    {/if}
                    {#if showRecentlyAltered}
                        <ul>
                            {#each surveysWithCopiedAnswers as survey}
                                <li>
                                    <EntityLink ref={Object.assign({}, survey.surveyInstance, {name: survey.surveyInstance.surveyEntity.name})}/>
                                </li>
                            {/each}
                        </ul>
                    {/if}
                </div>
            </NoData>
        </div>
    {/if}

</div>
{/if}


<style>
    .selected {
        background-color: #e2ffd9;
    }

    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>