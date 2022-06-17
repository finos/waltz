<script>

    import {createEventDispatcher, getContext} from "svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    const filterParameters = getContext("filterParameters");
    const selectedFilter = getContext("selectedFilter");

    const dispatch = createEventDispatcher();

    function editFilter() {
        dispatch("edit");
    }

    function selectFilter(filter) {
        $selectedFilter = filter;
        editFilter();
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
            </td>
        </tr>
    {/each}
    <tr on:click={() => editFilter()}>
        <td colspan="3">
            <button class="btn btn-skinny">
                <Icon name="plus"/>
                Add another filter
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