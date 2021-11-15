<script>

    import SurveyInstanceGrid from "./SurveyInstanceGrid.svelte";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {surveyInstanceStatus} from "../../../common/services/enums/survey-instance-status";
    import {selectedSurveyStatusCell, selectSurveyRow} from "./user-survey-store";
    import {timeFormat} from "d3-time-format";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";

    export let surveys = [];

    let gridData = [];

    let currentDate = new Date();
    let weekFromNow = new Date();
    let monthFromNow = new Date();

    weekFromNow.setDate(weekFromNow.getDate() + 7);
    monthFromNow.setDate(currentDate.getDate() + 30);

    let columnDefs = [
        { field: "surveyInstance.surveyEntity.name", name: "Subject Name", width: "20%"},
        { field: "surveyInstance.surveyEntityExternalId", name: "Subject Ext Id"},
        { field: "surveyInstance.qualifierEntity.name", name: "Qualifier", width: "15%"},
        { field: "displayStatus", name: "Status"},
        { field: "surveyInstance.submittedBy", name: "Submitter", width: "15%"},
        { field: "displaySubmittedAt", name: "Submitted"},
        { field: "surveyInstance.approvedBy", name: "Approver", width: "15%"},
        { field: "displayApprovedAt", name: "Approved"},
    ]

    const dateFormat = timeFormat("%Y-%m-%d");

    const tableHeaders = [
        {
            class: "awaiting-approval",
            name: "Awaiting Approval",
            description: "Completed surveys - awaiting approval",
            width: "25%",
            data: d => d.completed
        },{
            class: "overdue",
            name: "Overdue",
            description: "Overdue surveys - awaiting completion",
            width: "25%",
            data: d => d.overdue
        },{
            class: "rejected",
            name: "Rejected",
            description: "Rejected surveys",
            width: "25%",
            data: d => d.rejected
        },{
            class: "approved",
            description: "Approved surveys",
            name: "Approved",
            width: "25%",
            data: d => d.approved
        }
    ]

    onMount(() => $selectedSurveyStatusCell = null);

    function selectSurveyFilter(header, templateInfo) {
        $selectedSurveyStatusCell = {header, templateInfo};
        gridData = header.data(templateInfo)
    }

    function selectRow(d) {
        $selectSurveyRow(d.surveyInstance);
    }


    $: byTemplateId = _
        .chain(surveys)
        .map(d => Object.assign(
            {},
            d,
            {
                displayApprovedAt: _.isEmpty(d.surveyInstance.approvedAt) ? null : dateFormat(new Date(d.surveyInstance.approvedAt)),
                displaySubmittedAt: _.isEmpty(d.surveyInstance.submittedAt) ? null : dateFormat(new Date(d.surveyInstance.submittedAt)),
                displayStatus: _.get(surveyInstanceStatus[d.surveyInstance.status], "name", d.surveyInstance.status)
            }))
        .orderBy(d => _.toLower(d.surveyInstance.surveyEntity.name))
        .groupBy(d => d.surveyTemplateRef.id)
        .value();

    $: templateSummaries = _
        .chain(byTemplateId)
        .map((v, k) => {

            const surveysByStatus = _.groupBy(v, d => d.surveyInstance.status);

            const incomplete = _.orderBy(
                _.concat(
                    _.get(surveysByStatus, ["NOT_STARTED"], []),
                    _.get(surveysByStatus, ["IN_PROGRESS"], [])),
                d => _.toLower(d.surveyInstance.surveyEntity.name));
            const completed = _.get(surveysByStatus, ["COMPLETED"], [])
            const approved = _.get(surveysByStatus, ["APPROVED"], [])
            const rejected = _.get(surveysByStatus, ["REJECTED"], [])

            const [overdue, outstanding] = _.partition(incomplete, d => new Date(d.surveyRun.dueDate) < currentDate);
            const [dueWeek, moreThanWeek] = _.partition(outstanding, d => new Date(d.surveyRun.dueDate) < weekFromNow);
            const [dueMonth, future] = _.partition(moreThanWeek, d => new Date(d.surveyRun.dueDate) < monthFromNow);

            return {
                template: templatesById[k],
                incomplete,
                approved,
                rejected,
                completed,
                overdue,
                dueWeek,
                dueMonth
            }})
        .value();

    $: templatesById = _
        .chain(surveys)
        .map(d => d.surveyTemplateRef)
        .compact()
        .keyBy(d => d.id)
        .value()

    $: surveysByStatus = _.keyBy(surveys, d => d.surveyInstance.status);
    $: incompleteSurveys = _.concat(_.get(surveysByStatus, "IN_PROGRESS", []) , _.get(surveysByStatus, "NOT_STARTED", []));

</script>

<div class="help-block">
    <Icon name="info-circle"/>The table below details the surveys for which you are an assigned owner. Owners are responsible
    for approving, rejecting and reopening surveys. Select a filter to see the individual survey details and navigate to them.
</div>
{#if _.isEmpty(templateSummaries)}
    <NoData>There are no surveys where you are an assigned owner</NoData>
{:else}
    <table class="table table-condensed">
        <thead>
        <tr>
            <th width="30%">Survey Name</th>
            {#each tableHeaders as header}
                <th width={`${60 / tableHeaders.length}%`}>{header.name}</th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each templateSummaries as templateInfo}
            <tr>
                <td>{templateInfo.template.name}</td>
                {#each tableHeaders as header}
                    <td class={_.isEmpty(header.data(templateInfo)) ? "" : header.class}
                        class:selected={$selectedSurveyStatusCell?.header === header && $selectedSurveyStatusCell?.templateInfo === templateInfo}>
                        {#if _.isEmpty(header.data(templateInfo))}
                            <div class="text-muted">0</div>
                        {:else}
                            <button class="btn btn-skinny"
                                    on:click={() => selectSurveyFilter(header, templateInfo)}>
                                {_.size(header.data(templateInfo))}
                            </button>
                        {/if}
                    </td>
                {/each}
            </tr>
        {/each}
        </tbody>
    </table>

    <hr>

    {#if _.isEmpty(gridData)}
        <NoData>There are no surveys for the current selection</NoData>
    {:else }
        <h4>{$selectedSurveyStatusCell.header.description}:</h4>
        <SurveyInstanceGrid {columnDefs}
                            rowData={gridData}
                            onSelectRow={selectRow}/>
    {/if}
{/if}

<style type="text/scss">
    @import '../../../../style/variables';

    .overdue {
        background-color: $waltz-amber-background;

        &.selected {
            outline: solid 1px $waltz-amber;
        }
    }

    .awaiting-approval{
        background-color: $waltz-blue-background;

        &.selected {
            outline: solid 1px $waltz-blue;
        }
    }

    .rejected {
        background-color: $waltz-maroon-background;

        &.selected {
            outline: solid 1px $waltz-dark-red;
        }
    }

    .approved{
        background-color: $waltz-green-background;

        &.selected {
            outline: solid 1px $waltz-green;
        }
    }

    td * {
        width: 100%;
        display: block;
        text-align: center;
    }

    th {
        text-align: center;
    }

</style>