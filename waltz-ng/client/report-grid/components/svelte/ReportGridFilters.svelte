<script>
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {getDisplayNameForColumn} from "./report-grid-utils";
    import {activeSummaries, filters, summaries} from "./report-grid-store";
    import {mkChunks} from "../../../common/list-utils";
    import EntityIcon from "../../../common/svelte/EntityIcon.svelte";

    const supportedColumnKinds = ["ASSESSMENT_DEFINITION", "MEASURABLE", "DATA_TYPE", "APP_GROUP", "INVOLVEMENT_KIND", "COST_KIND"];

    let chunkedSummaryData = [];

    function isSelectedSummary(cId) {
        return _.some(
            $filters,
            f => f.summaryId === cId);
    }


    function onToggleFilter(optionSummary) {
        if (isSelectedSummary(optionSummary.summaryId)) {
            $filters = _.reject(
                $filters,
                f => f.summaryId === optionSummary.summaryId);
        } else {
            const newFilter = {
                summaryId: optionSummary.summaryId,
                columnDefinitionId: optionSummary.columnDefinitionId,
                optionCode: optionSummary.optionInfo.code
            };
            $filters = _.concat($filters, [newFilter]);
        }
    }


    function removeSummary(summary) {
        activeSummaries.remove(summary.column.id);
        // remove any filters which refer to the property used by this summary
        $filters = _.reject($filters, f => f.columnDefinitionId === summary.column.id);
    }


    function addToActiveSummaries(column) {
        activeSummaries.add(column.id);
    }


    function addOrRemoveFromActiveSummaries(summary) {
        if (isActive($activeSummaries, summary)) {
            removeSummary(summary);
        } else {
            addToActiveSummaries(summary.column);
        }
    }


    function isActive(activeSummaries, summary) {
        return _.includes(activeSummaries, summary.column.id)
    }


    function mkOptionSummaryTitle(option) {
        const optionName = option.optionInfo.name || "Not Provided";

        if (option.counts.total !== option.counts.visible){
            return `${optionName}: (${option.counts.total}) ${option.counts.visible}`
        } else {
            return `${optionName}: ${option.counts.visible}`
        }
    }


    $: {
        const byColDefId = _.keyBy(
            $summaries,
            d => d.column.id);

        const activeSummaryDefs = _
            .chain($activeSummaries)
            .map(d => byColDefId[d])
            .compact()
            .value();

        chunkedSummaryData = mkChunks(activeSummaryDefs, 3);
    }

    $: availableSummaries = _.filter(
        $summaries,
        s => _.includes(supportedColumnKinds, s.column.columnEntityKind));
</script>

