<script>

    import SurveyInstanceGrid from "./SurveyInstanceGrid.svelte";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {surveyInstanceStatus} from "../../../common/services/enums/survey-instance-status";
    import {selectedSurveyStatusCell, selectSurveyRow} from "./user-survey-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";

    export let surveys = [];

    let gridData = [];

    let columnDefs = [
        { field: "surveyInstance.surveyEntity.name", name: "Subject Name", width: "30%"},
        { field: "surveyInstance.surveyEntityExternalId", name: "Subject Ext Id"},
        { field: "surveyInstance.qualifierEntity.name", name: "Qualifier"},
        { field: "displayStatus", name: "Status"},
        { field: "surveyRun.dueDate", name: "Due Date"}
    ];

    let currentDate = new Date();
    let weekFromNow = new Date();
    let monthFromNow = new Date();

    weekFromNow.setDate(weekFromNow.getDate() + 7);
    monthFromNow.setDate(currentDate.getDate() + 30);

    const tableHeaders = [
        {
            class: "rejected",
            name: "Rejected",
            description: "Rejected surveys",
            width: "10%",
            data: d => d.rejected
        },{
            class: "overdue",
            name: "Overdue",
            description: "Overdue surveys",
            width: "10%",
            data: d => d.overdue
        },{
            class: "due-week",
            name: "Due Week",
            description: "Incomplete surveys - due within 7 days",
            width: "10%",
            data: d => d.dueWeek
        },{
            class: "due-month",
            name: "Due Month",
            description: "Incomplete surveys - due within 30 days",
            width: "10%",
            data: d => d.dueMonth
        },{
            class: "incomplete",
            name: "Total Outstanding",
            description: "Total outstanding - all incomplete surveys",
            width: "10%",
            data: d => d.incomplete
        },{
            class: "awaiting-approval",
            name: "Awaiting Approval",
            description: "Completed surveys - awaiting approval",
            width: "10%",
            data: d => d.completed
        },{
            class: "approved",
            description: "Approved surveys",
            name: "Approved",
            width: "10%",
            data: d => d.approved
        }
    ]

    function selectSurveyFilter(header, templateInfo) {
        $selectedSurveyStatusCell = {header, templateInfo};
        gridData = header.data(templateInfo)
    }

    function selectRow(d) {
        $selectSurveyRow(d.surveyInstance);
    }

    function determineClass(selectedHeader, header, templateInfo){
        return !_.isEmpty(header.data(templateInfo))
            ? header.class
            : "";
    }

    onMount(() => $selectedSurveyStatusCell = null);

    $: byTemplateId = _
        .chain(surveys)
        .map(d => Object.assign({}, d, { displayStatus: _.get(surveyInstanceStatus[d.surveyInstance.status], "name", d.surveyInstance.status)}))
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
    <Icon name="info-circle"/>The table below details the surveys for which you are an assigned recipient. Recipients are responsible for
    completing a survey. Select a filter to see the survey details and use the table to navigate to them.
</div>
{#if _.isEmpty(templateSummaries)}
    <NoData>There are no surveys where you are an assigned recipient</NoData>
{:else}
    <table class="table table-condensed">
        <thead>
        <tr>
            <th width="30%">Survey Name</th>
            {#each tableHeaders as header}
                <th width={`${60 / tableHeaders.length}%`}>{header.name}</th>
            {/each}
            <th width="10%">Total</th>
        </tr>
        </thead>
        <tbody>
        {#each templateSummaries as templateInfo}
            <tr>
                <td>{templateInfo.template.name}</td>
                {#each tableHeaders as header}
                    <td on:click|stopPropagation={() => selectSurveyFilter(header, templateInfo)}
                        class={determineClass($selectedSurveyStatusCell, header, templateInfo)}
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
                <td><div>{_.size(byTemplateId[templateInfo.template.id])}</div></td>
            </tr>
        {/each}
        </tbody>
    </table>

    <hr>

    {#if _.isEmpty(gridData)}
        <NoData>There are no surveys for the current selection</NoData>
    {:else }
        <h4>{$selectedSurveyStatusCell?.header.description}</h4>
        <SurveyInstanceGrid {columnDefs}
                            rowData={gridData}
                            onSelectRow={selectRow}/>
    {/if}
{/if}

<style type="text/scss">
    @import '../../../../style/variables';

    .overdue {
        background-color: $waltz-red-background;

        &.selected {
            border: solid 1px $waltz-red;
        }
    }

    .rejected {
        background-color: $waltz-maroon-background;
        &.selected {
            border: solid 1px $waltz-dark-red;
        }
    }

    .due-week {
        background-color: $waltz-orange-background;

        &.selected {
            border: solid 1px $waltz-orange;
        }
    }

    .due-month {
        background-color: $waltz-amber-background;
        &.selected {
            border: solid 1px $waltz-amber;
        }
    }

    .incomplete {
        background-color: $waltz-blue-background;

        &.selected {
            border: solid 1px $waltz-blue;
        }
    }

    .awaiting-approval {
        background-color: $waltz-lime-background;

        &.selected {
            border: solid 1px $waltz-lime;
        }
    }

    .approved {
        background-color: $waltz-green-background;

        &.selected {
            border: solid 1px $waltz-green;
        }
    }

    td * {
        width: 100%;
        height: 100%;
        display: block;
        text-align: center;
    }

    th {
        text-align: center;
    }

</style>