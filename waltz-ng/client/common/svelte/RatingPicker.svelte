<script>
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import {truncateMiddle} from "../string-utils";
    import Icon from "./Icon.svelte";

    export let scheme;
    export let isMultiSelect = true;

    const dispatch = createEventDispatcher();

    let selectedItems = [];

    function onSelect(item) {
        if (isMultiSelect) {
            const existing = selectedItems.indexOf(item) > -1;

            if (existing) {
                selectedItems = _.without(selectedItems, item);
            } else {
                selectedItems = _.concat(selectedItems, [item])
            }
        } else {
            selectedItems = [item];
        }
        dispatch("select", selectedItems);
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
            class:selected={selectedItems.indexOf(item) > -1}
            on:click={() => onSelect(item)}>
            <td>
                {#if isMultiSelect}
                    <Icon size="2x"
                          name={selectedItems.indexOf(item) > -1
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
</table>

<style>
    .selected {
        background-color: #DFF1D2;
    }
</style>