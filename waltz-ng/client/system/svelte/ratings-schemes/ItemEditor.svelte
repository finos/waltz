<script>
    import ColorPicker from "./ColorPicker.svelte";
    import {
        greyHex,
        lightGreyHex,
        yellowHex,
        goldHex,
        purpleHex,
        pinkHex,
        redHex,
        amberHex,
        greenHex,
        blueHex
    } from "../../../common/colors";
    import Icon from "../../../common/svelte/Icon.svelte";


    export let item;
    export let doCancel;
    export let doSave;

    const predefinedColors = [
        greyHex,
        lightGreyHex,
        greenHex,
        blueHex,
        purpleHex,
        redHex,
        pinkHex,
        goldHex,
        amberHex,
        yellowHex
    ];

    let workingCopy = Object.assign({}, item);
    let savePromise = null;

    function save() {
        savePromise = doSave(workingCopy);
    }

    function onSelectColor(evt) {
        workingCopy.color = evt.detail;
    }
</script>


<h4>
    {item.name || "NEW"} <small>Edit</small>
</h4>

<form autocomplete="off"
      on:submit|preventDefault={save}>

    <div class="form-group">

        <!-- NAME -->
        <label for="name">
            Name
            <small class="text-muted">(required)</small>
        </label>
        <input class="form-control"
               id="name"
               required="required"
               placeholder="Name of rating item"
               bind:value={workingCopy.name}>
        <div class="help-block">
            Short name which describes this rating scheme item
        </div>

        <!-- GROUP? -->
        <label for="group">
            Group
            <small class="text-muted">(optional)</small>
        </label>
        <input class="form-control"
               id="group"
               placeholder="Optional grouping of rating item"
               bind:value={workingCopy.ratingGroup}>
        <div class="help-block">
            Optional group name to categorize this rating scheme item
        </div>


        <!-- CODE -->
        <label for="code">
            Rating Code
            <small class="text-muted">(required)</small>
        </label>
        <input class="form-control"
               id="code"
               style="width: 3em"
               required="required"
               disabled={workingCopy.id}
               maxlength="3"
               placeholder=""
               bind:value={workingCopy.rating}>
        <div class="help-block">
            Short code (maximum of 3 chars) to represent this rating scheme item
            {#if workingCopy.id}
                <br>
                <Icon name="warning"/>
                The rating code for this item cannot be changed once set.
            {/if}
        </div>


        <!-- POSITION -->
        <label for="position">
            Position
            <small class="text-muted">(required)</small>
        </label>
        <input class="form-control"
               type="number"
               id="position"
               style="width: 6em"
               required="required"
               maxlength="1"
               placeholder=""
               bind:value={workingCopy.position}>
        <div class="help-block">
            Position, used for ordering items.
            Lower numbers go first, name is used as a tie breaker.
        </div>


        <!-- USER SELECTABLE -->
        <label for="userSelectable">
            User selectable ?
        </label>
        <input type=checkbox
               id="userSelectable"
               bind:checked={workingCopy.userSelectable}>
        <span class="text-muted">
            {#if workingCopy.userSelectable}
                Yes, users can select this rating
                <Icon name="lock"/>
            {:else}
                No, users cannot select this rating
                <Icon name="unlock"/>
            {/if}
        </span>
        <div class="help-block">
            Determines if <i>anyone</i> can select this rating (set to false if it's not meant for end user usage)
        </div>


        <!-- REQUIRES COMMENT -->
        <label for="requiresComment">
            Requires Comment ?
        </label>
        <input type=checkbox
               id="requiresComment"
               bind:checked={workingCopy.requiresComment}>
        <span class="text-muted">
            {#if workingCopy.requiresComment}
                Yes, user must supply a comment when selecting this item
                <Icon name="lock"/>
            {:else}
                No, users may omit providing a comment when selecting this item
                <Icon name="unlock"/>
            {/if}
        </span>
        <div class="help-block">
            Determines if the user <i>must</i> supply a comment when selecting this item.
        </div>


        <!-- COLOR -->
        <label for="color-picker">Color</label>
        <div id="color-picker">
            <ColorPicker startColor={item.color}
                         on:select={onSelectColor}
                         {predefinedColors}/>
        </div>
        <div class="help-block">
            Select from a predefined color or use the custom button to pick out a custom color.
        </div>


        <!-- DESCRIPTION -->
        <label for="description">
            Description
            <small class="text-muted">(required)</small>
        </label>
        <textarea id="description"
                  class="form-control"
                  rows="6"
                  style="width: 100%"
                  required="required"
                  bind:value={workingCopy.description}/>
        <div class="help-block">
            HTML or markdown code, any paths should be absolute
        </div>

        <!-- EXTERNAL ID -->
        <label for="external-id">
            External Identifier
        </label>
        <input class="form-control"
               id="external-id"
               placeholder="External identifier of rating item"
               bind:value={workingCopy.externalId}>
        <div class="help-block">
            Short name used to identify this rating in an external system
        </div>


    </div>


    <button class="btn btn-success"
            type="submit">
        Save
    </button>
    <button class="btn-link"
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
                Failed to save rating item. Reason: {e.error}
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
</style>