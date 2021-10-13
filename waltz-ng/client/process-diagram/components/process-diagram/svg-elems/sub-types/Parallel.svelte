<script>
    export let width = 10;
    export let height= 10;

    $: minX = width / 5;
    $: midX = width / 2;
    $: maxX = width * 4 / 5;

    $: minY = height / 5;
    $: midY = height / 2;
    $: maxY = height * 4 / 5;

</script>



<g transform="translate(0, {height / 2})">
    <line x1={midX} y1={minY} x2={midX} y2={maxY}></line>
    <line x1={minX} y1={midY} x2={maxX} y2={midY}></line>
</g>


<style>

    line {
        stroke: #999;
        stroke-width: 3;
    }

</style>