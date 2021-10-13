<script>
    export let width = 10;
    export let height= 10;

    $: minX = width / 4;
    $: minY = height / 4;

    $: maxX = width * 3 / 4;
    $: maxY = height * 3 / 4;

</script>



<g transform="translate(0, {height / 2})">
    <line x1={minX} y1={minY} x2={maxX} y2={maxY}></line>
    <line x1={minX} y1={maxY} x2={maxX} y2={minY}></line>
</g>


<style>

    line {
        stroke: #999;
        stroke-width: 3;
    }

</style>