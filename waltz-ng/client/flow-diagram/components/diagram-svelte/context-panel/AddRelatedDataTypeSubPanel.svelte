<script>
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {createEventDispatcher} from "svelte";


    let dispatch = createEventDispatcher();

    function selectEntity(e) {
        dispatch("select", e.detail)
    }

    function cancel() {
        dispatch("cancel");
    }

</script>

<div class="form-group">
    <label for="datatype">Datatype:</label>
    <div id="datatype">
        <EntitySearchSelector on:select={selectEntity}
                              placeholder="Search for datatype"
                              entityKinds={['DATA_TYPE']}>
        </EntitySearchSelector>
    </div>
    <p class="text-muted">Start typing to select the datatype</p>
</div>

<div class="context-panel-footer">
    <button class="btn btn-skinny"
            on:click={cancel}>
        Cancel
    </button>
</div>

<style>
    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>