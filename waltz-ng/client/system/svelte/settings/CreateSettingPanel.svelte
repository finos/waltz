<script>

    import _ from "lodash";
    import {createEventDispatcher} from "svelte";

    const dispatch = createEventDispatcher();

    let working = {
        name: null,
        description: null,
        restricted: false,
        value: null
    }

    function save() {
        dispatch("save", working);
        clear();
    }

    function clear(){
        working = {
            name: null,
            description: null,
            restricted: false,
            value: null
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
        Name of this setting
    </div>

    <textarea class="form-control"
              id="value"
              placeholder="Value"
              bind:value={working.value}/>
    <div class="help-block small">
        Value of this setting
    </div>

    <div>
        <input type="checkbox"
               bind:checked={working.restricted}>
        Restricted
    </div>
    <div class="help-block small">
        Can this setting be edited
    </div>

    <textarea class="form-control"
              id="description"
              placeholder="Description"
              bind:value={working.description}/>
    <div class="help-block small">
        Description of this setting
    </div>

    <div style="padding-top: 1em">
        <button class="btn btn-default"
                disabled={_.isEmpty(working.name)}
                on:click|preventDefault={save}>
            Save
        </button>
        <button class="btn btn-default"
                on:click|preventDefault={clear}>
            Clear
        </button>
    </div>
</form>