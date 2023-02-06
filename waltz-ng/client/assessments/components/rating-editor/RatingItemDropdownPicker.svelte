<script>


    import _ from "lodash";
    import {createEventDispatcher} from "svelte";

    export let ratings = [];
    export let selectedRatingId;

    let ratingId = selectedRatingId;
    let dropdownConfig;

    $: {

        const grouped = _
            .chain(ratings)
            .sortBy([d => d.ratingGroup, d => d.position, d => d.name])
            .groupBy("ratingGroup")
            .value();

        if (grouped[null] && _.size(grouped) > 1) {
            grouped["Ungrouped"] = grouped[null];
            delete grouped[null];
        }

        dropdownConfig = grouped[null]
            ? {style: "simple", options: grouped[null]}
            : {style: "grouped", groups: _.map(grouped, (v, k) => ({groupName: k, options: v}))};

    }

    const dispatch = createEventDispatcher();

    $: {
        const selectedRating = _.find(ratings, d => d.id === ratingId);
        dispatch("select", selectedRating);
    }


</script>

<select class="form-control"
        bind:value={ratingId}>
    {#if dropdownConfig.style === 'simple'}
        {#each dropdownConfig.options as entry}
            <option value={entry.id}>
                {entry.name}
            </option>
        {/each}
    {:else if dropdownConfig.style === 'grouped'}
        {#each dropdownConfig.groups as g}
            <optgroup label={g.groupName}>
                {#each g.options as entry}
                    <option value={entry.id}>
                        {entry.name}
                    </option>
                {/each}
            </optgroup>
        {/each}
    {/if}
</select>

