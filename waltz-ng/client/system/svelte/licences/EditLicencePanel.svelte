<script>

    import _ from "lodash";
    import {createEventDispatcher, onMount} from "svelte";

    export let licence;
    export let showCancel = true;

    const dispatch = createEventDispatcher();

    onMount(() => {
        if (licence) {
            working = {
                id: licence.id,
                name: licence.name,
                description: licence.description,
                externalId: licence.externalId
            }
        }
    });

    let working = {
        name: null,
        description: null,
        externalId: null,
    }

    function save() {
        dispatch("save", working);
        clear();
    }

    function cancel() {
        dispatch("cancel");
        clear();
    }

    function clear(){
        if (licence) {
            working = {
                id: licence.id,
                name: licence.name,
                description: licence.description,
                externalId: licence.externalId
            }
        } else {
            working = {
                name: null,
                description: null,
                externalId: null,
            }
        }
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
    <div class="help-block small">
        Name of this licence
    </div>

    <textarea class="form-control"
              id="description"
              placeholder="Description"
              bind:value={working.description}/>
    <div class="help-block small">
        Description of this licence
    </div>

    <input class="form-control"
           id="externalId"
           maxlength="255"
           required="required"
           placeholder="External Id"
           bind:value={working.externalId}/>
    <div class="help-block small">
        External Identifier of this licence
    </div>

    <div style="padding-top: 1em">
        <button class="btn btn-default"
                title="saves any changes to licence info"
                disabled={_.isEmpty(working.name)}
                on:click|preventDefault={save}>
            Save
        </button>
        {#if showCancel}
            <button class="btn btn-default"
                    on:click|preventDefault={cancel}>
                Cancel
            </button>
        {/if}
        <button class="btn btn-default"
                title="clears any changes to licence info"
                on:click|preventDefault={clear}>
            Clear
        </button>
    </div>
</form>