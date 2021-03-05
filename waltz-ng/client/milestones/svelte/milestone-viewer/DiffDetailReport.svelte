<script>
    import {toMap} from "../../../common/map-utils";
    import {backgroundColors} from "./stores/decorators";
    import {ratingSchemeItems} from "./stores/ratings";

    export let report;

    let niceName;
    let selectedRow = null;
    let detail = [];

    $: niceName = toMap($ratingSchemeItems, d => d.id, d => d.name);

    function showRow(row) {
        selectedRow = row;
    }

    function showSummary() {
        selectedRow = null;
    }

</script>


{#if !selectedRow}
    {#if report.diff.length > 0}
        <table class="table table-condensed small">
            <colgroup>
                <col width="25%">
                <col width="25%">
                <col width="50%">
            </colgroup>
            <thead>
            <th>From</th>
            <th>To</th>
            <th>Count</th>
            </thead>
            <tbody>
            {#each report.diff as row}
                <tr on:click={() => showRow(row)}
                    class="clickable">
                    <td style="background-color:{$backgroundColors(row.r1)}">
                        {niceName[row.r1] || "-" }
                    </td>
                    <td style="background-color:{$backgroundColors(row.r2)}">
                        {niceName[row.r2] || "-" }
                    </td>
                    <td>
                        {row.changes.length}
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    {:else}
        <div class="alert alert-info">
            There are no changes over this date selection
        </div>
    {/if}
{:else}
    <table class="table table-condensed small">
        <colgroup>
            <col width="25%">
            <col width="25%">
            <col width="50%">
        </colgroup>
        <thead class="clickable"
               on:click={() => showSummary()}>
            <th>From</th>
            <th>To</th>
            <th>Technology Product</th>
        </thead>
        <tbody>
        {#each selectedRow.changes as change }
            <tr>
                <td style="background-color:{$backgroundColors(selectedRow.r1)}">
                    {niceName[selectedRow.r1] || "-" }
                </td>
                <td style="background-color:{$backgroundColors(selectedRow.r2)}">
                    {niceName[selectedRow.r2] || "-" }
                </td>
                <td>
                    {change.name}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}
