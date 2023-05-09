<script>
    import _ from "lodash";

    import Icon from "../../../common/svelte/Icon.svelte";

    export let scheme;
    export let doCancel;
    export let doSaveScheme;

    function getRequiredFields(d) {
        return [d.name, d.description];
    }

    let workingCopy = _.pick(scheme, ["id", "name", "externalId", "description"]);
    let savePromise = null;

    $: invalid = _.some(getRequiredFields(workingCopy), v => _.isEmpty(v));

    function saveScheme() {
        savePromise = doSaveScheme(workingCopy);
    }

</script>


<div class="row">
    <div class="col-sm-12">
        <form autocomplete="off"
              on:submit|preventDefault={saveScheme}>
            <h3>Scheme <small>{scheme?.name || "NEW"}</small></h3>

            <div class="form-group">
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

                <label for="external-id">
                    External ID
                </label>
                <input class="form-control"
                       id="external-id"
                       placeholder="External ID"
                       bind:value={workingCopy.externalId}>
                <div class="help-block">
                    Optional external ID, typically used for integration with other systems.
                    Be careful when changing this value, as it may break integrations.
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
                        Failed to save scheme. Reason: {e.error}
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
</div>

<style>
</style>

