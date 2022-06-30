<script>

    import {createEventDispatcher, getContext} from "svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";

    const filterParameters = getContext("filterParameters");
    const selectedFilter = getContext("selectedFilter");

    const dispatch = createEventDispatcher();

    function addFilter() {
        $selectedFilter = null;
        editFilter();
    }

    function editFilter() {
        dispatch("edit");
    }

    function selectFilter(filter) {
        $selectedFilter = filter;
        editFilter();
    }

    function deleteFilter(filter) {
        $filterParameters = _.without($filterParameters, filter);
        toasts.info(`Removed ${filter.assessmentDefinition.name} filter`);
    }

</script>


<table class="table table-condensed table-hover">
    <thead>
    <tr>
        <th>Definition</th>
        <th>Ratings</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    {#each $filterParameters as filter}
        <tr>
            <td>{filter.assessmentDefinition.name}</td>
            <td>
                <ul>
                    {#each filter.ratingSchemeItems as rating}
                        <li>
                            <RatingIndicatorCell {...rating}/>
                        </li>
                    {/each}
                </ul>
            </td>
            <td>
                <button class="btn btn-skinny"
                        on:click={() => selectFilter(filter)}>
                    <Icon name="pencil"/>
                </button>
                <button class="btn btn-skinny"
                        on:click={() => deleteFilter(filter)}>
                    <Icon name="trash"/>
                </button>
            </td>
        </tr>
    {:else}
        <tr>
            <td colspan="3">
                <i>No filters applied</i>
            </td>
        </tr>
    {/each}
    </tbody>
    <tbody>
    <tr on:click={() => addFilter()}>
        <td colspan="3">
            <button class="btn btn-skinny">
                Add filter
                <Icon name="plus"/>
            </button>
        </td>
    </tr>
    </tbody>
</table>

<style>

    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
        font-size: 12px
    }

    li {
        padding-top: 0.1em;
    }

</style>