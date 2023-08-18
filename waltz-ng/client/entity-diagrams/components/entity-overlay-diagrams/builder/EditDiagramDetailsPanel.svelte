<script>
    import {createEventDispatcher} from "svelte";
    import DropdownPicker
        from "../../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {diagramService} from "../entity-diagram-store";

    const dispatch = createEventDispatcher();

    const {selectedDiagram} = diagramService;

    let aggregateEntityKinds = [entity.APPLICATION, entity.CHANGE_INITIATIVE];

    let working = Object.assign({
        name: null,
        description: null,
        aggregatedEntityKind: entity.APPLICATION.key,
    },
    $selectedDiagram)

    function cancel() {
        dispatch("cancel");
    }

    function save() {
        dispatch("save", working);
    }

    function selectEntityKind(kind) {
        working.aggregatedEntityKind = kind.key;
    }

    $: selectedKind = _.find(aggregateEntityKinds, d => d.key === working.aggregatedEntityKind);

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

    <DropdownPicker items={aggregateEntityKinds}
                    onSelect={selectEntityKind}
                    defaultMessage="Select additional column options"
                    selectedItem={selectedKind}/>
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