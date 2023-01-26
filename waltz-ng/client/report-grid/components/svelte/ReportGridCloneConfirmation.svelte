<script>

    import _ from "lodash";

    export let grid;
    export let doClone;
    export let doCancel;

    const workingCopy = {
        name: null,
        description: null,
    }

    $: disabled = _.isNil(workingCopy.name) || _.isEmpty(workingCopy.name)

</script>


<h4>Cloning report grid: {grid.definition.name}</h4>
<ul>
    <li>Please provide a name for the new grid</li>
    <li>All columns will be copied to the newly created grid</li>
    <li>You will be an owner of the new grid, able to edit columns and members</li>
</ul>
<br>
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

<button class="btn btn-success btn-sm"
        title={disabled ? "You must set a name for your grid before cloning" : null}
        disabled={disabled}
        on:click={() => doClone(grid.definition.id, workingCopy)}>
    Clone
</button>
<button class="btn btn-primary btn btn-sm"
        on:click={doCancel}>
    Cancel
</button>