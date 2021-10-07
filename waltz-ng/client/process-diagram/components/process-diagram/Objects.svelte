<script>
    import {appsByDiagramMeasurableId, layoutDataById, objects, selectedObject} from "./diagram-store";
    import {findAssociatedApps, toComp} from "./process-diagram-utils";
    import _ from "lodash";

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

    function getAppCount(obj){
        const associatedApps = findAssociatedApps($appsByDiagramMeasurableId, obj);
        return associatedApps.length;
    }

</script>

{#each objs as d}
    <g transform={d.transform}
       class={`object ${d.obj.stereotype}`}
       on:click={() => $selectedObject = d.obj}>
        <svelte:component obj={d.obj}
                          layout={d.layout}
                          this={d.comp}
                          appCount={getAppCount(d.obj)}
                          isSelected={d.obj === $selectedObject}/>
    </g>
{/each}