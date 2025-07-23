<script>
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import { accessLogStore } from "../../../svelte-stores/access-log-store";
    import { changeLogSummariesStore } from "../../../svelte-stores/change-log-summaries-store";
    import _ from 'lodash';
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import YearOnYearUsersChart from "./YearOnYearUsersChart.svelte";
    import YoYChangeLogsChart from "./YoYChangeLogsChart.svelte";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";
    import GridWithCellRenderer from "../../../common/svelte/GridWithCellRenderer.svelte";
    import ViewLinkLabelled from "../../../common/svelte/ViewLinkLabelled.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";

    const VIEW_MODES = {
        YEARLY: "Yearly",
        MONTHLY: "Monthly"
    }

    const numberToMonthMap = {
        1: "Jan",
        2: "Feb",
        3: "Mar",
        4: "Apr",
        5: "May",
        6: "Jun",
        7: "Jul",
        8: "Aug",
        9: "Sep",
        10: "Oct",
        11: "Nov",
        12: "Dec"
    }

    const viewModeItemList = Object.values(VIEW_MODES)
        .map(d => ({name: d}));

    let viewMode = null;

    let selectedAccessLogYear = new Date().getFullYear();
    let selectedChangeLogYear = new Date().getFullYear();

    $: mostViewedPagesSinceDays = viewMode === VIEW_MODES.MONTHLY ? 31 : 365;

    $: mostViewedPagesCall = accessLogStore.findAccessLogsSince(mostViewedPagesSinceDays);

    $: changeLogYearsListCall = changeLogSummariesStore.findChangeLogYears();

    $: accessLogYearsListCall = accessLogStore.findAccessLogYears();

    $: changeLogYearsListData = $changeLogYearsListCall.data;

    $: accessLogYearsListData = $accessLogYearsListCall.data;

    $: changeLogYearsList = changeLogYearsListData
        ? changeLogYearsListData
            .map(d => ({name: d}))
            .sort((a, b) => b.name - a.name)
        : [];

    $: accessLogYearsList = accessLogYearsListData
        ? accessLogYearsListData
            .map(d => ({name: d}))
            .sort((a, b) => b.name - a.name)
        : [];

    $: mostViewedPages = $mostViewedPagesCall.data;

    $: mostViewedPagesGridData = mostViewedPages
        ? mostViewedPages
            .sort((a, b) => b.counts - a.counts)
            .slice(0, 30)
            .map(d => ({state: d.state, counts: d.counts.toLocaleString()}))
        : [];

const mostViewedPagesGridColumns = [
    {
        name: "Page",
        field: "state",
        maxLength: 50,
        cellRendererComponent: ViewLinkLabelled,
        cellRendererProps: row => ({
            state: row.state.split("|")[0],
            label: row.state,
            ctx: {
                id: Math.floor(Math.random()*100)
            }
        }),
        width: 20
    },
    { name: "Views", field: "counts", width: 20 }
];

</script>

<PageHeader icon="bar-chart"
            name="Analytics Dashboard">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Analytics Dashboard</li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-sm-12">
            <div class="col-sm-12">
                <SubSection>
                    <div slot="header">
                        Filters
                    </div>
                    <div slot="content">
                        <div class="row row-mini-gutters">
                            <div class="col-sm-4">
                                <DropdownPicker
                                    items={viewModeItemList}
                                    onSelect={d => _.isNull(d) ? viewMode = null : viewMode = d.name}
                                    selectedItem={_.find(viewModeItemList, d => d != null ? d.name === viewMode : null) ?? null}
                                    defaultMessage="Filter Timeframe"/>
                            </div>
                            {#if !_.isNull(viewMode) && viewMode === VIEW_MODES.MONTHLY}
                                <div class="col-sm-4">
                                    <DropdownPicker
                                        items={accessLogYearsList}
                                        onSelect={d => selectedAccessLogYear = d.name }
                                        selectedItem={_.find(accessLogYearsList, d => d.name === selectedAccessLogYear)}
                                        defaultMessage="Filter Access Logs"/>
                                </div>
                                <div class="col-sm-4">
                                    <DropdownPicker
                                        items={changeLogYearsList}
                                        onSelect={d => selectedChangeLogYear = d.name }
                                        selectedItem={_.find(changeLogYearsList, d => d.name === selectedChangeLogYear)}
                                        defaultMessage="Filter Change Logs"/>
                                </div>
                            {/if}
                        </div>
                    </div>
                </SubSection>
            </div>
        </div>
    </div>
    <hr/>
    <div class="row" style="margin-bottom: 10px">
        <div class="col-sm-12">
            <YearOnYearUsersChart
                chartTimeFrame={viewMode}
                year={selectedAccessLogYear}
                numToMonthMap={numberToMonthMap}/>
        </div>
    </div>
    <div class="row" style="margin-bottom: 10px">
        <div class="col-sm-12">
            <div class="col-sm-6">
                <SubSection>
                    <div slot="header">
                        Most Viewed Pages in the past {viewMode === VIEW_MODES.MONTHLY ? "Month" : "Year"} [Top 30]
                    </div>
                    <div slot="content">
                        <div class="row">
                            <div class="col-sm-12">
                            {#if mostViewedPagesGridData.length > 0}
                                    <GridWithCellRenderer
                                        columnDefs={mostViewedPagesGridColumns}
                                        rowData={mostViewedPagesGridData}
                                        onSelectRow={(r) => console.log(r)}>
                                    </GridWithCellRenderer>
                            {:else}
                                <LoadingPlaceholder/>
                            {/if}
                            </div>
                        </div>
                    </div>
                </SubSection>
            </div>
            <div class="col-sm-6">
                <YoYChangeLogsChart
                    chartTimeFrame={viewMode}
                    year={selectedChangeLogYear}
                    numToMonthMap={numberToMonthMap}/>
            </div>
        </div>
    </div>
</div>