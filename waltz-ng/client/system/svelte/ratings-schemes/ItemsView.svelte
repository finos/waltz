<script>
    import Icon from "../../../common/svelte/Icon.svelte";

    import RatingSchemeItemEditor from "./ItemEditor.svelte";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {blueHex} from "../../../common/colors";

    export let doSave;
    export let doCancel;
    export let ratings;
    export let scheme;

    const Modes = {
        LIST: "list",
        EDIT: "edit",
        DELETE: "delete"
    };

    let activeMode = Modes.LIST;
    let activeItem = null;


    function mkNew() {
        activeItem = {
            position: 0,
            ratingSchemeId: scheme.id,
            color: blueHex,
            userSelectable: true
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


    function onSave(item) {
        doSave(item)
            .then(() => {
                activeItem = null;
                activeMode = Modes.LIST;
            });
    }


    $: console.log({ratings})

</script>


<h3>Ratings <small>{scheme.name}</small></h3>

{#if activeMode === Modes.EDIT}
   <RatingSchemeItemEditor item={activeItem}
                           doSave={onSave}
                           doCancel={onCancel} />
{/if}

{#if activeMode === Modes.LIST}
    <table class="table table-striped table-hover table-condensed">
        <thead>
        <tr>
            <th width="25%">Rating</th>
            <th width="25%">Color</th>
            <th width="25%">Description</th>
            <th width="25%">Operations</th>
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
        <tfoot>
            <tr>
                <td colspan="4">
                    <button class="btn-link"
                            on:click={mkNew}>
                        <Icon name="plus"/>
                        Add new rating scheme item
                    </button>
                </td>
            </tr>
        </tfoot>
    </table>

    <button class="btn btn-link"
            on:click={doCancel}>
        Cancel
    </button>
{/if}


<style>
    .rating-square {
        display: inline-block;
        width: 1em;
        height: 1em;
    }
</style>