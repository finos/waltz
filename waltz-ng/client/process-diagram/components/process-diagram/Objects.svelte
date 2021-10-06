<script>
    import {appCountsByDiagramMeasurableId, layoutDataById, objects} from "./diagram-store";
    import {toComp} from "./process-diagram-utils";
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

    $: aligns = $appCountsByDiagramMeasurableId;

    $: console.log({aligns, objs});

    function getAppCount(obj){
        return _.get($appCountsByDiagramMeasurableId, [obj.waltzReference?.id], 0);
    }

</script>

{#each objs as d}
    <g transform={d.transform}
       class={`object ${d.obj.stereotype}`}>
        <svelte:component obj={d.obj}
                          layout={d.layout}
                          this={d.comp}
                          appCount={getAppCount(d.obj)}/>
    </g>
{/each}
