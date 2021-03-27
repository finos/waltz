<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import ColorPicker from "./ColorPicker.svelte";
    import {greyHex, lightGreyHex, yellowHex, goldHex, purpleHex, pinkHex, redHex, amberHex, greenHex, blueHex} from "../../../common/colors";

    export let ratings = [];
    export let doSave = (s) => console.log("RSI: doSave", s)

    const Modes = {
        LIST: "list",
        EDIT: "edit",
        DELETE: "delete"
    };

    let activeMode = Modes.LIST;
    let activeItem = null;

    function mkNew() {
        activeItem = {
            name: "NEW"
        };
        activeMode = Modes.EDIT;
    }

    function onEdit(item) {
        activeItem = Object.assign({}, item);
        activeMode = Modes.EDIT;
    }

    function onCancel() {
        activeItem = null;
        activeMode = Modes.LIST;
    }

</script>


<h3>Ratings</h3>

{#if activeMode === Modes.EDIT}
    <h4>
        <div class="rating-square"
             style="background-color: {activeItem.color}"/>
        Edit {activeItem.name}
    </h4>

    <ColorPicker startColor={activeItem.color}
                 predefinedColors={[greyHex, lightGreyHex, redHex, pinkHex, yellowHex, goldHex, amberHex, greenHex, blueHex, purpleHex]}/>

    <button class="btn-link"
            on:click={onCancel}>
        Cancel
    </button>
{/if}

{#if activeMode === Modes.LIST}
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
        {#each ratings as rating}
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
                            on:click={() => onEdit(rating)}
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
        {:else}
            <tr>
                <td colspan="4">No ratings yet</td>
            </tr>
        {/each}
        </tbody>
    </table>
    <button class="btn-link"
            on:click={mkNew}>
        <Icon name="plus"/>
        Add new rating scheme item
    </button>
{/if}

<style>
    .rating-square {
        display: inline-block;
        width: 1em;
        height: 1em;
    }
</style>