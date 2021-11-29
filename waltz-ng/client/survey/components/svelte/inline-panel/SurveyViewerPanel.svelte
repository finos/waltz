<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import {groupedQuestions, responsesByQuestionId, selectedSection} from "./survey-detail-store";
    import _ from "lodash";
    import {surveyInstanceViewStore} from "../../../../svelte-stores/survey-instance-view-store";
    import EntityInfoPanel from "../../../../common/svelte/info-panels/EntityInfoPanel.svelte";
    import SurveyOverviewSubPanel from "./SurveyOverviewSubPanel.svelte";
    import SurveyInstanceInfoPanel from "../../../../common/svelte/info-panels/SurveyInstanceInfoPanel.svelte";

    export let primaryEntityRef;

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
        const surveyInstanceId = evt.detail;
        surveyCall = surveyInstanceViewStore.getById(surveyInstanceId, true);
    }

    let selectedTab = 'sections';

    $: surveyCall = surveyInstanceViewStore.getById(primaryEntityRef?.id);
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

<SurveyOverviewSubPanel on:action={onAction}
                        {questionsWithResponse}
                        {survey}/>


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
    {#if selectedTab === 'sections'}
        <!-- SECTIONS -->
        <div class="help-block small">
            <Icon name="info-circle"/>Select a section below to focus on its questions
        </div>
        <ul class="section-list">
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
    {:else if selectedTab === 'detail'}
        <!-- SURVEY INSTANCE DETAILS -->
        <SurveyInstanceInfoPanel {primaryEntityRef}>
        </SurveyInstanceInfoPanel>
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