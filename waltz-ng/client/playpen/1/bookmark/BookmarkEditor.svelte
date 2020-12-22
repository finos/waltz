<script>
    export let doCancel;
    export let doSave;
    export let bookmark;
    export let kinds;

    let workingCopy = Object.assign({}, bookmark);
</script>

<h4>Bookmark Edit</h4>

<form autocomplete="off">
    <div class="form-group">
        <label for="title">Title</label>
        <input class="form-control"
               id="title"
               placeholder="Link Title"
               bind:value={workingCopy.title}>
    </div>

    <div class="form-group">
        <label for="url">
            URL
            <small class="text-muted">(required)</small>
        </label>
        <input class="form-control"
               id="url"
               required="required"
               placeholder="URL"
               bind:value={workingCopy.url}>
    </div>

    <div class="checkbox">
        <label>
            <input type="checkbox"
                   bind:checked={workingCopy.isRestricted}>
            Is Restricted ?
        </label>
        <div class="help-block">
            Use this flag to indicate if visitors may need special permissions to access the link
        </div>
    </div>


    <div class="form-group">
        <label for="kind">
            Kind
        </label>
        <select id="kind"
                bind:value={workingCopy.bookmarkKind}
                title="Select your spell"
                class="form-control">
            {#each kinds as kind}
                <option value={kind.key}>{kind.name}</option>
            {/each}
        </select>
    </div>


    <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control"
                  id="description"
                  bind:value={workingCopy.description}/>
    </div>

    <button type="submit"
            class="btn btn-success"
            on:click|preventDefault={() => doSave(workingCopy)}>
        Save
    </button>

    <button class="btn btn-link"
            on:click={doCancel}>
        Cancel
    </button>
</form>

<style>
    input:invalid {
        border: 2px solid red;
    }
</style>
