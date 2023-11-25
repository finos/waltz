<script>

    import {Directions} from "../flow-detail-utils";
    import {mkDirectionFilter, mkDirectionFilterId} from "./filter-utils";
    import _ from "lodash";
    import {filters, updateFilters} from "../flow-details-store";

    function selectDirection(direction) {
        const filterId = mkDirectionFilterId();
        const newFilter = mkDirectionFilter(filterId, direction);
        updateFilters(filterId, newFilter);
    }

    $: directionFilter = _.find($filters, d => d.id === mkDirectionFilterId());


</script>

<div class="help-block"
 style="padding-top: 1em;">
Use the buttons to filter on the direction of the flows.
</div>
<div style="display: flex; padding-top: 1em;  padding-bottom: 1em">
    <button type="button"
            class:active={_.isEmpty(directionFilter) || _.isEqual(directionFilter.direction, Directions.ALL)}
            class="btn btn-default"
            on:click={() => selectDirection(Directions.ALL)}>
        All
    </button>
    <button type="button"
            class="btn btn-default"
            class:active={!_.isEmpty(directionFilter) && _.isEqual(directionFilter.direction, Directions.INBOUND)}
            on:click={() => selectDirection(Directions.INBOUND)}>
        Consumes
    </button>
    <button type="button"
            class="btn btn-default"
            class:active={!_.isEmpty(directionFilter) && _.isEqual(directionFilter.direction, Directions.OUTBOUND)}
            on:click={() => selectDirection(Directions.OUTBOUND)}>
        Produces
    </button>
</div>

<style>


</style>