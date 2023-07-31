<script>
    import {createEventDispatcher} from "svelte";
    import DropdownPicker
        from "../../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";

    const dispatch = createEventDispatcher();

    let working = {
        name: null,
        description: null,
        aggregatedEntityKind: entity.APPLICATION.key,
    }

    function cancel() {
        dispatch("cancel");
    }

    function save() {
        dispatch("save", working);
    }

</script>


<form autocomplete="off"
      on:submit|preventDefault={save}>

    <input class="form-control"
           id="name"
           maxlength="255"
           required="required"
           placeholder="Name"
           bind:value={working.name}/>
    <div class="help-block">
        Name of this Diagram
    </div>

    <textarea class="form-control"
              id="description"
              placeholder="Description"
              bind:value={working.description}></textarea>
    <div class="help-block">
        Description of this diagram
    </div>

    <DropdownPicker items={[entity.APPLICATION, entity.CHANGE_INITIATIVE]}
                    onSelect={(d) => working.aggregatedEntityKind = d.detail}
                    defaultMessage="Select additional column options"
                    selectedItem={entity[working.aggregatedEntityKind]}/>
    <div class="help-block">
        Entity kind aggregated as part of overlay data
    </div>

    <div style="padding-top: 1em">
        <button class="btn btn-default"
                disabled={_.isEmpty(working.name) || _.isEmpty(working.aggregatedEntityKind)}
                on:click|preventDefault={save}>
            Save
        </button>
        <button class="btn btn-default"
                on:click|preventDefault={cancel}>
            Cancel
        </button>
    </div>
</form>