<script>

    import Grid from "../../../playpen/1/Grid.svelte";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {groupAndMap} from "../../../common/map-utils";

    export let surveys = [];

    let gridData = [];

    let columnDefs = [
        { field: "surveyInstance.surveyEntity.name", name: "Subject Name"},
        { field: "surveyInstance.surveyEntity.externalId", name: "Subject Ext Id"},
        { field: "surveyRun.name", name: "Survey Run"},
        { field: "surveyInstance.status", name: "Status"},
        { field: "surveyRun.issuedOn", name: "Issued"},
        { field: "surveyRun.dueDate", name: "Due Date"},
    ]

    $: surveysByTemplate = _.groupBy(surveys, d => d.surveyTemplateRef.id)

    // $: templateSurveyInfo = _
    //     .chain(surveysByTemplate)
    //     .map((v, k))

    $: grouped = groupAndMap()

    $: templatesById = _.chain(surveys)
        .map(d => d.surveyTemplateRef)
        .compact()
        .keyBy(d => d.id)
        .value()

    $: surveysByStatus = _.keyBy(surveys, d => d.surveyInstance.status);
    $: incompleteSurveys = _.concat(_.get(surveysByStatus, "IN_PROGRESS", []) , _.get(surveysByStatus, "NOT_STARTED", []));

    $: console.log({surveysByTemplate, surveysByStatus, incompleteSurveys, templatesById});

    let currentDate = new Date();
    let weekFromNow = new Date();
    let monthFromNow = new Date();

    weekFromNow.setDate(weekFromNow.getDate() + 7);
    monthFromNow.setMonth(currentDate.getMonth() + 1);

    let overdue;
    let outstanding;
    let nextWeek;
    let nextMonth;
    let moreThanWeek;
    let future;

    $: [overdue, outstanding] = _.partition(incompleteSurveys, d => console.log(d.surveyRun.dueDate) || new Date(d.surveyRun.dueDate) < currentDate);
    $: [nextWeek, moreThanWeek] = _.partition(outstanding, d => new Date(d.surveyRun.dueDate) < weekFromNow);
    $: [nextMonth, future] = _.partition(moreThanWeek, d => new Date(d.surveyRun.dueDate) < monthFromNow);

    console.log({currentDate, weekFromNow, monthFromNow})

</script>

<table class="table table-condensed">
    <thead>
        <th>Due Week</th>
        <th>Due Month</th>
        <th>Overdue</th>
        <th>Total</th>
    </thead>
    <tbody>
        <tr>
            <td><button class="btn btn-info" on:click={() => gridData = nextWeek}>{_.size(nextWeek)}</button></td>
            <td><button class="btn btn-info" on:click={() => gridData = nextMonth}>{_.size(nextMonth)}</button></td>
            <td><button class="btn btn-info" on:click={() => gridData = overdue}>{_.size(overdue)}</button></td>
            <td><button class="btn btn-info" on:click={() => gridData = incompleteSurveys}>{_.size(incompleteSurveys)}</button></td>
        </tr>
    </tbody>
</table>

<table class="table table-condensed">
    <thead>
        <th>Template</th>
        <th>Outstanding Surveys</th>
        <th>Awaiting Approval</th>
        <th>Approved</th>
        <th>Rejected</th>
        <th>Total</th>
    </thead>
    <tbody>
    <!--{#each surveysByTemplate as templateInfo}-->
    <!--    <tr>-->
    <!--        <td>{templatesById[templateInfo.id]}</td>-->
    <!--    </tr>-->
    <!--{/each}-->
    </tbody>
</table>

<div class="help-block">
    <Icon name="info-circle"/> The surveys below are due for completion. You can filter the list by due date above or one of the templates below
</div>

<hr>

<h4>Incomplete Surveys:</h4>
{#if _.isEmpty(gridData)}
    <NoData>There are no surveys for the current selection</NoData>
{:else }
    <Grid {columnDefs} rowData={gridData}/>
{/if}