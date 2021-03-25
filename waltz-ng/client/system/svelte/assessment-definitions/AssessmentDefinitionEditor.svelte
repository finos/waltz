<script>
    import _ from "lodash";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let doCancel;
    export let doSave;
    export let definition;

    function getRequiredFields(d) {
        return [d.name, d.entityKind, d.description];
    }

    const possibleEntityKinds = [
        {value: "APPLICATION", name: "Application"},
        {value: "CHANGE_INITIATIVE", name: "Change Initiative"},
        {value: "LOGICAL_DATA_FLOW", name: "Logical Data Flow"},
        {value: "PHYSICAL_FLOW", name: "Physical Flow"},
        {value: "PHYSICAL_SPECIFICATION", name: "Physical Specification"},
        {value: "ENTITY_RELATIONSHIP", name: "Entity Relationship"},
        {value: "LICENCE", name: "Software Licence"},
        {value: "SOFTWARE_PACKAGE", name: "Software Package"},
        {value: "CHANGE_SET", name: "Change Set"},
    ];

    const possibleVisibility = [
        {value: "PRIMARY", name: "Primary"},
        {value: "SECONDARY", name: "Secondary"}
    ];

    const ratingSchemes = ratingSchemeStore.loadAll();

    let workingCopy = _.cloneDeep(definition);
    let savePromise = null;

    $: possibleRatingSchemes = _.sortBy($ratingSchemes.data, d => d.name);

    $: invalid = _.some(getRequiredFields(workingCopy), v => _.isEmpty(v));

    function save() {
        savePromise = doSave(workingCopy);
    }
</script>


<form autocomplete="off"
      on:submit|preventDefault={save}>

    <div class="row">
        <div class="col-md-12">
            <h3>{definition.name || "NEW"}</h3>
        </div>
    </div>

    <div class="row">
        <div class="form-group">
            <div class="col-md-8">
                <!-- NAME -->
                <label for="name">
                    Name
                    <small class="text-muted">(required)</small>
                </label>
                <input class="form-control"
                       id="name"
                       required="required"
                       placeholder="Name of assessment"
                       bind:value={workingCopy.name}>
                <div class="help-block">
                    Short name which describes this assessment
                </div>

                <!-- RATING SCHEME -->
                <label for="ratingScheme">
                    Rating Scheme
                    <small class="text-muted">(required)</small>
                </label>
                <select id="ratingScheme"
                        bind:value={workingCopy.ratingSchemeId}>
                    {#each possibleRatingSchemes as r}
                        <option value={r.id}>
                            {r.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    The rating scheme determines the possible values this assessment can have.
                    Changing ratings on an existing assessment should be treated with <i>extreme</i> caution
                    as existing mappings will become invalid.
                </div>

                <!-- ENTITY KIND -->
                <label for="entityKind">
                    Entity Kind
                    <small class="text-muted">(required)</small>
                </label>
                <select id="entityKind"
                        bind:value={workingCopy.entityKind}>
                    {#each possibleEntityKinds as k}
                        <option value={k.value}>
                            {k.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    Determines which classes of entity this assessment is applicable for
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


            <div class="col-md-4">
                <!-- EXT_ID -->
                <label for="externalId">
                    External Id
                    <small class="text-muted">(recommended)</small>
                </label>
                <input class="form-control"
                       id="externalId"
                       placeholder="External identifier"
                       bind:value={workingCopy.externalId}>
                <div class="help-block">
                    External identifiers help with data import/export as they <i>should not</i> change if the display name is updated
                </div>

                <!--VISIBILITY-->
                <label for="visibility">
                    Assessment Visibility
                    <small class="text-muted">(required)</small>
                </label>
                <select id="visibility"
                        bind:value={workingCopy.visibility}>
                    {#each possibleVisibility as r}
                        <option value={r.value}>
                            {r.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    The visibility setting determines if the assessment is shown by default to all users.
                    Please note that users are free to override these defaults and choose their own primary and secondary assessments.
                </div>

                <!-- READ ONLY -->
                <label for="isReadOnly">
                    Is Read Only ?
                </label>
                <input type=checkbox
                       id="isReadOnly"
                       bind:checked={workingCopy.isReadOnly}>
                <span class="text-muted">
                    {#if workingCopy.isReadOnly}
                        Yes, assessments are locked
                        <Icon name="lock"/>
                    {:else}
                        No, assessments can be edited
                        <Icon name="unlock"/>
                    {/if}
                </span>
                <div class="help-block">
                    Determines if <i>anyone</i> can edit this assessment
                </div>

                <!-- PERMITTED ROLE -->
                <label for="permittedRole">
                    Permitted Role
                </label>
                <input type=text
                       id="permittedRole"
                       bind:value={workingCopy.permittedRole}>
                <div class="help-block">
                    If provided, restricts editing to users which have been assigned the role
                </div>
            </div>
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


<style>
    label {
        display: block;
    }
    input:invalid {
        border: 2px solid red;
    }

    textarea:invalid {
        border: 2px solid red;
    }
</style>