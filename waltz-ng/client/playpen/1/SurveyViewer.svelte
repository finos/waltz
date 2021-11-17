<script>

    import {questions, responses, selectedSection} from "./survey-detail-store";
    import {groupQuestions, indexResponses} from "../../survey/survey-utils";
    import _ from "lodash";
    import SurveyQuestionResponse from "./SurveyQuestionResponse.svelte";
    import Icon from "../../common/svelte/Icon.svelte";

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
    <div class="help-block small">
        <Icon name="info-circle"/>Select a section below to focus on its questions
    </div>
    <ul class="section-list">
        {#each groupedQuestions as section}
                <li class="clickable section-list-item"
                    on:mouseenter={() => section.hovering = true}
                    on:mouseleave={() => section.hovering = false}
                    class:highlighted={section.hovering}
                    class:selected={section === $selectedSection}
                    on:click={() => selectSection(section)}>
                        {section.sectionName}
                </li>
        {/each}
    </ul>
</div>
<div class="col-sm-9 question-list">
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
        color: $text-muted;
        font-weight: bold;
        border-bottom: 1px solid #ddd;
        background-color: #fafafa;
        background: linear-gradient(90deg, #fafafa 0%, rgba(255,255,255,1) 100%);
    }

    .sub-question-label {
        font-size: small;
    }

    .section-question {
        padding-top: 0.5em;
    }

    .section {
        outline: 1px solid #ddd;
        margin-bottom: 1em;
        padding-bottom: 0.5em;
    }

    .highlighted {
        background-color: #f3f9ff;
    }

    .question-list {
        padding-top: 2.5em;
    }

</style>