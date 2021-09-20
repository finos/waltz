<script>
    export let obj;
    export let layout;

    function onMouseEnter() {
        console.log("decision:me:", obj);
    }

    let points = "";
    $: {
        const w = layout.width;
        const h = layout.height;
        points = `
            ${w/2},0
            0,${h/2}
            ${w/2},${h}
            ${w},${h/2}
            ${w/2},0
        `;
    }

</script>


<g on:mouseenter={onMouseEnter}>

    <polyline {points}></polyline>

    <foreignObject width={layout.width * 2}
                   height="200"
                   transform={`translate(${layout.width * -0.7}, ${layout.height * 1})`}>
        <div style="text-align: left; font-size: smaller">
            {obj.name}
        </div>
    </foreignObject>
</g>


<style>
    polyline {
        stroke: #999;
        fill: #eee;
    }
</style>