<script>
    import _ from "lodash";

    export let doCancel;
    export let doSave;
    export let bookmark;

    let workingCopy;

    $: workingCopy = _.cloneDeep(bookmark);
    $: console.log({bookmark, workingCopy})

</script>

<h4>Bookmark Edit</h4>

<form>
    <div class="form-group">
        <label for="title">Title</label>
        <input class="form-control"
               id="title"
               placeholder="Link Title"
               bind:value={workingCopy.title}>
    </div>
    <div class="form-group">
        <label for="url">URL</label>
        <input class="form-control"
               id="url"
               placeholder="URL"
               bind:value={workingCopy.url}>
    </div>
    <div class="checkbox">
        <label>
            <input type="checkbox"
                   bind:checked={workingCopy.isRestricted}>
            Is Restricted ?
        </label>
    </div>
    <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control"
                  id="description"
                  bind:value={workingCopy.description}/>
    </div>
    <button type="submit"
            class="btn btn-success"
            on:click={() => doSave(workingCopy)}>
        Save
    </button>

    <button class="btn btn-link"
            on:click={doCancel}>
        Cancel
    </button>
</form>
