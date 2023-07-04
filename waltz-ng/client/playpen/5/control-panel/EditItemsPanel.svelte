<script>


    import _ from "lodash";
    import {groups, items, selectedGroup} from "../diagram-builder-store";
    import {mkGroup} from "../diagram-builder-utils";
    import EntityPicker from "../../../report-grid/components/svelte/pickers/EntityPicker.svelte";
    import {sameRef} from "../../../common/entity-utils";
    import {createEventDispatcher} from "svelte";

    const dispatch = createEventDispatcher();

    function selectItem(entity) {

        const groupNumber = _.size($groups) + 1;
        const newGroup = mkGroup(entity.name, groupNumber, $selectedGroup.id, groupNumber, entity)
        $groups = _.concat($groups, newGroup);
    }

    function deselectItem(entity) {
        $groups = _.reject($groups, d => d.parentId === $selectedGroup.id && sameRef(d.data, entity));
    }

    $: alreadyAddedFilter = (entity) => {
        return !_.some($groups, d => d.parentId === $selectedGroup.id && d.data && sameRef(d.data, entity));
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


{#if $selectedGroup.itemKind}
    <EntityPicker entityKind={$selectedGroup.itemKind}
                  onSelect={selectItem}
                  onDeselect={deselectItem}
                  selectionFilter={alreadyAddedFilter}/>
{:else}
    <div class="help-block">No item kind has been selected for this group. Edit the group details to set the type of entity to be added to this group</div>
{/if}

<button class="btn btn-default"
        on:click={cancel}>
    Cancel
</button>

