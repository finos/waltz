<script>

    export let grid;
    export let doCancel = () => console.log("Cancel");
    export let doSave = () => console.log("Saving");

    let workingCopy = Object.assign({}, grid);

    function noChange(workingCopy) {
        return workingCopy.name === grid.name && workingCopy.description === grid.description
    }

</script>


<form autocomplete="off">

    <div class="form-group">
        <label for="title">Title</label>
        <input class="form-control"
               id="title"
               placeholder="Grid Name"
               bind:value={workingCopy.name}>
    </div>

    <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control"
                  id="description"
                  bind:value={workingCopy.description}/>
    </div>

    <button type="submit"
            class="btn btn-success"
            disabled={noChange(workingCopy)}
            on:click|preventDefault={() => doSave(workingCopy)}>
        Save
    </button>

    <button class="btn btn-link"
            on:click={doCancel}>
        Cancel
    </button>
</form>