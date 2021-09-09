<script>
    import {platformStrategy, afcGovernance, situationalAppraisal} from "./demo-data";
    import {calcViewBox, mkLayoutData} from "./process-diagram-utils";
    import Defs from "./Defs.svelte";
    import Objects from "./Objects.svelte";
    import Connections from "./Connections.svelte";

    const process = platformStrategy;

    let viewBox = calcViewBox(process.objects);

    const layoutDataById = mkLayoutData(process.objects);

    console.log(layoutDataById);


    const connections = _.filter(
        process.connections,
        conn => layoutDataById[conn.startObjectId] && layoutDataById[conn.endObjectId]); // ditch conns w/o objs


</script>

<svg width="100%"
     height="700"
     preserveAspectRatio="xMinYMin"
     {viewBox}>

    <Defs/>
    <Objects objects={process.objects} {layoutDataById}/>
    <Connections {connections} {layoutDataById}/>

</svg>

<style>
    svg {
        border: 1px solid red;
    }
</style>
