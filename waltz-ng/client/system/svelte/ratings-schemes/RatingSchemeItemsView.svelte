<script>
    import Icon from "../../../common/svelte/Icon.svelte";

    import RatingSchemeItemEditor from "./RatingSchemeItemEditor.svelte";

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

    function save() {
        console.log("Not yet done!")
    }

</script>


<h3>Ratings</h3>

{#if activeMode === Modes.EDIT}
   <RatingSchemeItemEditor item={activeItem}/>
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