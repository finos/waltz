<script>
    import {layoutDataById, objects} from "./diagram-store";
    import {toComp} from "./process-diagram-utils";

    $: objs = _.map(
        $objects,
        obj => {
            const layout = $layoutDataById[obj.objectId];
            return {
                obj,
                comp: toComp(obj),
                transform: `translate(${layout.x} ${layout.y})`,
                layout
            };
        });
</script>

{#each objs as d}
    <g transform={d.transform}
       class={`object ${d.obj.stereotype}`}>
        <svelte:component obj={d.obj}
                          layout={d.layout}
                          this={d.comp}/>
    </g>
{/each}
