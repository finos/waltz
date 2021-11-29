<script>
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {toSurveyName} from "./survey-viewer-utils";
    import SurveyActions from "./SurveyActions.svelte";
    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";

    export let survey;
    export let questionsWithResponse;

    $: surveyName = toSurveyName(survey);
    $: surveyRef = survey
        ? Object.assign({}, survey.surveyInstance, {name: surveyName})
        : null;

    $: subjectRef = survey?.surveyInstance?.surveyEntity;

    $: descContext = {
        entity: subjectRef,
        instance: surveyRef
    };

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
                        <EntityLink ref={surveyRef}/>
                    </td>
                </tr>
                <tr>
                    <td>Subject</td>
                    <td>
                        <EntityLink ref={subjectRef}/>
                    </td>
                </tr>
                </tbody>
            </table>


            <div class="help-block small">
                <DescriptionFade text={survey?.surveyRun.description}
                                 context={descContext}/>
            </div>


            <SurveyActions {survey}
                           {questionsWithResponse}/>

        </div>
    </div>
</div>
