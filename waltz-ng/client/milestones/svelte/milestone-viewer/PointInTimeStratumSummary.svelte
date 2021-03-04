<script>
    export let data;
    export let color;
    export let measurablesById;

    let showingAll = false;
    let rows = [];

    $: rows = _
        .chain(data.stratum?.values)
        .flatMap((ids, rating) => _.map(
            ids,
            id => ({
                measurable: measurablesById[id],
                rating
            })))
        .value();



    function countFor(stratum, ratingCode) {
        return _.size(stratum?.values[ratingCode]);
    }

    function toggleShowAll() {
        showingAll = ! showingAll;
    }

    const niceName = {
        g: "Buy",
        r: "Sell",
        a: "Hold"
    };

</script>

{#if !showingAll}
<table class="table table-condensed">
    <colgroup>
        <col width="25%">
        <col width="25%">
        <col width="25%">
        <col width="25%">
    </colgroup>
    <thead class="clickable"
           on:click={() => toggleShowAll()}>
        <th>Buy</th>
        <th>Sell</th>
        <th>Hold</th>
        <th>Total</th>
    </thead>
    <tbody>
        <tr class="clickable">
            <td style="background-color:{color.bg('g')}">{countFor(data.stratum, "g")}</td>
            <td class="rating-r">{countFor(data.stratum, "r")}</td>
            <td class="rating-a">{countFor(data.stratum, "a")}</td>
            <td><b>{countFor(data.stratum, "a") + countFor(data.stratum, "g") + countFor(data.stratum, "r")}</b></td>
        </tr>
    </tbody>
</table>
{/if}

{#if showingAll}
    <div class:waltz-scroll-region-300={rows.length > 3}
         class:fake-region={rows.length <= 3}>
        <table class="table table-condensed">
            <colgroup>
                <col width="70%">
                <col width="30%">
            </colgroup>
            <thead class="clickable"
                   on:click={() => toggleShowAll()}>
                <th>Name</th>
                <th>Rating</th>
            </thead>
            {#each rows as row}
            <tr class="rating-{row.rating}">
                <td>{row.measurable.name}</td>
                <td>{niceName[row.rating] || "?"}</td>
            </tr>
            {/each}
        </table>
    </div>
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

    .fake-region {
        border: 1px solid #ddd;
        padding: 0.4em;
    }
</style>

