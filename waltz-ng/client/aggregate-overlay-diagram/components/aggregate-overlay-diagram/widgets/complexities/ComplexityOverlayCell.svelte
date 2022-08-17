<script>
    import {ComplexityModes, complexityRenderMode} from "./store";
    import ComplexityTotalCell from "./ComplexityTotalCell.svelte";
    import ComplexityAverageCell from "./ComplexityAverageCell.svelte";

    export let cellData = [];
    export let maxTotalComplexity = 0;
    export let maxAverageComplexity = 0;
    export let renderMode;
    export let width;
    export let height;
    export let applicationsById = {};
    export let complexityKindsById = {};

    $: comp = $complexityRenderMode === ComplexityModes.TOTAL
        ? ComplexityTotalCell
        : ComplexityAverageCell;

</script>

<div class="content">
    <svelte:component this={comp}
                      {cellData}
                      {maxTotalComplexity}
                      {maxAverageComplexity}
                      {renderMode}
                      {width}
                      {height}
                      {applicationsById}
                      {complexityKindsById}/>
</div>
