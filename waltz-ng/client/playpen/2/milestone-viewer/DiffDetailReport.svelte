<script>
    export let report;
    export let measurablesById;

    const niceName = {
        g: "Buy",
        r: "Sell",
        a: "Hold"
    };

    let selectedRow = null;


    let detail = [];

    function showRow(row) {
        console.log({row})
        selectedRow = row;
    }

    function showSummary() {
        selectedRow = null;
    }

    $: console.log({report})
</script>


{#if !selectedRow}
    <table class="table table-condensed small">
        <thead>
        <th>From</th>
        <th>To</th>
        <th>Count</th>
        </thead>
        <tbody>
        {#each report.diff as row}
            <tr on:click={() => showRow(row)}>
                <td class={`rating-${row.r1}`}>
                    {niceName[row.r1] || "-" }
                </td>
                <td class={`rating-${row.r2}`}>
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
    <table class="table table-condensed small">
        <thead on:click={() => showSummary()}>
            <th>From</th>
            <th>To</th>
            <th>Thing</th>
        </thead>
        <tbody>
        {#each selectedRow.changes as change }
            <tr>
                <td class={`rating-${selectedRow.r1}`}>
                    {niceName[selectedRow.r1] || "-" }
                </td>
                <td class={`rating-${selectedRow.r2}`}>
                    {niceName[selectedRow.r2] || "-" }
                </td>
                <td>
                    {change.name}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
    <h3>Row</h3>
{/if}


<style type="text/scss">
    @import "../../../../style/variables";
    .rating-a {
        background: $waltz-amber-background;
    }
    .rating-r {
        background: $waltz-red-background;
    }
    .rating-g {
        background: $waltz-green-background;
    }
</style>
