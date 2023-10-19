<script>

    import {entity} from "../../../common/services/enums/entity";
    import {surveyInstanceViewStore} from "../../../svelte-stores/survey-instance-view-store";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import _ from "lodash";
    import {surveyInstanceStatus} from "../../../common/services/enums/survey-instance-status";
    import DateTime from "../../../common/svelte/DateTime.svelte";
    import {termSearch} from "../../../common";
    import Toggle from "../../../common/svelte/Toggle.svelte";
    import DatePicker from "../../../common/svelte/DatePicker.svelte";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    import NoData from "../../../common/svelte/NoData.svelte";

    export let primaryEntityRef;

    let instancesCall;
    let qry;
    let hiddenStatuses = [surveyInstanceStatus.WITHDRAWN.key];

    let filterIssuedOnDate = false;
    let filterApprovalDate = false;
    let filterSubmissionDate = false;

    let today = new Date(); // start of month

    let issuedOnFilterDate = new Date(today.getFullYear(), today.getMonth(), 1); // start of month
    let approvedFilterDate = new Date(today.getFullYear(), today.getMonth(), 1); // start of month
    let submittedFilterDate = new Date(today.getFullYear(), today.getMonth(), 1); // start of month


    const issuedOnDateFilter = function(survey, date) {
        const issuedOnDate = new Date(survey.surveyInstance.issuedOn);
        return issuedOnDate > date;
    }

    const approvedDateFilter = function(survey, date) {
        const approvedDate = new Date(survey.surveyInstance.approvedAt);
        return approvedDate > date;
    }

    const submittedDateFilter = function(survey, date) {
        const submittedDate = new Date(survey.surveyInstance.submittedAt);
        return submittedDate > date;
    }

    function goToInstance(instanceId) {
        $pageInfo = {
            state: "main.survey.instance.view",
            params: {
                id: instanceId
            }
        };
    }

    $: {
        if (primaryEntityRef) {
            if (primaryEntityRef.kind === entity.PERSON.key) {
                instancesCall = surveyInstanceViewStore.findByRecipientId(primaryEntityRef.id);
            } else {
                instancesCall = surveyInstanceViewStore.findByEntityReference(primaryEntityRef);
            }
        }
    }

    $: instances = _.orderBy($instancesCall?.data || [], ["surveyInstance.issuedOn", "surveyTemplateRef.name"], ["desc", "asc"]);

    $: searchedInstances = _.isEmpty(qry)
        ? instances
        : termSearch(instances, qry, ["surveyTemplateRef.name", "surveyInstance.name", "surveyRun.name", "surveyInstance.status"]);

    $: filteredInstances = _
        .chain(searchedInstances)
        .filter(d => !_.includes(hiddenStatuses, d.surveyInstance.status))
        .filter(d => !filterIssuedOnDate || issuedOnDateFilter(d, issuedOnFilterDate))
        .filter(d => !filterSubmissionDate || submittedDateFilter(d, submittedFilterDate))
        .filter(d => !filterApprovalDate || approvedDateFilter(d, approvedFilterDate))
        .value();

    function setIssuedOnFilterDate(newDate) {
        issuedOnFilterDate = newDate;
    }

    function setSubmittedFilterDate(newDate) {
        submittedFilterDate = newDate;
    }

    function setApprovedFilterDate(newDate) {
        approvedFilterDate = newDate;
    }

    function toggleStatus(status) {
        if(_.includes(hiddenStatuses, status)) {
            hiddenStatuses = _.filter(hiddenStatuses, d => d !== status);
        } else {
            hiddenStatuses = _.concat(hiddenStatuses, status);
        }
    }

</script>


<div class="help-block">
    Surveys for this entity are listed below in order of issuance date (most recent first).
    Use the filters to limit the list or use the search to find instances of a particular template.
    Some surveys may have historical versions, click on the survey to see more details.
</div>

