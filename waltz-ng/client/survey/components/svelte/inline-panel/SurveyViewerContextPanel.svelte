<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import {groupedQuestions, responsesByQuestionId, selectedSection} from "./survey-detail-store";
    import _ from "lodash";
    import {surveyInstanceViewStore} from "../../../../svelte-stores/survey-instance-view-store";
    import SurveyInstanceInfoPanel from "../../../../common/svelte/info-panels/SurveyInstanceInfoPanel.svelte";
    import SurveyPeople from "./SurveyPeople.svelte";
    import SurveyActions from "./SurveyActions.svelte";
    import SurveyInstanceVersionPicker from "./SurveyInstanceVersionPicker.svelte";

    export let instanceId;

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

    function onAction(evt) {
        surveyCall = surveyInstanceViewStore.getById(instanceId, true);
    }

    $: surveyCall = instanceId && surveyInstanceViewStore.getById(instanceId);
    $: survey = $surveyCall?.data;

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

</script>


<!-- SURVEY INSTANCE DETAILS -->
{#if survey}
<SurveyInstanceInfoPanel {instanceId}>
    <div slot="post-title">
        <SurveyActions on:action={onAction}
                       {survey}
                       {questionsWithResponse}/>

        <SurveyInstanceVersionPicker on:select
                                     instance={survey?.surveyInstance}/>

        <br>

        <h5>
            <Icon name="columns"/>
            Sections
        </h5>
        <div class="help-block small">
            <Icon name="info-circle"/>Select a section below to focus on its questions
        </div>
        <ul class="section-list small">
            {#each sectionList as section}
                <li class="clickable section-list-item"
                    on:mouseenter={() => section.hovering = true}
                    on:mouseleave={() => section.hovering = false}
                    class:highlighted={section.hovering}
                    class:selected={section?.sectionName === $selectedSection?.sectionName}
                    on:click={() => selectSection(section)}>
                    {section.sectionName}
                    <span title={`${getResponsesCount(section)} questions with a response out of a total ${_.size(section.questions)} questions`}
                          class="small pull-right text-muted">
                {`(${getResponsesCount(section)} / ${_.size(section.questions)})`}
            </span>
                </li>
            {/each}
        </ul>

        <br>

        <h5>
            <Icon name="table"/>
            Detail
        </h5>
    </div>
    <div slot="post-header">
        <h5>
            <Icon name="users"/>
            People
        </h5>
        <SurveyPeople id={instanceId}
                      groupApprovers={survey.surveyInstance?.owningRole}/>
    </div>
</SurveyInstanceInfoPanel>
{/if}

<style type="text/scss">

    @import "style/variables";


    li {
        padding-top: 0;
    }

    .highlighted {
        background-color: #f3f9ff;
    }

    .section-list {

        padding-left: 1.7em;

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