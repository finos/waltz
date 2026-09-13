<script>
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import { displayError } from "../../../common/error-utils";
    import { analyticsDashboardStore } from "../../../svelte-stores/analytics-dashboard-store";
    import { downloadAnalyticsWorkbook } from "./analytics-export";
    import { deriveGrouping } from "./analytics-utils";
    import moment from "moment";
    import { period } from "../../../common/services/enums/period";
    import { toOptions } from "../../../common/services/enums";
    import ChangeLogTrendsChart from "./ChangeLogTrendsChart.svelte";
    import AccessLogTrendsChart from "./AccessLogTrendsChart.svelte";
    import TopPagesChart from "./TopPagesChart.svelte";
    import TopActiveUsersChart from "./TopActiveUsersChart.svelte";
    import SessionDurationsChart from "./SessionDurationsChart.svelte";
    import ActivityHeatmapChart from "./ActivityHeatmapChart.svelte";
    import SeverityDonutChart from "./SeverityDonutChart.svelte";
    import DayOfWeekRadarChart from "./DayOfWeekRadarChart.svelte";
    import TopContributorsChart from "./TopContributorsChart.svelte";
    import EntityKindBubbleChart from "./EntityKindBubbleChart.svelte";
    import OperationsDonutChart from "./OperationsDonutChart.svelte";
    import OperationTrendsChart from "./OperationTrendsChart.svelte";

    const DATE_FORMAT = "YYYY-MM-DD";
    const DEFAULT_PRESET = "6 months";

    const groupingOptions = toOptions(period);

    // Quick ranges: each sets both dates and lets the grouping fall back to auto.
    const rangePresets = [
        { label: "7 days", days: 7 },
        { label: "30 days", days: 30 },
        { label: "3 months", months: 3 },
        { label: "6 months", months: 6 },
        { label: "12 months", months: 12 },
        { label: "Year to date", ytd: true }
    ];

    let startDate = moment().subtract(6, "months").format(DATE_FORMAT);
    let endDate = moment().format(DATE_FORMAT);
    let activePreset = DEFAULT_PRESET;
    // Empty string means "auto" (grouping derived from the range); a period key means the
    // user has overridden it.
    let groupingOverride = "";

    function applyPreset(preset) {
        endDate = moment().format(DATE_FORMAT);
        if (preset.ytd) {
            startDate = moment().startOf("year").format(DATE_FORMAT);
        } else if (preset.days) {
            startDate = moment().subtract(preset.days - 1, "days").format(DATE_FORMAT);
        } else {
            startDate = moment().subtract(preset.months, "months").format(DATE_FORMAT);
        }
        groupingOverride = "";
        activePreset = preset.label;
    }

    function onDateChange() {
        activePreset = null;
    }

    $: autoGrouping = deriveGrouping(startDate, endDate);
    $: selectedFrequency = groupingOverride || autoGrouping;
    $: resolvedGroupingName = period[selectedFrequency]?.name;

    let exporting = false;

    // Export every chart's data (for the current range + grouping) to a single
    // spreadsheet, one worksheet per chart.
    function exportToExcel() {
        exporting = true;
        toasts.info("Exporting analytics data");
        analyticsDashboardStore
            .fetchAllForExport(selectedFrequency, startDate, endDate)
            .then(datasets => {
                downloadAnalyticsWorkbook(datasets, `analytics-dashboard_${startDate}_to_${endDate}`);
                toasts.success("Analytics data exported");
            })
            .catch(e => displayError("Analytics export failed", e))
            .finally(() => exporting = false);
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
                    Date Range & Grouping
                </div>
                <div slot="content">
                    <div class="row">
                        <div class="col-md-12 preset-row">
                            <button type="button"
                                    class="btn btn-xs btn-default pull-right"
                                    on:click={exportToExcel}
                                    disabled={exporting}>
                                <Icon name={exporting ? "refresh" : "file-excel-o"}
                                      spin={exporting}/>
                                Export to Excel
                            </button>
                            <span class="waltz-display-field-label">Quick ranges</span>
                            {#each rangePresets as preset}
                                <button type="button"
                                        class="btn btn-sm"
                                        class:btn-primary={activePreset === preset.label}
                                        class:btn-default={activePreset !== preset.label}
                                        on:click={() => applyPreset(preset)}>
                                    {preset.label}
                                </button>
                            {/each}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12">
                            <div class="help-block small">
                                Pick a quick range or set custom dates. <strong>Group by</strong>
                                sets how results are bucketed; <strong>Auto</strong> chooses a bucket
                                that suits the range.
                            </div>
                        </div>
                    </div>
                    <div class="row row-mini-gutters">
                        <div class="col-md-4">
                            <div class="waltz-display-field-label">Start Date</div>
                            <input type="date"
                                   id="startDate"
                                   class="form-control"
                                   max={endDate}
                                   bind:value={startDate}
                                   on:change={onDateChange} />
                        </div>
                        <div class="col-md-4">
                            <div class="waltz-display-field-label">End Date</div>
                            <input type="date"
                                   id="endDate"
                                   class="form-control"
                                   min={startDate}
                                   bind:value={endDate}
                                   on:change={onDateChange} />
                        </div>
                        <div class="col-md-4">
                            <div class="waltz-display-field-label">Group by</div>
                            <select id="grouping"
                                    class="form-control"
                                    bind:value={groupingOverride}>
                                <option value="">Auto</option>
                                {#each groupingOptions as option}
                                    <option value={option.code}>{option.name}</option>
                                {/each}
                            </select>
                            {#if !groupingOverride}
                                <div class="help-block small">Auto &rarr; {resolvedGroupingName}</div>
                            {/if}
                        </div>
                    </div>
                </div>
            </SubSection>
        </div>
    </div>
    <hr/>

    <!-- ACCESS LOG ANALYTICS SECTION -->
    <div class="row waltz-section-gap">
        <div class="col-md-12">
            <h3>
                <Icon name="users"/> Access Log Analytics
            </h3>
            <div class="help-block">
                How people use Waltz &mdash; page hits, active users and session activity over the selected range.
            </div>
        </div>
    </div>

    <!-- Access Log Charts Row 1 -->
    <div class="row waltz-row-gap">
        <div class="col-md-6">
            <AccessLogTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency} />
        </div>
        <div class="col-md-6">
            <DayOfWeekRadarChart
                {startDate}
                {endDate} />
        </div>
    </div>

    <!-- Access Log Charts Row 2 -->
    <div class="row waltz-row-gap">
        <div class="col-md-6">
            <TopPagesChart
                {startDate}
                {endDate} />
        </div>
        <div class="col-md-6">
            <ActivityHeatmapChart
                {startDate}
                {endDate} />
        </div>
    </div>

    <!-- Access Log Charts Row 3 -->
    <div class="row waltz-section-gap">
        <div class="col-md-6">
            <TopActiveUsersChart
                {startDate}
                {endDate} />
        </div>
        <div class="col-md-6">
            <SessionDurationsChart
                {startDate}
                {endDate} />
        </div>
    </div>

    <!-- CHANGE LOG ANALYTICS SECTION -->
    <div class="row waltz-section-gap">
        <div class="col-md-12">
            <h3>
                <Icon name="edit"/> Change Log Analytics
            </h3>
            <div class="help-block">
                What changed in Waltz &mdash; change-log entries by trend, contributor, operation, severity and entity kind.
            </div>
        </div>
    </div>

    <!-- Change Log Charts Row 1 -->
    <div class="row waltz-row-gap">
        <div class="col-md-6">
            <ChangeLogTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency} />
        </div>
        <div class="col-md-6">
            <TopContributorsChart
                {startDate}
                {endDate}
                period={selectedFrequency} />
        </div>
    </div>

    <!-- Change Log Charts Row 2 -->
    <div class="row waltz-row-gap">
        <div class="col-md-6">
            <OperationsDonutChart
                {startDate}
                {endDate} />
        </div>
        <div class="col-md-6">
            <SeverityDonutChart
                {startDate}
                {endDate} />
        </div>
    </div>

    <!-- Change Log Charts Row 3 - Merged Entity & Child Entity Kind -->
    <div class="row waltz-row-gap">
        <div class="col-md-12">
            <EntityKindBubbleChart
                {startDate}
                {endDate} />
        </div>
    </div>

    <!-- Operation Trends Chart (Full Width) -->
    <div class="row waltz-section-gap">
        <div class="col-md-12">
            <OperationTrendsChart
                {startDate}
                {endDate}
                period={selectedFrequency} />
        </div>
    </div>
</div>

<style>
    .waltz-row-gap {
        margin-bottom: 20px;
    }

    .waltz-section-gap {
        margin-bottom: 30px;
    }

    .preset-row {
        margin-bottom: 12px;
    }

    .preset-row .waltz-display-field-label {
        margin-right: 10px;
    }

    .preset-row .btn {
        margin-right: 6px;
        margin-bottom: 6px;
    }
</style>
