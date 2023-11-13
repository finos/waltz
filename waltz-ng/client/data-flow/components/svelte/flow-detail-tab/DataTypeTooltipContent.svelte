<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {flowClassificationStore} from "../../../../svelte-stores/flow-classification-store";
    import _ from "lodash";

    export let decorators = [];

    let flowClassificationCall = flowClassificationStore.findAll();

    $: flowClassifications = $flowClassificationCall?.data;
    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);

</script>

<h4>Data Types</h4>
<ul class="list-unstyled">
    {#each _.orderBy(decorators, d => d.decoratorEntity.name) as type}
        <li style="padding-bottom: 2px">
            <div class="rating-icon"
                 style={`background-color: ${flowClassificationsByCode[type.rating]?.color}`}>
            </div>
            <EntityLink ref={type.decoratorEntity}
                        showIcon={false}>
            </EntityLink>
        </li>
    {/each}
</ul>

<style>

    .rating-icon {
        display: inline-block;
        height: 1em;
        width: 1em;
        border:1px solid #ccc;
        border-radius: 2px;
    }

</style>