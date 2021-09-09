<script>
    import {platformStrategy, afcGovernance, situationalAppraisal} from "./demo-data";
    import {calcRectAttrs, calcViewBox, toComp} from "./process-diagram-utils";
    import Defs from "./Defs.svelte";
    import Objects from "./Objects.svelte";
    import Connections from "./Connections.svelte";

    const process = platformStrategy;

    let viewBox = calcViewBox(process.objects);

    const objects = _
        .chain(process.objects)
        .map(d => Object.assign({}, d, calcRectAttrs(d)))
        .value();

    const objectsById = _.keyBy(objects, d => d.objectId);

    const connections = _
        .chain(process.connections)
        .map(conn => {
            const connObjects = {
                startObject: objectsById[conn.startObjectId],
                endObject: objectsById[conn.endObjectId]
            };
            return Object.assign(
                {},
                conn,
                connObjects);
        })
        .filter(conn => conn.startObject && conn.endObject)
        .value();

</script>

<svg width="100%"
     height="700"
     preserveAspectRatio="xMinYMin"
     {viewBox}>

    <Defs/>
    <Objects {objects}/>
    <Connections {connections}/>

</svg>

<style>
    svg {
        border: 1px solid red;
    }
</style>
