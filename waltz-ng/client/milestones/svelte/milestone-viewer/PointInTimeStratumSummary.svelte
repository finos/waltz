<script>
    import {toMap} from "../../../common/map-utils";
    import {backgroundColors} from "./stores/decorators";

    export let data;
    export let config;

    let showingAll = false;
    let rows = [];

    $: color = config.color;
    $: measurablesById = config.measurablesById;
    $: ratings = config.ratingSchemeItems;

    $: rows = _
        .chain(data.stratum?.values)
        .flatMap((ids, rating) => _.map(
            ids,
            id => ({
                measurable: measurablesById[id],
                rating
            })))
        .value();



    function countFor(stratum, ratingId) {
        return _.size(stratum?.values[ratingId]);
    }

    function toggleShowAll() {
        showingAll = ! showingAll;
    }

    function getTotalForStratum(stratum) {
        return _
            .chain(ratings)
            .map(d => countFor(stratum, d.id))
            .sum()
            .value()
    }

    $: niceName = toMap(ratings, d => d.id, d => d.name);

    $: colWidth = 100 / (_.size(ratings) + 1);

</script>

{#if !showingAll}
<table class="table table-condensed">
    <colgroup>
        {#each ratings as rating}
        <col width="{colWidth}%">
        {/each}
        <col width="{colWidth}%">
    </colgroup>
    <thead class="clickable"
           on:click={() => toggleShowAll()}>
        {#each ratings as rating}
            <th>{rating.name}</th>
        {/each}
        <th>Total</th>
    </thead>
    <tbody>
        <tr class="clickable">
            {#each ratings as rating}
            <td style="background-color:{$backgroundColors(rating.id)}">{countFor(data.stratum, rating.id)}</td>
            {/each}
            <td><b>{getTotalForStratum(data.stratum)}</b></td>
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
            <tr style="background-color:{$backgroundColors(row.rating)}">
                <td>{row.measurable.name}</td>
                <td>{niceName[row.rating] || "?"}</td>
            </tr>
            {/each}
        </table>
    </div>
{/if}



<style type="text/scss">
    .fake-region {
        border: 1px solid #ddd;
        padding: 0.4em;
    }
</style>

