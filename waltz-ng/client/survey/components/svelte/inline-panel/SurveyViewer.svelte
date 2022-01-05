<script>

    import {
        formDetails,
        groupedQuestions,
        responses,
        responsesByQuestionId,
        selectedSection,
        surveyDetails
    } from "./survey-detail-store";
    import _ from "lodash";
    import SurveyQuestionResponse from "./SurveyQuestionResponse.svelte";
    import SurveyViewerContextPanel from "./SurveyViewerContextPanel.svelte";
    import {surveyInstanceViewStore} from "../../../../svelte-stores/survey-instance-view-store";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";


    export let primaryEntityRef;

    let instanceCall, formDetailsCall, responsesCall;

    $: {
        if (primaryEntityRef) {
            instanceCall = surveyInstanceViewStore.getInfoById(primaryEntityRef.id);
            formDetailsCall = surveyInstanceViewStore.getFormDetailsById(primaryEntityRef.id);
            responsesCall = surveyInstanceStore.findResponses(primaryEntityRef.id);
        }
    }

    $: $surveyDetails = $instanceCall?.data;
    $: $formDetails = $formDetailsCall?.data;
    $: $responses = $responsesCall?.data;

    $: sectionsToShow = $selectedSection
        ? [$selectedSection]
        : $groupedQuestions;

    function onChangeInstance(d) {
        primaryEntityRef = Object.assign({}, primaryEntityRef, {id: d.detail});
    }

</script>


<div class="row">
    <div class="col-sm-8 question-list">
        {#each sectionsToShow as section}
            <div class="section col-md-12">
                <div class="row section-question-header">
                    <div class="col-md-12">{section?.sectionName}</div>
                </div>

                {#each section?.questions as question}
                    <div class="row section-question">
                        <div class="col-md-6 help-block">
                            {question?.questionText}
                            {#if question?.isMandatory}
                            <span class="mandatory"
                                  title="This question is mandatory">*</span>
                            {/if}
                        </div>
                        <div class:col-md-6={_.isEmpty(question?.subQuestions)}
                             class:col-md-2={!_.isEmpty(question?.subQuestions)}
                             class="force-wrap">
                            {#if question?.label}
                                <div class="help-block sub-question-label">{question.label}</div>
                            {/if}
                            <SurveyQuestionResponse {question}
                                                    response={_.get($responsesByQuestionId, question.id, null)}/>
                        </div>
                        {#if question.subQuestions}
                            {#each question?.subQuestions as subQuestion}
                                <div class="col-md-2">
                                    {#if question?.label}
                                        <div class="help-block sub-question-label">{subQuestion?.label}</div>
                                    {/if}
                                    <SurveyQuestionResponse question={subQuestion}
                                                            response={_.get($responsesByQuestionId, subQuestion.id, null)}/>
                                </div>
                            {/each}
                        {/if}
                    </div>
                {/each}
            </div>
        {/each}
    </div>

    {#if primaryEntityRef}
    <div class="col-sm-4"
         style="padding-left: 0">
        <SurveyViewerContextPanel on:select={onChangeInstance}
                                  instanceId={primaryEntityRef.id}/>
    </div>
    {/if}
</div>


<style type="text/scss">

    @import "style/variables";

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

    .question-list {
    }

    .mandatory {
        color: #a94442;
    }

</style>