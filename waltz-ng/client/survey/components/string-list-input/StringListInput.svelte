<script>


    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {fade} from 'svelte/transition';

    export let placeholder = "...";
    export let onListChange = (items) => console.log("list changed", items);
    export let items = [];

    let workingItem;

    function mkSafe(xs) {
        return xs || [];
    }

    function addToList() {
        items = [...mkSafe(items), workingItem];
        workingItem = null;
        onListChange(items);
    }

    function removeItemFromList(idx) {
        items.splice(idx, 1);
        items = items;
        onListChange(items);
    }

</script>

{#if !_.isEmpty(items)}
    <ul class="item-list">
        {#each mkSafe(items) as item, idx (idx)}
            <li class="waltz-visibility-parent"
                transition:fade={{duration: 100}}>
                {item}
                <button class="btn btn-skinny waltz-visibility-child-50 pull-right"
                        style="color: #c70202"
                        on:click={() => removeItemFromList(idx)}>
                    <Icon name="trash"/>
                </button>
            </li>
        {/each}
    </ul>
{/if}

<form autocomplete="off"
      on:submit|preventDefault={addToList}>
    <input class="form-control input-sm"
           placeholder={placeholder}
           bind:value={workingItem}
           style="display: inline; width: 80%">
    <button class="btn btn-success"
            disabled={_.isEmpty(_.trim(workingItem))}
            type="submit">
        Add
    </button>
</form>


<style>

    .item-list {
        padding-bottom: 1em;
        list-style-type: none;
        padding-left: 0;
    }

    .item-list li {
        border-bottom: 1px solid #ccc;
        padding-bottom: 0.5em;
        margin-bottom: 0.5em;
    }

    .item-list li:last-child {
        border-bottom: none;
    }
</style>