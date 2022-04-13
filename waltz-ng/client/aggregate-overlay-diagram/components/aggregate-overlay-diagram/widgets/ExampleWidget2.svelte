<script>

    import {scaleLinear} from "d3-scale";

    export let cellData;

    const rScale = scaleLinear()
        .domain([0, 200])
        .range([0, 100]);

    global.rScale = rScale;

</script>

<h1>Hello Mum</h1>

<svg class="widget" viewBox="0 0 270 90">
   <foreignObject width="270" height="90">
       <h1>Hello Mum</h1>
       App Count: {cellData.numApps}
   </foreignObject>
</svg>
