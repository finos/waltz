<script>
    import _ from "lodash";

    import Icon from "../../../common/svelte/Icon.svelte";


    export let doCancel;
    export let scheme;

    function getRequiredFields(d) {
        return [d.name, d.description];
    }

    let workingCopy = _.pick(scheme, ["id", "name", "description"]);
    let savePromise = null;

    $: invalid = _.some(getRequiredFields(workingCopy), v => _.isEmpty(v));

    function save() {
        console.log("Save", scheme);
    }
</script>


<div class="row">
    <div class="col-md-6">

        <form autocomplete="off"
              on:submit|preventDefault={save}>
            <h3>{scheme.name || "NEW"}</h3>

            <!-- NAME -->
            <label for="name">
                Name
                <small class="text-muted">(required)</small>
            </label>
            <input class="form-control"
                   id="name"
                   required="required"
                   placeholder="Name of scheme"
                   bind:value={workingCopy.name}>
            <div class="help-block">
                Short name which describes this rating scheme
            </div>

            <!-- DESCRIPTION -->
            <label for="description">
                Description
                <small class="text-muted">(required)</small>
            </label>
            <textarea id="description"
                      class="form-control"
                      rows="12"
                      style="width: 100%"
                      required="required"
                      bind:value={workingCopy.description}/>
            <div class="help-block">
                HTML or markdown code, any paths should be absolute
            </div>


            <button type="submit"
                    class="btn btn-success"
                    disabled={invalid || savePromise}>
                Save
            </button>

            <button class="btn btn-link"
                    on:click={doCancel}>
                Cancel
            </button>

            {#if savePromise}
                {#await savePromise}
                    Saving...
                {:then r}
                    Saved!
                {:catch e}
            <span class="alert alert-warning">
                Failed to save assessment definition. Reason: {e.error}
                <button class="btn-link"
                        on:click={() => savePromise = null}>
                    <Icon name="check"/>
                    Okay
                </button>
            </span>
                {/await}
            {/if}
        </form>
    </div>
    <div class="col-md-6">
        <h3>Ratings</h3>
        <table class="table table-condensed">
            <thead>
            <tr>
                <th width="20%">Rating</th>
                <th width="15%">Color</th>
                <th width="35%">Description</th>
                <th width="30%">Operations</th>
            </tr>
            </thead>
            <tbody>
            {#each scheme.ratings as rating}
                <tr>
                    <td>{rating.name}</td>
                    <td>
                        <div class="rating-square"
                             style="background-color: {rating.color}" />
                        {rating.color}
                    </td>
                    <td>
                        {rating.description}
                    </td>
                    <td>
                        <button class="btn-link"
                                aria-label="Edit rating {rating.name}">
                            <Icon name="edit"/>
                            Edit
                        </button>
                        |
                        <button class="btn-link"
                                aria-label="Remove rating {rating.name}">
                            <Icon name="trash"/>
                            Remove
                        </button>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</div>


<style>
    .rating-square {
        display: inline-block;
        width: 1em;
        height: 1em;
    }
</style>