<script>
    export let obj;
    export let layoutData;

    function onMouseEnter() {
        console.log("decision:me:", obj);
    }

    let points = "";
    $: {
        const w = layoutData.width;
        const h = layoutData.height;
        points = `
            ${w/2},0
            0,${h/2}
            ${w/2},${h}
            ${w},${h/2}
            ${w/2},0
        `;
    };

</script>


<g on:mouseenter={onMouseEnter}>

    <polyline {points}></polyline>

    <foreignObject width={layoutData.width * 2}
                   height="200"
                   transform={`translate(${layoutData.width * -0.7}, ${layoutData.height * 1})`}>
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