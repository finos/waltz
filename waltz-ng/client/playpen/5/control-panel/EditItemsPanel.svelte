<script>


    import _ from "lodash";
    import {items, selectedGroup, groups} from "../diagram-builder-store";
    import {mkGroup, mkItem} from "../diagram-builder-utils";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import EntityPicker from "../../../report-grid/components/svelte/pickers/EntityPicker.svelte";
    import {sameRef} from "../../../common/entity-utils";


    const Modes = {
        VIEW: "VIEW",
        ADD: "ADD"
    }

    let activeMode = Modes.ADD;

    function addItem(entity) {
        const itemNumber = _.size($items) + 1;
        const newItem = mkItem("Item " + itemNumber.toString(), itemNumber, $selectedGroup.id, entity)
        $items = _.concat($items, newItem);

        const groupNumber = _.size($groups) + 1;
        const newGroup = mkGroup("Group " + groupNumber.toString(), groupNumber, $selectedGroup.id, entity)
        $groups = _.concat($groups, newGroup);
    }

    function removeItem(item) {
        $items = _.reject($items, d => d.id === item.id);
    }

    function deselectItem(entity) {
        $items = _.reject($items, d => d.groupId === $selectedGroup.id && sameRef(d.data, entity));
    }

    $: alreadyAddedFilter = (entity) => {
        console.log({entity, items: $items, groups: $groups});
        return !_.some($items, d => d.groupId === $selectedGroup.id && sameRef(d.data, entity)) || !_.some($groups, d => d.id === $selectedGroup.id && d.data && sameRef(d.data, entity));
    }

</script>


{#if activeMode === Modes.VIEW}
    <ul>
        {#each $selectedGroup.items as item}
            <li>
                {item.title} / <EntityLink ref={item.data}/>
                <button class="btn btn-skinny"
                        on:click={() => removeItem(item)}>
                    Delete
                </button>
            </li>
        {/each}
        <li>
            <button class="btn btn-default"
                    on:click={() => activeMode = Modes.ADD}>
                Add Item
            </button>
        </li>
    </ul>
{:else if activeMode === Modes.ADD}

    <EntityPicker entityKind={$selectedGroup.itemKind}
                  onSelect={addItem}
                  onDeselect={deselectItem}
                  selectionFilter={alreadyAddedFilter}/>

    <button class="btn btn-default"
            on:click={() => activeMode = Modes.ADD}>
        Cancel
    </button>
{/if}