<div>
    <!-- NO SUMMARIES -->
    {#if _.isEmpty($summaries)}
        <NoData class="small"
               style="display: inline-block; margin-bottom: 0.5em;">
                <strong>No summaries selected</strong>
                <p>
                    There are no active summaries.  To add one select the
                    'Add to summary' option in the column drop down menu.
                    Once added you can select summary rows to quickly filter
                    the report.
                </p>
        </NoData>
    {:else }
        <!-- HELP -->
        <p class="help-block small">
            Select a value in the summary tables to quickly filter the data.
            Select the row again to clear the filter.
            You can add more filters using the list to the right.

        </p>
    {/if}


    <div class="row">
        <div class="col-sm-8">
            {#each chunkedSummaryData as row}
                <div class="row">
                    {#each row as summary}
                        <div class="col-sm-4">
                            <h5 class="waltz-visibility-parent">
                                <EntityIcon kind={summary.column.columnEntityKind}/>
                                <span>{getDisplayNameForColumn(summary?.column)}</span>
                                <button class="btn btn-skinny waltz-visibility-child-30 clickable pull-right"
                                        on:click={() => removeSummary(summary)}>
                                    <Icon name="close"/>
                                </button>
                            </h5>
                            <table class="table table-condensed small option-summary-table">
                                <tbody>
                                {#each summary.optionSummaries as optionSummary}
                                    <tr class="clickable"
                                        class:undefined-option={optionSummary.optionInfo.code === undefined}
                                        class:waltz-highlighted-row={isSelectedSummary(optionSummary.summaryId)}
                                        class:text-muted={optionSummary.counts.visible === 0}
                                        on:click={() => onToggleFilter(optionSummary)}>
                                        <td>
                                            <div class="color-indicator"
                                                 style={`
                                                    opacity: ${optionSummary.counts.visible > 0 ? 1 : 0.2};
                                                    background-color: ${optionSummary.optionInfo.color}`}>
                                            </div>
                                            <span>{optionSummary.optionInfo.name || "Not Provided"}</span>
                                        </td>
                                        <!-- COUNTERS -->
                                        <td class="text-right">
                                            <!-- TOTAL COUNTER -->
                                            {#if optionSummary.counts.total !== optionSummary.counts.visible}
                                                <span class="text-muted small">
                                                    ({optionSummary.counts.total})
                                                </span>
                                            {/if}
                                            <!-- VISIBLE COUNTER -->
                                            <span>{optionSummary.counts.visible}</span>
                                        </td>
                                    </tr>
                                {/each}
                                </tbody>
                                <!-- TOTAL -->
                                <tbody>
                                <tr>
                                    <td>
                                        <b>Total</b>
                                    </td>
                                    <td class="text-right">
                                        {#if summary.total !== summary.totalVisible}
                                            <span class="text-muted small">
                                                ({summary.total})
                                            </span>
                                        {/if}
                                        <span>{summary.totalVisible}</span>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    {/each}
                </div>
            {/each}
        </div>
        <div class="col-sm-4">
            <h5>
                <Icon name="filter"/>
                Filter Picker
            </h5>
            <div class:waltz-scroll-region-350={_.size(availableSummaries) > 10}>
                <table class="table table-condensed small summary-table table-hover">
                    <tbody>
                    {#each availableSummaries as summary}
                        <tr on:click={() => addOrRemoveFromActiveSummaries(summary)}
                            class="clickable waltz-visibility-parent"
                            class:isActiveFilter={isActive($activeSummaries, summary)}>
                            <td>
                                <span class="waltz-visibility-child-30">
                                    <Icon name={isActive($activeSummaries, summary) ? 'check' : 'arrow-left'}/>
                                </span>
                                <span class="column-name">
                                {getDisplayNameForColumn(summary.column)}
                            </span>
                                <ul style="display: inline-block"
                                    class="list-inline column-values-summary">
                                    {#each summary.optionSummaries as option}
                                        <li title={mkOptionSummaryTitle(option)}>
                                            <span style={`
                                                    background-color: ${option.optionInfo.color};
                                                    opacity: ${option.counts.visible > 0 ? 1 : 0.2};
                                            `}/>
                                        </li>
                                    {/each}
                                </ul>
                            </td>
                    </tr>
                    {:else}
                        <tr>
                            <td>
                                <NoData type="info">
                                    <span>No columns available for use as filters</span>
                                </NoData>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            </div>

        </div>
    </div>
</div>

<style>
    .column-name {
        padding-right: 1em;
    }

    .column-values-summary {
        margin-bottom: 0;
    }

    .summary-table td {
        padding: 2px;
    }

    .option-summary-table tr {
        transition: background-color 0.2s linear;
      }

    .summary-table tr {
        transition: background-color 0.4s linear;
      }

    .column-values-summary li {
        padding: 0.2em;
    }

    .column-values-summary span {
        border: 1px solid #ccc;
        display: inline-block;
        height: 1em;
        width: 1em;
        transition: opacity 0.3s linear;
    }

    .isActiveFilter {
        background-color: #fdfde2;
    }

    .undefined-option {
        font-style: italic;
    }

    .color-indicator {
        display: inline-block;
        height: 10px; width: 10px;
        transition: opacity 0.3s linear;
    }

</style>
