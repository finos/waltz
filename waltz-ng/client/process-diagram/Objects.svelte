<script>
    import {toComp} from "./process-diagram-utils";
    export let objects = [];
    export let layoutDataById = {};

</script>
{#each objects as obj}
    <g transform={`translate(${layoutDataById[obj.objectId].x} ${layoutDataById[obj.objectId].y})`}
       class={`object ${obj.stereotype}`}>
        <svelte:component {obj}
                          layoutData={layoutDataById[obj.objectId]}
                          this={toComp(obj)}/>
    </g>
{/each}
