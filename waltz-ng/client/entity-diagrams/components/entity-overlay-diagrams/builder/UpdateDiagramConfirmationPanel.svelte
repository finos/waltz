<script>
    import {createEventDispatcher} from "svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {diagramService} from "../entity-diagram-store";

    const dispatch = createEventDispatcher();

    const {selectedDiagram} = diagramService;

    function cancel() {
        dispatch("cancel");
    }

    function save() {
        dispatch("save");
    }

</script>

<div>
    <NoData type="warning">Are you sure you want to update the diagram layout and data for: {$selectedDiagram.name}</NoData>
</div>

<button class="btn btn-default"
        on:click|preventDefault={save}>
    Save
</button>
<button class="btn btn-default"
        on:click|preventDefault={cancel}>
    Cancel
</button>