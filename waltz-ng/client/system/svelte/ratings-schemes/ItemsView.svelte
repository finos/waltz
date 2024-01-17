<script>
    import Icon from "../../../common/svelte/Icon.svelte";

    import RatingSchemeItemEditor from "./ItemEditor.svelte";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {blueHex} from "../../../common/colors";

    import ItemRemovalConfirmation from "./ItemRemovalConfirmation.svelte";

    export let doSave;
    export let doRemove;
    export let doCancel;
    export let ratings;
    export let scheme;

    const Modes = {
        LIST: "list",
        EDIT: "edit",
        DELETE: "delete"
    };

    const usageCall = ratingSchemeStore.calcRatingUsageStats();

    let activeMode = Modes.LIST;
    let activeItem = null;

    $: usageData = $usageCall.data;
    $: usageCountsByRatingId = _.reduce(
        usageData,
        (acc, d) => {
            acc[d.ratingId] = (acc[d.ratingId] || 0) + d.count;
            return acc;
        },
        {});


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


    function onDelete(item) {
        activeItem = Object.assign({}, item);
        activeMode = Modes.DELETE;
    }


    function onCancel() {
        activeItem = null;
        activeMode = Modes.LIST;
    }


    function onSave(item) {
        return doSave(item)
            .then(() => {
                activeItem = null;
                activeMode = Modes.LIST;
            });
    }

    function onRemove(itemId) {
        return doRemove(itemId)
            .then(() => {
                activeItem = null;
                activeMode = Modes.LIST;
            });
    }

</script>


<h3>Ratings <small>{scheme.name}</small></h3>

{#if activeMode === Modes.EDIT}
   <RatingSchemeItemEditor item={activeItem}
                           doSave={onSave}
                           doCancel={onCancel} />
{/if}

{#if activeMode === Modes.DELETE}
   <ItemRemovalConfirmation item={activeItem}
                            doRemove={onRemove}
                            doCancel={onCancel} />
{/if}

{#if activeMode === Modes.LIST}
    <table class="table table-striped table-hover table-condensed small">
        <thead>
        <tr>
            <th width="20%"><i>Group</i></th>
            <th width="25%">Rating</th>
            <th width="5%">Code</th>
            <th width="10%">Req. Comment</th>

            <th width="10%">Color</th>
            <th width="10%">Usages</th>
            <th width="20%">Operations</th>
        </tr>
        </thead>
        <tbody>
        {#each ratings as rating}
            <tr>
                <td>{rating.ratingGroup || '-'}</td>
                <td>{rating.name}</td>
                <td>{rating.rating}</td>
                <td>
                    {#if rating.requiresComment}
                        <Icon name="check"/>
                    {/if}
                </td>
                <td>
                    <div class="rating-square"
                         style="background-color: {rating.color}">
                    </div>
                    {rating.color}
                </td>
                <td>
                    {usageCountsByRatingId[rating.id] || "-"}
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
                            on:click={() => onDelete(rating)}
                            disabled={usageCountsByRatingId[rating.id] || 0 > 0}
                            aria-label="Remove rating {rating.name}">
                        <Icon name="trash"/>
                        Remove
                    </button>
                </td>
            </tr>
        {:else}
            <tr>
                <td colspan="7">No ratings yet</td>
            </tr>
        {/each}
        </tbody>
        <tfoot>
            <tr>
                <td colspan="7">
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

    button:disabled {
        color: #999
    }

    button:disabled:hover {
        cursor: not-allowed;
    }
</style>