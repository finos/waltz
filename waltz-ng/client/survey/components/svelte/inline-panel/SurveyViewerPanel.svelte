<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import {groupedQuestions, questions, responsesByQuestionId, selectedSection} from "./survey-detail-store";
    import _ from "lodash";
    import SurveyContextPanel from "../../../../playpen/1/SurveyContextPanel.svelte";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import {actionToIcon} from "./survey-viewer-utils";
    import {surveyInstanceViewStore} from "../../../../svelte-stores/survey-instance-view-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {determineAvailableStatusActions} from "../../../survey-actions";
    import toasts from "../../../../svelte-stores/toast-store";
    import {displayError} from "../../../../common/error-utils";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let primaryEntityRef;
    let selectedTab = 'sections';

    $: findPossibleActionsCall = surveyInstanceStore.findPossibleActions(primaryEntityRef?.id);
    $: possibleActions = $findPossibleActionsCall?.data;

    $: actionList = determineAvailableStatusActions(
        _.isNull(survey?.surveyInstance?.originalInstanceId),
        possibleActions || [])

    $: surveyCall = surveyInstanceViewStore.getById(primaryEntityRef?.id);
    $: survey = $surveyCall?.data;

    $: surveyName = survey?.surveyInstance?.name || survey?.surveyRun?.name || survey?.surveyTemplateRef?.name

    function invokeAction(action, surveyInstanceId) {
        const display = action.actionDisplay
        const verb = action.verb
        const name = action.actionName

        // SHOW MESSAGE
        const msg = `Are you sure you want to ${_.toLower(display)} this survey?`;
        const reason = action.isCommentMandatory
            ? prompt(msg + " Please enter a reason below (mandatory):", verb)
            : confirm(msg);

        const updateCmd = {action: name, reason: reason};

        const prom = reason
            ? Promise.resolve(surveyInstanceStore.updateStatus(surveyInstanceId, updateCmd))
                .then(() => {
                    toasts.success("Survey response " + verb + " successfully")
                    surveyCall = surveyInstanceViewStore.getById(surveyInstanceId, true);
                    findPossibleActionsCall = surveyInstanceStore.findPossibleActions(surveyInstanceId, true);
                })
                .catch(e => displayError("Unable to update status of survey. " + e.error, e))
            : Promise.reject(display + " cancelled")
                .catch(e => toasts.info(e))
    }

    function selectSection(section) {
        if ($selectedSection === section) {
            $selectedSection = null;
        } else {
            $selectedSection = section;
        }
    }

    function getResponsesCount(section) {
        return _
            .chain(section.questions)
            .filter(q => _.includes(questionsWithResponse, q.id))
            .size();
    }

    $: questionsWithResponse = _
        .chain(_.values($responsesByQuestionId))
        .filter(d => !_.isEmpty(d.stringResponse)
            || !_.isEmpty(d.entityResponse)
            || !_.isEmpty(d.numberResponse)
            || d.booleanResponse !== "null"
            || d.dateResponse
            || !_.isEmpty(d.listResponse)
            || !_.isEmpty(d.entityListResponse))
        .map(d => Number(d.questionId))
        .value();

    $: sectionList = $groupedQuestions;

    $: hasMandatoryQuestionsWithoutResponse = _.some($questions, q => console.log({q, m:q.isMandatory, ninc: !_.includes(questionsWithResponse, q.id)}) || q.isMandatory && !_.includes(questionsWithResponse, q.id));

</script>

<div class="waltz-sub-section show-border">
    <div class="wss-name">
        Overview
    </div>
    <div class="wss-content">
        <div style="padding: 0.5em">
            <table class="table table-condensed small">
                <tbody>
                    <tr>
                        <td>Survey</td>
                        <td>
                            <EntityLink ref={Object.assign({}, survey?.surveyInstance, {name: surveyName})}/>
                        </td>
                    </tr>
                    <tr>
                        <td>Subject</td>
                        <td>
                            <EntityLink ref={survey?.surveyInstance?.surveyEntity}/>
                        </td>
                    </tr>
                </tbody>
            </table>
            {#if !_.isEmpty(actionList)}
                <ul class="list-inline">
                    {#each actionList as action}
                        <li>
                            <button class={`btn btn-xs ${actionToIcon[action?.actionName].class}`}
                                    disabled={action.actionName === 'SUBMITTING' && hasMandatoryQuestionsWithoutResponse}
                                    on:click={() => invokeAction(action, primaryEntityRef?.id)}>
                                <Icon name={actionToIcon[action?.actionName].icon}/>{action?.actionDisplay}
                            </button>
                        </li>
                    {/each}
                </ul>
                {#if hasMandatoryQuestionsWithoutResponse}
                    <div style="padding-top: 0.5em"
                         class="small">
                        <NoData type="warning">
                            <Icon name="exclamation-triangle"/>There are mandatory questions that have not been completed for this survey
                        </NoData>
                    </div>
                {/if}
            {/if}
        </div>
    </div>
</div>

<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->

    <input type="radio"
           bind:group={selectedTab}
           value="sections"
           id="sections">
    <label class="wt-label"
           for="sections">
        <span>Sections</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="detail"
           id="detail">
    <label class="wt-label"
           for="detail">
        <span>Detail</span>
    </label>

    <div class="wt-tab wt-active">
    <!-- SERVERS -->
    {#if selectedTab === 'sections'}
        <div class="help-block small">
            <Icon name="info-circle"/>Select a section below to focus on its questions
        </div>
        <ul class="section-list">
            {#each sectionList as section}
                <li class="clickable section-list-item"
                    on:mouseenter={() => section.hovering = true}
                    on:mouseleave={() => section.hovering = false}
                    class:highlighted={section.hovering}
                    class:selected={section === $selectedSection}
                    on:click={() => selectSection(section)}>
                    {section.sectionName}
                    <span title={`${getResponsesCount(section)} questions with a response out of a total ${_.size(section.questions)} questions`}
                          class="small pull-right text-muted">
                        {`(${getResponsesCount(section)} / ${_.size(section.questions)})`}
                    </span>
                </li>
            {/each}
        </ul>
    {:else if selectedTab === 'detail'}
        <SurveyContextPanel {primaryEntityRef}/>
    {/if}
    </div>
</div>


<style type="text/scss">

    @import "style/variables";

    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }

    .highlighted {
        background-color: #f3f9ff;
    }

    .section-list {

        li:not(:last-child)  {
            border-bottom: 1px solid #EEEEEE ;
        }

        li {
            padding: 0.25em;
        }

        .selected {
            background-color: $waltz-yellow-background;
        }
    }
</style>