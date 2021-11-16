<script>

    import {questions, responses, selectedSection, surveyDetails} from "./survey-detail-store";
    import {groupQuestions, indexResponses} from "../../survey/survey-utils";
    import NoData from "../../common/svelte/NoData.svelte";
    import _ from "lodash";
    import SurveyQuestionResponse from "./SurveyQuestionResponse.svelte";

    $: console.log({surveyDetails: $surveyDetails, questions: $questions, responses: $responses});

    $: groupedQuestions = groupQuestions($questions);
    $: responsesByQuestionId = indexResponses($responses);

    $: sectionsToShow = $selectedSection ? [$selectedSection] : groupedQuestions;

    function selectSection(section) {
        if ($selectedSection === section) {
            $selectedSection = null;
        } else {
            $selectedSection = section;
        }
    }

</script>


<div class="col-sm-3">
    <h4>Sections</h4>
    <ul class="section-list small">
        {#each groupedQuestions as section}
            <li class="clickable"
                class:selected={section === $selectedSection}
                on:click={() => selectSection(section)}>
                {section.sectionName}
            </li>
        {/each}
    </ul>
</div>
<div class="col-sm-9">
    {#each sectionsToShow as section}
        <div class="section col-md-12">
            <div class="row section-question-header">
                <div class="col-md-12">{section.sectionName}</div>
            </div>

            {#each section.questions as question}
                <div class="row section-question">
                    <div class="col-md-3 help-block">{question.questionText}</div>
                    <div class:col-md-9={_.isEmpty(question.subQuestions)}
                         class:col-md-3={!_.isEmpty(question.subQuestions)}>
                        {#if question.label}
                            <div class="help-block sub-question-label">{question.label}</div>
                        {/if}
                        <SurveyQuestionResponse {question}
                                                response={_.get(responsesByQuestionId, question.id, null)}/>
                    </div>
                    {#if question.subQuestions}
                        {#each question.subQuestions as subQuestion}
                            <div class="col-md-3">
                                {#if question.label}
                                    <div class="help-block sub-question-label">{subQuestion.label}</div>
                                {/if}
                                <SurveyQuestionResponse question={subQuestion}
                                                        response={_.get(responsesByQuestionId, subQuestion.id, null)}/>
                            </div>
                        {/each}
                    {/if}
                </div>
            {/each}
        </div>
    {/each}
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

    .section-question-header {
        font-weight: bold;
        border-bottom: 1px solid #ddd;
        background-color: #fafafa;
    }

    .sub-question-label {
        font-size: small;
    }

    .section-question {
        padding-top: 0.5em;
    }

    .section {
        margin-top: 3em;
        outline: 1px solid #ddd;
        margin-bottom: 1em;
        padding-bottom: 0.5em;
    }

</style>