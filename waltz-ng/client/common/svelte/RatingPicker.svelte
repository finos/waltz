<script>
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import {truncateMiddle} from "../string-utils";
    import Icon from "./Icon.svelte";

    export let scheme;
    export let isMultiSelect = true;
    export let selectedRatings = [];

    const dispatch = createEventDispatcher();

    function onSelect(item) {
        if (isMultiSelect) {
            const existing = _.find(selectedRatings, d => d.id === item.id);

            if (existing) {
                selectedRatings = _.reject(selectedRatings, d => d.id === item.id);
            } else {
                selectedRatings = _.concat(selectedRatings, [item]);
            }
        } else {
            selectedRatings = [item];
        }
        dispatch("select", selectedRatings);
    }

    function selectAll() {
        selectedRatings = [...items];
        dispatch("select", selectedRatings);
    }

    function deselectAll() {
        selectedRatings = [];
        dispatch("select", selectedRatings);
    }

    $: items = _
        .chain(scheme?.ratings)
        .orderBy([d => d.position, d => d.name])
        .value();

</script>


<table class="small table-condensed table">
    <colgroup>
        <col width="30%">
        <col width="70%">
    </colgroup>
    <thead>
    <tr>
        <th>Name</th>
        <th>Desc</th>
    </tr>
    </thead>
    <tbody>
    {#each items as item}
        <tr class="clickable"
            class:selected={_.find(selectedRatings, d => d.id === item.id)}
            on:click={() => onSelect(item)}>
            <td>
                {#if isMultiSelect}
                    <Icon size="2x"
                          name={_.find(selectedRatings, d => d.id === item.id)
                            ? 'check-square-o'
                            : 'square-o'}/>
                {/if}
                <RatingIndicatorCell {...item}/>
            </td>
            <td>
                {truncateMiddle(item.description, 200) || "-"}
            </td>
        </tr>
    {/each}
    </tbody>
    {#if isMultiSelect}
        <tbody>
            <tr>
                <td colspan="2">
                    <button class="btn-skinny"
                    on:click={selectAll}>
                        <Icon name="check-square-o"/>
                        Select All
                    </button>
                    |
                    <button class="btn-skinny"
                            on:click={deselectAll}>
                        <Icon name="square-o"/>
                        Deselect All
                    </button>
                </td>
            </tr>
        </tbody>
    {/if}
</table>

<style>
    .selected {
        background-color: #DFF1D2;
    }
</style>