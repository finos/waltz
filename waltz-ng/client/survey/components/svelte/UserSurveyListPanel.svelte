<script>

    import {surveyInstanceViewStore} from "../../../svelte-stores/survey-instance-view-store";
    import SurveyRecipientInfoPanel from "./SurveyRecipientInfoPanel.svelte";
    import _ from "lodash";
    import SurveyApproverInfoPanel from "./SurveyApproverInfoPanel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    let selectedTab = 'completions';

    let surveyInstanceCall = surveyInstanceViewStore.findForUser();
    $: surveyInstances = $surveyInstanceCall?.data;

    $: surveysByInvolvementKind = _.keyBy(surveyInstances, d => d.surveyInvolvementKind);

    $: recipientSurveys = _.get(surveysByInvolvementKind["RECIPIENT"], "surveyInstances", []);
    $: approverSurveys = _.get(surveysByInvolvementKind["OWNER"], "surveyInstances", []);

    $: incompleteSurveys = _.filter(recipientSurveys, c => c.surveyInstance.status === 'IN_PROGRESS' || c.surveyInstance.status === 'NOT_STARTED');
    $: completedSurveys = _.filter(approverSurveys, c => c.surveyInstance.status === 'COMPLETED');

</script>

<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="completions"
           id="completions">
    <label class="wt-label"
           for="completions">
        <span><Icon name="pencil-square-o"/>Surveys to Complete - {_.size(incompleteSurveys)}</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="approvals"
           id="approvals">
    <label class="wt-label"
           for="approvals">
        <span><Icon name="check-square-o"/>Surveys to Approve - {_.size(completedSurveys)}</span>
    </label>

    <div class="wt-tab wt-active">
        {#if selectedTab === 'completions'}
            <SurveyRecipientInfoPanel surveys={_.get(surveysByInvolvementKind, 'RECIPIENT', []).surveyInstances}/>
        {:else if selectedTab === 'approvals'}
            <SurveyApproverInfoPanel surveys={_.get(surveysByInvolvementKind, 'OWNER', []).surveyInstances}/>
        {/if}
    </div>
</div>
