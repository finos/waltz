<script>
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {mkPropNameForRef} from "./report-grid-utils";
    import {filters, selectedGrid, summaries, activeSummaryColRefs} from "./report-grid-store";
    import {mkChunks} from "../../../common/list-utils";

    function isSelectedCounter (cId) {
        return _.some($filters, f => f.counterId === cId);
    }

    function onToggleFilter(counter) {
        if (_.some($filters, f => f.counterId === counter.counterId)) {
            $filters = _.reject($filters, f => f.counterId === counter.counterId);
        } else {
            const newFilter = {
                counterId: counter.counterId,
                propName: counter.colRef,
                ratingId: counter.rating.id
            };
            $filters = _.concat($filters, [newFilter]);
        }
    }

    $: chunkedSummaryData = mkChunks(
        _.filter(
            $summaries,
            d => _.includes($activeSummaryColRefs, mkPropNameForRef(d.column.columnEntityReference))),
        4);


    $: $activeSummaryColRefs =  _
        .chain($selectedGrid?.definition.columnDefinitions)
        .filter(d => d.usageKind === "SUMMARY")
        .map(d => mkPropNameForRef(d.columnEntityReference))
        .value();

    function onRemoveSummary(summary) {
        const refToRemove = mkPropNameForRef(summary.column.columnEntityReference);
        $activeSummaryColRefs = _.reject($activeSummaryColRefs, ref => ref === refToRemove);
        // remove any filters which refer to the property used by this summary
        $filters = _.reject($filters, f => f.propName === refToRemove);
    }

    $: console.log({chunkedSummaryData, fs: $filters, acS: $activeSummaryColRefs, ss: $summaries})

</script>

<div>
    <!-- NO SUMMARIES -->
    {#if _.isEmpty(chunkedSummaryData)}
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
            You can add more summaries using the column menu ('Add to summary').
        </p>
    {/if}

    <div class="row">
        {#each chunkedSummaryData as row}
            {#each row as summary}
                <div class="col-sm-3">
                    <h5 class="waltz-visibility-parent">
                        <span>{summary.column.columnEntityReference.name}</span>
                        <button class="btn btn-skinny waltz-visibility-child-30 clickable pull-right"
                                on:click={() => onRemoveSummary(summary)}>
                            <Icon name="close"/>
                        </button>
                    </h5>
                    <table class="table table-condensed small">
                        <tbody>
                        {#each summary.counters as counter}
                            <tr class="clickable"
                                class:waltz-highlighted-row={isSelectedCounter(counter.counterId)}
                                class:text-muted={counter.counts.visible === 0}
                                on:click={() => onToggleFilter(counter)}>
                                <td>
                                    <div style={`
                                        display: inline-block;
                                        height: 10px; width: 10px;
                                        background-color: ${counter.rating.color}`}>
                                    </div>
                                    <span>{counter.rating.name}</span>
                                </td>
                                <!-- COUNTERS -->
                                <td class="text-right">
                                    <!-- TOTAL COUNTER -->
                                    {#if counter.counts.total !== counter.counts.visible}
                                    <span class="text-muted small">
                                            (
                                            <span>{counter.counts.total}</span>
                                            )
                                        </span>
                                    {/if}
                                    <!-- VISIBLE COUNTER -->
                                    <span>{counter.counts.visible}</span>
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
                                        (
                                        <span>{summary.total}</span>
                                        )
                                    </span>
                                    <span>{summary.totalVisible}</span>
                                {/if}
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            {/each}
        {/each}
    </div>
</div>