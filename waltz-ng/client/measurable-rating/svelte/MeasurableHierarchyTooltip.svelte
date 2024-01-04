<script>

    import _ from "lodash";
    import MeasurableHierarchyNode from "./MeasurableHierarchyNode.svelte";

    export let measurableHierarchy;

    $: measurablesByLevel  = _.keyBy(measurableHierarchy, d => d.level);

</script>

<div>
   <MeasurableHierarchyNode {measurablesByLevel}/>
</div>

