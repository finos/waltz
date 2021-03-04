<script>
    export let report;
    export let measurablesById;
    export let color;

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
                <td style="background-color:{color.bg(`${row.r1}`)}">
                    {niceName[row.r1] || "-" }
                </td>
                <td style="background-color:{color.bg(`${row.r2}`)}">
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
                <td style="background-color:{color.bg(`${selectedRow.r1}`)}">
                    {niceName[selectedRow.r1] || "-" }
                </td>
                <td style="background-color:{color.bg(`${selectedRow.r2}`)}">
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
