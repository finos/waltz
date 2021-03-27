<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let predefinedColors = [];
    export let startColor;

    $: selectedColor = startColor;

    $: dispatch("select", selectedColor);
</script>


<div class="color-picker">

    <input type="color"
           id="custom"
           bind:value={selectedColor}>
    <label for="custom">Custom</label>

    <div class="predefined-colors">
        Predefined:
        {#each predefinedColors as predefinedColor }
            <button class="color-box"
                    on:click={() => selectedColor = predefinedColor}
                    style="background-color:{predefinedColor}">
            </button>
        {/each}

        Original:
        <button class="color-box"
                on:click={() => selectedColor = startColor}
                style="background-color:{startColor}">
        </button>
    </div>

</div>


<style>
    .color-picker {
        border: 1px solid #eee;
    }

    .predefined-colors {
        alignment: center;
        background-color: #fafafa;
        padding-top: 0.5em;
        padding-left: 0.5em;
    }

    input {
        margin-top: 1em;
        margin-left: 1em;
        box-sizing: content-box;
        border: 1px solid #ccc;
        border-radius: 2px;
    }

    .color-box {
        cursor: pointer;
        width: 1em;
        height: 1em;
        border: none;
        margin-left: 0.3em;
    }
</style>