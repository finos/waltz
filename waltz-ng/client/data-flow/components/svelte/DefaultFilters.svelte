<script>

    import _ from "lodash";
    import {categoryQuery, clientQuery, entityKindFilter} from "./flow-decorator-store";
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    const dispatch = createEventDispatcher();

    let filteredKinds = [];

    function addOrRemoveKindFromFilter(kind) {
        filteredKinds = _.includes(filteredKinds, kind)
            ? _.without(filteredKinds, kind)
            : _.concat(filteredKinds, kind)
    }

    $: $entityKindFilter = c => !_.includes(filteredKinds, c.kind);

    function changeView() {
        dispatch('submit');
    }

</script>

<div class="row row-no-gutters">
    <div class="col-sm-12">
        <div class="help-block">
            <Icon name="info-circle"/> Use the selection below to filter counterpart entities by name or entity kind
        </div>
    </div>
</div>

<div class="row row-no-gutters">
    <div class="col-sm-5">
        Filter data types:
    </div>
    <div class="col-sm-7">
        <input class="form-control"
               id="category-filter"
               type="text"
               placeholder="Search by name..."
               bind:value={$categoryQuery}/>
    </div>
</div>

<br>

<div class="row row-no-gutters">
    <div class="col-sm-5">
            Filter applications and actors:
    </div>
    <div class="col-sm-7">
        <input class="form-control"
               id="client-filter"
               type="text"
               placeholder="Search by name..."
               bind:value={$clientQuery}/>
    </div>
</div>

<br>

<div class="row row-no-gutters">
    <div class="col-sm-5">
            Display applications:
    </div>
    <div class="col-sm-7">
        <input id="application-filter"
               type="checkbox"
               checked={!_.includes(filteredKinds, "APPLICATION")}
               on:click={() => addOrRemoveKindFromFilter("APPLICATION")}>
    </div>
</div>

<br>

<div class="row row-no-gutters">
    <div class="col-sm-5">
            Display actors:
    </div>
    <div class="col-sm-7">
        <input id="actor-filter"
               type="checkbox"
               checked={!_.includes(filteredKinds, "ACTOR")}
               on:click={() => addOrRemoveKindFromFilter("ACTOR")}>
    </div>
</div>

<br>

<div class="row row-no-gutters">
    <div class="col-sm-12">
        <div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
            <button class="btn btn-skinny"
                    on:click={changeView}>
                View assessment filters
            </button>
        </div>
    </div>
</div>