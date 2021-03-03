<script>
    export let data;
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
    <thead class="clickable"
           on:click={() => toggleShowAll()}>
        <th>Buy</th>
        <th>Sell</th>
        <th>Hold</th>
    </thead>
    <tr class="clickable">
        <td>{countFor(data.stratum, "g")}</td>
        <td>{countFor(data.stratum, "r")}</td>
        <td>{countFor(data.stratum, "a")}</td>
    </tr>
</table>
{/if}

{#if showingAll}
    <div class:waltz-scroll-region-300={rows.length > 3}
         class:fake-region={rows.length <= 3}>
        <table class="table table-condensed">
            <thead class="clickable"
                   on:click={() => toggleShowAll()}>
                <th>Name</th>
                <th>Rating</th>
            </thead>
            {#each rows as row}
            <tr class="row-rating-{row.rating}">
                <td>{row.measurable.name}</td>
                <td>{niceName[row.rating] || "?"}</td>
            </tr>
            {/each}
        </table>
    </div>
{/if}



<style type="text/scss">
    @import "../../../../style/variables";
    .row-rating-a {
        background: $waltz-amber-background;
    }
    .row-rating-r {
        background: $waltz-red-background;
    }
    .row-rating-g {
        background: $waltz-green-background;
    }

    .fake-region {
        border: 1px solid #ddd;
        padding: 0.4em;
    }
</style>

