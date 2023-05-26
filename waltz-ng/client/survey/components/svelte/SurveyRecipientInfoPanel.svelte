<script>

    import SurveyInstanceGrid from "./SurveyInstanceGrid.svelte";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {surveyInstanceStatus} from "../../../common/services/enums/survey-instance-status";
    import {selectedSurvey, selectedSurveyStatusCell} from "./user-survey-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";
    import SurveyViewer from "./inline-panel/SurveyViewer.svelte";

    export let surveys = [];

    let gridData = [];

    const Modes = {
        INFO: "INFO",
        GRID: "GRID",
        SURVEY: "SURVEY",
    }

    let activeMode = Modes.INFO;

    let columnDefs = [
        { field: "surveyInstance.surveyEntity.name", name: "Subject Name", width: "30%"},
        { field: "surveyInstance.surveyEntityExternalId", name: "Subject Ext Id"},
        { field: "surveyInstance.qualifierEntity.name", name: "Qualifier"},
        { field: "displayStatus", name: "Status"},
        { field: "surveyInstance.dueDate", name: "Due Date"}
    ];

    let currentDate = new Date();
    let weekFromNow = new Date();
    let monthFromNow = new Date();

    weekFromNow.setDate(weekFromNow.getDate() + 7);
    monthFromNow.setDate(currentDate.getDate() + 30);

    const tableHeaders = [
        {
            cellClass: "rejected",
            name: "Rejected",
            longName: "Rejected surveys",
            description: "Survey owner has rejected survey. Survey must be reopened then recipients are required to update and resubmit",
            width: "10%",
            data: d => d.rejected
        }, {
            cellClass: "overdue",
            name: "Overdue",
            longName: "Overdue surveys",
            description: "Survey is past its submission due date within Waltz",
            width: "10%",
            data: d => d.overdue
        }, {
            cellClass: "due-week",
            name: "Due Week",
            longName: "Incomplete surveys - due within 7 days",
            description: "Survey is due to be submitted within 7 days",
            width: "10%",
            data: d => d.dueWeek
        }, {
            cellClass: "due-month",
            name: "Due Month",
            longName: "Incomplete surveys - due within 30 days",
            description: "Survey is due to be submitted within 30 days",
            width: "10%",
            data: d => d.dueMonth
        }, {
            cellClass: "incomplete",
            name: "Total Outstanding",
            longName: "Total outstanding - all incomplete surveys",
            description: "All surveys requiring completion (sum of overdue, due week, due month and anything with a submission due date over a month)",
            width: "10%",
            data: d => d.incomplete
        }, {
            cellClass: "awaiting-approval",
            headerClass: "secondary",
            name: "Awaiting Approval",
            longName: "Completed surveys - awaiting approval",
            description: "Completed and submitted surveys waiting approval by a survey owner",
            width: "10%",
            data: d => d.completed
        }, {
            cellClass: "approved",
            headerClass: "secondary",
            longName: "Approved surveys",
            description: "Survey has been approved by the survey owner, no further action required",
            name: "Approved",
            width: "10%",
            data: d => d.approved
        }
    ]

    function selectSurveyFilter(header, templateInfo) {
        $selectedSurveyStatusCell = {header, templateInfo};
        $selectedSurvey = null;
        gridData = header.data(templateInfo)
        activeMode = Modes.GRID
    }

    function selectRow(d) {
        $selectedSurvey =  Object.assign({}, d.surveyInstance, {kind: "SURVEY_INSTANCE"});
        activeMode = Modes.SURVEY
    }

    function goToNext(d){
        $selectedSurvey = Object.assign({}, d, {kind: "SURVEY_INSTANCE"});
    }

    function determineClass(selectedHeader, header, templateInfo){
        return !_.isEmpty(header.data(templateInfo))
            ? header.cellClass
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

            const [overdue, outstanding] = _.partition(incomplete, d => new Date(d.surveyInstance.dueDate) < currentDate);
            const [dueWeek, moreThanWeek] = _.partition(outstanding, d => new Date(d.surveyInstance.dueDate) < weekFromNow);
            const [dueMonth, future] = _.partition(moreThanWeek, d => new Date(d.surveyInstance.dueDate) < monthFromNow);

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
        .orderBy(d => d.template.name)
        .value();

    $: templatesById = _
        .chain(surveys)
        .map(d => d.surveyTemplateRef)
        .compact()
        .keyBy(d => d.id)
        .value()

    $: surveysByStatus = _.keyBy(surveys, d => d.surveyInstance.status);
    $: incompleteSurveys = _.concat(_.get(surveysByStatus, "IN_PROGRESS", []) , _.get(surveysByStatus, "NOT_STARTED", []));

    $: currentSurvey = _.findIndex(gridData, d => d?.surveyInstance?.id === $selectedSurvey?.id);
    $: previousSurvey =  _.get(gridData[currentSurvey -1], 'surveyInstance', null);
    $: nextSurvey =  _.get(gridData[currentSurvey +1], 'surveyInstance', null);

</script>

<div class="help-block small">
    <Icon name="pencil-square-o" size="4x" pullLeft={true}/>
    The table below details the surveys for which you are an assigned recipient.
    Recipients are responsible for completing a survey.
    Select a filter to see the survey details and use the table to navigate to them.
</div>
{#if _.isEmpty(templateSummaries)}
    <br>
    <NoData>There are no surveys where you are an assigned recipient</NoData>
{:else}
    <table class="table table-condensed">
        <thead>
        <tr>
            <th width="30%"
                style="text-align: left">
                Survey Name
            </th>
            {#each tableHeaders as header}
                <th width={`${60 / tableHeaders.length}%`}
                    class={header.headerClass}
                    title={header.description}>
                    {header.name}
                </th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each templateSummaries as templateInfo}
            <tr>
                <td>{templateInfo.template.name}</td>
                {#each tableHeaders as header}
                    <td class={determineClass($selectedSurveyStatusCell, header, templateInfo)}
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
        <tr class="total-row">
            <td>Total</td>
            <td colspan="4">
            </td>
            <td>
                <span>{_.sumBy(templateSummaries, d => _.size(d.incomplete))}
                </span>
            </td>
            <td></td>
        </tr>
        </tbody>
    </table>

    <br>
    <hr>

    {#if activeMode === Modes.INFO}
        <NoData>There are no surveys for the current selection</NoData>
    {:else if activeMode === Modes.GRID}
        <h4>{$selectedSurveyStatusCell?.header.longName}</h4>

        <div class="help-block small">
            {$selectedSurveyStatusCell?.header.description}
        </div>
        <div class="help-block small">
            <Icon name="info-circle"/>Select a survey from the list below for detail and responses, or to execute any available actions.
        </div>
        <br>
        <SurveyInstanceGrid {columnDefs}
                            rowData={gridData}
                            onSelectRow={selectRow}/>
    {:else if activeMode === Modes.SURVEY}
        <h4>
            <button class="breadcrumb btn-skinny"
                    on:click={() => selectSurveyFilter($selectedSurveyStatusCell?.header, $selectedSurveyStatusCell?.templateInfo)}>
                {$selectedSurveyStatusCell?.header.description}
            </button>
            / {$selectedSurvey?.surveyEntity?.name || "Unknown"}
        </h4>
        <div class="help-block small">
            <Icon name="info-circle"/>To navigate back to the filtered survey list click on the link above or select a different filter.
        </div>
        <div style="padding: 0.5em">
            <div class="col-md-6"
                 style="border-right: solid 1px #cccccc; padding-right: 0.5em">
                {#if previousSurvey}
                    <button class="btn btn-skinny pull-right"
                            on:click={() => goToNext(previousSurvey)}>
                        <Icon name="arrow-circle-left"/> Previous survey ({previousSurvey?.surveyEntity?.name  || "Unknown"})
                    </button>
                {:else}
                    <span class="text-muted pull-right">No previous surveys</span>
                {/if}
            </div>
            <div class="col-md-6"
                 style="border-left: solid 1px #cccccc; padding-left: 0.5em">
                {#if nextSurvey}
                    <button class="btn btn-skinny"
                            on:click={() => goToNext(nextSurvey)}>
                        Next survey ({nextSurvey?.surveyEntity?.name || "Unknown"}) <Icon name="arrow-circle-right"/>
                    </button>
                {:else}
                    <span class="text-muted">No further surveys</span>
                {/if}
            </div>
        </div>
        <br>
        <SurveyViewer primaryEntityRef={$selectedSurvey}/>
    {/if}
{/if}

<style type="text/scss">
    @import '../../../../style/variables';

    .breadcrumb {
        padding: 0;
    }

    .overdue {
        background-color: $waltz-red-background;

        &.selected {
            outline: solid 1px $waltz-red;
        }
    }

    .rejected {
        background-color: $waltz-maroon-background;
        &.selected {
            outline: solid 1px $waltz-dark-red;
        }
    }

    .due-week {
        background-color: $waltz-orange-background;

        &.selected {
            outline: solid 1px $waltz-orange;
        }
    }

    .due-month {
        background-color: $waltz-amber-background;
        &.selected {
            outline: solid 1px $waltz-amber;
        }
    }

    .incomplete {
        background-color: $waltz-blue-background;

        &.selected {
            outline: solid 1px $waltz-blue;
        }
    }

    .awaiting-approval {
        background-color: $waltz-lime-background;

        &.selected {
            outline: solid 1px $waltz-lime;
        }
    }


    .approved {
        background-color: $waltz-green-background;

        &.selected {
            outline: solid 1px $waltz-green;
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

    th.secondary {
        color: #777;
    }

    .total-row {
        color: #777;
        font-weight: bold
    }

</style>