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
    import CopySurveyResponsesPanel from "./CopySurveyResponsesPanel.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import { surveyCustomFieldTypes } from "../../survey-custom-fields";
    import ARCSurveyComponent from "../arc-survey-components/ARCSurveyComponent.svelte";

    export let primaryEntityRef;

    const Modes = {
        CONTEXT: "CONTEXT",
        COPY_RESPONSES: "COPY_RESPONSES"
    }

    let activeMode = Modes.CONTEXT

    let additionalFooterActions = [
        {
            name: "Copy Responses",
            icon: "clone",
            onClick: () => activeMode = Modes.COPY_RESPONSES
        }
    ];

    let instanceCall, formDetailsCall, responsesCall;

    $: {
        if (primaryEntityRef) {
            instanceCall = surveyInstanceViewStore.getInfoById(primaryEntityRef.id, true);
            formDetailsCall = surveyInstanceViewStore.getFormDetailsById(primaryEntityRef.id, true);
            responsesCall = surveyInstanceStore.findResponses(primaryEntityRef.id, true);
        }
    }

    $: $surveyDetails = $instanceCall?.data;
    $: $formDetails = $formDetailsCall?.data;
    $: $responses = $responsesCall?.data;

    $: sectionsToShow = $selectedSection
        ? [$selectedSection]
        : $groupedQuestions;

    function onChangeInstance(d) {
        activeMode = Modes.CONTEXT;
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
                        {#if !surveyCustomFieldTypes[question.fieldType]}
                            <div class="col-md-6 help-block">
                                <div>
                                    {question?.questionText}
                                    {#if question?.isMandatory}
                                        <span class="mandatory"
                                              title="This question is mandatory">*</span>
                                    {/if}
                                </div>
                                {#if question?.externalId}
                                    <div class="text-muted small"
                                         style="word-break: break-all">
                                        ({question?.externalId})
                                    </div>
                                {/if}
                                {#if question?.helpText}
                                    <div class="text-muted small"
                                         style="padding-top: 1em">
                                        <Markdown text={question?.helpText}/>
                                    </div>
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
                        {:else if question.fieldType === surveyCustomFieldTypes.ARC}
                            <ARCSurveyComponent question={question}
                                                instanceId={$surveyDetails?.surveyInstance?.id}
                                                currentResponse={$responsesByQuestionId[question?.id]?.jsonResponse}
                                                linkedEntityKind={$surveyDetails?.surveyInstance?.surveyEntity?.kind}
                                                linkedEntityId={$surveyDetails?.surveyInstance?.surveyEntity?.id}/>
                        {/if}
                    </div>
                {/each}
            </div>
        {/each}
    </div>

    {#if primaryEntityRef}
    <div class="col-sm-4"
         style="padding-left: 0">
        {#if activeMode === Modes.CONTEXT}
            <SurveyViewerContextPanel on:select={onChangeInstance}
                                      mode="VIEW"
                                      {additionalFooterActions}
                                      instanceId={primaryEntityRef.id}/>
        {:else if activeMode === Modes.COPY_RESPONSES}
            <CopySurveyResponsesPanel on:cancel={() => activeMode = Modes.CONTEXT}/>
        {/if}
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
        padding-top: 0.5em;
        padding-bottom: 0.3em;
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