
<script>

    import EntityLink from "../EntityLink.svelte";
    import {surveyInstanceViewStore} from "../../../svelte-stores/survey-instance-view-store";
    import DateTime from "../DateTime.svelte";

    import {surveyInstanceStatus} from "../../services/enums/survey-instance-status";
    import _ from "lodash";
    import {toSurveyName} from "../../../survey/components/svelte/inline-panel/survey-viewer-utils";
    import DescriptionFade from "../DescriptionFade.svelte";
    import DatePicker from "../DatePicker.svelte";
    import {surveyInstanceStore} from "../../../svelte-stores/survey-instance-store";
    import ToastStore from "../../../svelte-stores/toast-store";
    import {displayError} from "../../error-utils";

    export let instanceId;

    function reload() {
        surveyCall = surveyInstanceViewStore.getInfoById(instanceId, true);
    }

    function updateSubmissionDueDate(d) {
        const updDate = d.detail;
        let updatePromise = surveyInstanceStore.updateSubmissionDueDate(instanceId, updDate);

        Promise.resolve(updatePromise)
            .then(() => {
                ToastStore.success("Updated submission due date");
                return reload();
            })
            .catch(e => displayError("Failed to update submission due date and refresh page", e));
    }

    function updateApprovalDueDate(d) {
        const updDate = d.detail;
        let updatePromise = surveyInstanceStore.updateApprovalDueDate(instanceId, updDate);

        Promise.resolve(updatePromise)
            .then(() => {
                ToastStore.success("Updated approval due date");
                return reload();
            })
            .catch(e => displayError("Failed to update approval due date and refresh page", e));
    }

    let surveyCall;
    let permissionsCall;

    $: {
        if (instanceId) {
            surveyCall = surveyInstanceViewStore.getInfoById(instanceId);
            permissionsCall = surveyInstanceStore.getPermissions(instanceId);
        }
    }

    $: survey = $surveyCall?.data;
    $: permissions = $permissionsCall?.data;

    $: surveyName = toSurveyName(survey);

    $: descContext = survey
        ? {entity: survey.surveyInstance.surveyEntity, instance: survey.surveyInstance}
        : null;

</script>


{#if survey}
    <h4><EntityLink ref={Object.assign(survey.surveyInstance, {name: surveyName})}/></h4>
    <slot name="post-title"/>

    <slot name="pre-header"/>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">Template Name</td>
                <td width="50%">{survey.surveyTemplateRef?.name}</td>
            </tr>
            <tr>
                <td width="50%">Run Name</td>
                <td width="50%">{survey.surveyRun?.name}</td>
            </tr>
            <tr>
                <td width="50%">Subject</td>
                <td width="50%">
                    <EntityLink ref={survey.surveyInstance?.surveyEntity}/>
                </td>
            </tr>
            <tr>
                <td width="50%">Issued On</td>
                <td width="50%">
                    <DatePicker origDate={survey.surveyInstance.issuedOn}/>
                </td>
            </tr>
            <tr>
                <td width="50%">Status</td>
                <td width="50%">{_.get(surveyInstanceStatus[survey.surveyInstance?.status], "name", "-")}</td>
            </tr>
            <tr>
                <td width="50%">Submission Due Date</td>
                <td width="50%">
                    {#key survey.surveyInstance?.dueDate}
                        <DatePicker canEdit={permissions?.isMetaEdit}
                                    origDate={survey.surveyInstance.dueDate}
                                    options={{maxDate: survey.surveyInstance.approvalDueDate}}
                                    on:change={updateSubmissionDueDate}/>
                    {/key}
                </td>
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
                <td width="50%">
                    {#key survey.approvalInstance?.dueDate}
                        <DatePicker canEdit={permissions?.isMetaEdit}
                                    origDate={survey.surveyInstance.approvalDueDate}
                                    options={{minDate: survey.surveyInstance.dueDate}}
                                    on:change={updateApprovalDueDate}/>
                    {/key}
                </td>
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


    <div class="help-block small">
        <DescriptionFade text={survey.surveyRun?.description}
                         context={descContext}/>
    </div>



    <slot name="footer"/>
{/if}