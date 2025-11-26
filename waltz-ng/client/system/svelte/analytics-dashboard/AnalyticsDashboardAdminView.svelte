<script>
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import _ from 'lodash';
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import ChangeLogTrendsChart from "./ChangeLogTrendsChart.svelte";
    import AccessLogTrendsChart from "./AccessLogTrendsChart.svelte";
    import TopPagesChart from "./TopPagesChart.svelte";
    import ActivityHeatmapChart from "./ActivityHeatmapChart.svelte";
    import SeverityDonutChart from "./SeverityDonutChart.svelte";
    import DayOfWeekRadarChart from "./DayOfWeekRadarChart.svelte";
    import TopContributorsTreemap from "./TopContributorsTreemap.svelte";
    import EntityKindBubbleChart from "./EntityKindBubbleChart.svelte";
    import OperationsDonutChart from "./OperationsDonutChart.svelte";
    import OperationTrendsChart from "./OperationTrendsChart.svelte";
    import AccessLogOperationTrendsChart from "./AccessLogOperationTrendsChart.svelte";

    const FREQUENCY_OPTIONS = [
        { name: "Day", value: "day" },
        { name: "Week", value: "week" },
        { name: "Month", value: "month" },
        { name: "Year", value: "year" }
    ];

    let selectedFrequency = "month";
    let startDate = new Date(new Date().setMonth(new Date().getMonth() - 6)).toISOString().split('T')[0];
    let endDate = new Date().toISOString().split('T')[0];
    let chartKey = Date.now();
    let refreshing = false;

    function refreshCharts() {
        chartKey = Date.now();
        console.log('Charts refreshed with key:', chartKey);
    }

    // Auto-refresh when filters change
    $: {
        if (selectedFrequency && startDate && endDate) {
            refreshCharts();
        }
    }
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
        <div class="col-md-12">
            <SubSection>
                <div slot="header">
                    Date Range & Frequency Filters
                </div>
                <div slot="content">
                    <div class="row row-mini-gutters">
                        <div class="col-md-3">
                            <div class="waltz-display-field-label">Start Date</div>
                            <input type="date"
                                   id="startDate"
                                   class="form-control"
                                   bind:value={startDate} />
                        </div>
                        <div class="col-md-3">
                            <div class="waltz-display-field-label">End Date</div>
                            <input type="date"
                                   id="endDate"
                                   class="form-control"
                                   bind:value={endDate} />
                        </div>
                        <div class="col-md-3">
                            <div class="waltz-display-field-label">Frequency</div>
                            <select id="frequency"
                                    class="form-control"
                                    bind:value={selectedFrequency}>
                                {#each FREQUENCY_OPTIONS as option}
                                    <option value={option.value}>{option.name}</option>
                                {/each}
                            </select>
                        </div>
                        <div class="col-md-3" style="display: flex; align-items: end;">
                            <button class="btn btn-primary"
                                    style="margin-top: 25px;"
                                    disabled={refreshing}
                                    on:click={async () => {
                                        refreshing = true;
                                        console.log('Refresh button clicked!');
                                        refreshCharts();
                                        // Give visual feedback
                                        await new Promise(resolve => setTimeout(resolve, 1000));
                                        refreshing = false;
                                    }}>
                        {#if refreshing}
                            🔄 Refreshing...
                        {:else}
                            Refresh
                        {/if}
                            </button>
                        </div>
                    </div>
                </div>
            </SubSection>
        </div>
    </div>
    <hr/>

    <!-- ACCESS LOG ANALYTICS SECTION -->
    <div class="row" style="margin-bottom: 30px">
        <div class="col-md-12">
            <h3>
                <i class="fa fa-users"></i> Access Log Analytics
            </h3>
        </div>
    </div>

    <!-- Access Log Charts Row 1 -->
    <div class="row" style="margin-bottom: 20px">
        <div class="col-md-6">
            <AccessLogTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
        <div class="col-md-6">
            <DayOfWeekRadarChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
    </div>

    <!-- Access Log Charts Row 2 -->
    <div class="row" style="margin-bottom: 20px">
        <div class="col-md-6">
            <TopPagesChart
                {startDate}
                {endDate}
                key={chartKey} />
        </div>
        <div class="col-md-6">
            <ActivityHeatmapChart
                {startDate}
                {endDate}
                key={chartKey} />
        </div>
    </div>

    <!-- Access Log Operation Trends Chart (Full Width) -->
    <div class="row" style="margin-bottom: 30px">
        <div class="col-md-12">
            <AccessLogOperationTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
    </div>

    <!-- CHANGE LOG ANALYTICS SECTION -->
    <div class="row" style="margin-bottom: 30px">
        <div class="col-md-12">
            <h3>
                <i class="fa fa-edit"></i> Change Log Analytics
            </h3>
        </div>
    </div>

    <!-- Change Log Charts Row 1 -->
    <div class="row" style="margin-bottom: 20px">
        <div class="col-md-6">
            <ChangeLogTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
        <div class="col-md-6">
            <TopContributorsTreemap
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
    </div>

    <!-- Change Log Charts Row 2 -->
    <div class="row" style="margin-bottom: 20px">
        <div class="col-md-6">
            <OperationsDonutChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
        <div class="col-md-6">
            <SeverityDonutChart
                {startDate}
                {endDate}
                key={chartKey} />
        </div>
    </div>

    <!-- Change Log Charts Row 3 - Merged Entity & Child Entity Kind -->
    <div class="row" style="margin-bottom: 20px">
        <div class="col-md-12">
            <EntityKindBubbleChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
    </div>

    <!-- Operation Trends Chart (Full Width) -->
    <div class="row" style="margin-bottom: 30px">
        <div class="col-md-12">
            <OperationTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency}
                key={chartKey} />
        </div>
    </div>
</div>