<details>
    <summary>
        Filters
    </summary>
    <div class="row">

        <div class="col-sm-6">
            <div style="padding-top: 1em">
                <strong>Status:</strong>
            </div>

            <div class="status-filters">
                <div>
                    <input type="checkbox"
                           checked={!_.includes(hiddenStatuses, surveyInstanceStatus.WITHDRAWN.key)}
                           on:click={() => toggleStatus(surveyInstanceStatus.WITHDRAWN.key)}>
                        {surveyInstanceStatus.WITHDRAWN.name}
                </div>
                <div>
                    <input type="checkbox"
                           checked={!_.includes(hiddenStatuses, surveyInstanceStatus.NOT_STARTED.key)}
                           on:click={() => toggleStatus(surveyInstanceStatus.NOT_STARTED.key)}>
                        {surveyInstanceStatus.NOT_STARTED.name}
                </div>
                <div>
                    <input type="checkbox"
                       checked={!_.includes(hiddenStatuses, surveyInstanceStatus.IN_PROGRESS.key)}
                       on:click={() => toggleStatus(surveyInstanceStatus.IN_PROGRESS.key)}>
                    {surveyInstanceStatus.IN_PROGRESS.name}
                </div>
                <div>
                    <input type="checkbox"
                           checked={!_.includes(hiddenStatuses, surveyInstanceStatus.COMPLETED.key)}
                           on:click={() => toggleStatus(surveyInstanceStatus.COMPLETED.key)}>
                        {surveyInstanceStatus.COMPLETED.name}
                </div>
                <div>
                    <input type="checkbox"
                           checked={!_.includes(hiddenStatuses, surveyInstanceStatus.APPROVED.key)}
                           on:click={() => toggleStatus(surveyInstanceStatus.APPROVED.key)}>
                        {surveyInstanceStatus.APPROVED.name}
                </div>
                <div>
                    <input type="checkbox"
                           checked={!_.includes(hiddenStatuses, surveyInstanceStatus.REJECTED.key)}
                           on:click={() => toggleStatus(surveyInstanceStatus.REJECTED.key)}>
                        {surveyInstanceStatus.REJECTED.name}
                </div>
            </div>
        </div>


        <div class="col-sm-6">

            <div style="padding-top: 1em">
                <strong>Dates:</strong>
            </div>
            <div class="date-filters">
                <div>
                     <Toggle labelOn="Filter Issued On"
                             labelOff="Filter Issued On"
                             state={filterIssuedOnDate}
                             onToggle={() => filterIssuedOnDate = !filterIssuedOnDate}/>
                    {#if filterIssuedOnDate}
                        <DatePicker style="padding-top: 0.5em"
                                    canEdit={true}
                                    origDate={issuedOnFilterDate}
                                    on:change={d => setIssuedOnFilterDate(d.detail)}/>
                    {/if}
                </div>

                <div>
                     <Toggle labelOn="Filter Submitted Date"
                             labelOff="Filter Submitted Date"
                             state={filterSubmissionDate}
                             onToggle={() => filterSubmissionDate = !filterSubmissionDate}/>
                    {#if filterSubmissionDate}
                        <DatePicker canEdit={true}
                                    origDate={submittedFilterDate}
                                    on:change={d => setSubmittedFilterDate(d.detail)}/>
                    {/if}
                </div>

                <div>
                     <Toggle labelOn="Filter Approval Date"
                             labelOff="Filter Approval Date"
                             state={filterApprovalDate}
                             onToggle={() => filterApprovalDate = !filterApprovalDate}/>
                    {#if filterApprovalDate}
                        <DatePicker canEdit={true}
                                    origDate={approvedFilterDate}
                                    on:change={d => setApprovedFilterDate(d.detail)}/>
                    {/if}
                </div>
            </div>
        </div>

    </div>



</details>

<SearchInput bind:value={qry}
             placeholder="Search for a survey..."/>
<br>
<div class:waltz-scroll-region-350={_.size(instances) > 12}>
    <table class="table table-condensed table-striped small table-hover">
        <thead>
        <tr>
            <th style="width: 20%">Template Name</th>
            <th style="width: 20%">Run Name</th>
            <th style="width: 15%">Instance Name</th>
            <th style="width: 10%">Status</th>
            <th style="width: 10%">Issued On</th>
            <th>Submitted</th>
            <th>Approved</th>
            <th style="width: 5%"
                title="The number of previous versions of this survey. A new version is created when the survey is reopened from 'Withdrawn', 'Rejected' or 'Approved' status.">
                Historical Versions
            </th>
        </tr>
        </thead>
        <tbody>
        {#each filteredInstances as survey}
            <tr on:click={() => goToInstance(survey.surveyInstance.id)}
            class="clickable">
                <td>{survey.surveyTemplateRef.name}</td>
                <td>{survey.surveyRun.name || "-"}</td>
                <td>{survey.surveyInstance.name || "-"}</td>
                <td>{_.get(surveyInstanceStatus, [survey.surveyInstance.status, "name"], "Unknown")}</td>
                <td>{survey.surveyInstance.issuedOn}</td>
                <td>
                    {#if survey.surveyInstance.submittedAt}
                        <DateTime relative={false}
                                  formatStr="yyyy-MM-DD"
                                  dateTime={survey.surveyInstance.submittedAt}/>
                        / {survey.surveyInstance.submittedBy}
                        {:else}
                        -
                        {/if}
                    </td>
                <td>
                    {#if survey.surveyInstance.approvedAt}
                        <DateTime relative={false}
                                  formatStr="yyyy-MM-DD"
                                  dateTime={survey.surveyInstance.approvedAt}/>
                        / {survey.surveyInstance.approvedBy}
                    {:else}
                        -
                    {/if}
                </td>
                <td><span class="pull-right">{survey.historicalVersionsCount}</span></td>
            </tr>
        {:else }
            <tr>
                <td colspan="8">
                    <NoData class="info">There are no survey results for this entity. There may be filters applied limiting these results.</NoData>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style>

    .status-filters {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 1em 2em;
    }


    .date-filters {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 1em 2em;
    }


</style>