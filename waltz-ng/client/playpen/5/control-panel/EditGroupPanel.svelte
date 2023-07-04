<script>


    import _ from "lodash";
    import {selectedGroup} from "../diagram-builder-store";
    import DropdownPicker
        from "../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import {createEventDispatcher} from "svelte";

    let working = Object.assign({}, $selectedGroup);

    const dispatch = createEventDispatcher();

    let entityList = [
        entity.MEASURABLE,
        entity.DATA_TYPE,
        entity.PERSON,
    ];

    function editGroupKind(d) {
        working.itemKind = d.key;
    }

    function saveGroup() {
        dispatch("save", working);
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


<input class="form-control"
       id="title"
       maxlength="255"
       placeholder="New password"
       bind:value={working.title}/>
<div class="help-block">
    The title of this group.
</div>

<DropdownPicker items={entityList}
                onSelect={editGroupKind}
                defaultMessage="Select an item kind for this group"
                selectedItem={_.find(entityList, d => d.key === working.itemKind)}/>
<div class="help-block">
    Items in this group will be of this entity kind.
</div>

<label for="showTitle">Display Title</label>
<input type=checkbox
       id="showTitle"
       bind:checked={working.props.showTitle}>
<div class="help-block">
    Show or hide the title of this group or item.
</div>

<label for="bucketSize">Bucket Size</label>
<input id="bucketSize" type="range" min="1" max="5" bind:value={working.props.bucketSize}>

<label for="proportion">Proportion</label>
<input id="proportion" type="range" min="1" max="5" bind:value={working.props.proportion}>



<button class="btn btn-default"
        on:click={saveGroup}>
    Save
</button>
<button class="btn btn-default"
        on:click={cancel}>
    Cancel
</button>


