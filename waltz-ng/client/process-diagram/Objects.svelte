<script>
    import {toComp} from "./process-diagram-utils";
    export let objects = [];

</script>
{#each objects as obj}
    <g transform={`translate(${obj.topLeft.x} ${obj.topLeft.y})`}
       class={`object ${obj.stereotype}`}>
        <svelte:component {obj}
                          this={toComp(obj)}/>
    </g>
{/each}
