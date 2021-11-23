
<script>

    import EntityLink from "../EntityLink.svelte";
    import {surveyInstanceViewStore} from "../../../svelte-stores/survey-instance-view-store";
    import DateTime from "../DateTime.svelte";

    import {surveyInstanceStatus} from "../../services/enums/survey-instance-status";
    import _ from "lodash";

    export let primaryEntityRef;

    $: surveyCall = surveyInstanceViewStore.getById(primaryEntityRef?.id);
    $: survey = $surveyCall.data;

    $: surveyName = survey?.surveyInstance?.name || survey?.surveyRun?.name || survey?.surveyTemplateRef?.name

</script>

{#if survey}
    <h4><EntityLink ref={Object.assign(survey?.surveyInstance, {name: surveyName})}/></h4>
    <slot name="post-title"/>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">Run Name</td>
                <td width="50%">{survey.surveyRun.name}</td>
            </tr>
            <tr>
                <td width="50%">Subject</td>
                <td width="50%"><EntityLink ref={survey.surveyInstance?.surveyEntity || "Unknown"}/></td>
            </tr>
            <tr>
                <td width="50%">Status</td>
                <td width="50%">{_.get(surveyInstanceStatus[survey.surveyInstance?.status], "name", "-")}</td>
            </tr>
            <tr>
                <td width="50%">Submission Due Date</td>
                <td width="50%">{survey.surveyInstance?.dueDate}</td>
            </tr>
            {#if survey.surveyInstance?.submittedAt}
                <tr>
                    <td width="50%">Submitted</td>
                    <td width="50%">
                        <DateTime relative={false}
                                  dateTime={survey.surveyInstance?.submittedAt}
                                  formatStr="yyyy-MM-DD"/>
                        / {survey.surveyInstance?.submittedBy}
                    </td>
                </tr>
            {/if}
            <tr>
                <td width="50%">Approval Due Date</td>
                <td width="50%">{survey.surveyInstance?.approvalDueDate}</td>
            </tr>
            {#if survey.surveyInstance?.approvedAt}
                <tr>
                    <td width="50%">Approved</td>
                    <td width="50%">
                        <DateTime relative={false}
                                  dateTime={survey.surveyInstance?.approvedAt}
                                  formatStr="yyyy-MM-DD"/>
                        / {survey.surveyInstance?.approvedBy}
                    </td>
                </tr>
            {/if}
        </tbody>
    </table>

    <slot name="post-header"/>

    <slot name="footer"/>
{/if}