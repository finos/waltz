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


    export let item;
    export let onCancel;

    let workingItem = Object.assign({}, item);

    function save() {
        console.log("Not implemented yet");
    }
</script>
<h4>
    <div class="rating-square"
         style="background-color: {workingItem.color}"/>
    Edit {workingItem.name}
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
               placeholder="Name of scheme"
               bind:value={workingItem.name}>
        <div class="help-block">
            Short name which describes this rating scheme
        </div>

        <label for="color-picker">Color</label>
        <div id="color-picker">
            <ColorPicker startColor={item.color}
                         on:select={c => workingItem.color = c}
                         predefinedColors={[greyHex, lightGreyHex, redHex, pinkHex, yellowHex, goldHex, amberHex, greenHex, blueHex, purpleHex]}/>
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
                  rows="12"
                  style="width: 100%"
                  required="required"
                  bind:value={workingItem.description}/>
        <div class="help-block">
            HTML or markdown code, any paths should be absolute
        </div>

    </div>
</form>

<button class="btn-link"
        on:click={onCancel}>
    Cancel
</button>