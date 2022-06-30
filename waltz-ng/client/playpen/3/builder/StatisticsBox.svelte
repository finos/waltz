<script>
    export let width;
    export let height;
</script>


<foreignObject {width}
               {height}>
    <div class="statistics-box"></div>
</foreignObject>


<style>
    .statistics-box {
        background-color: #f2f6f2;
        overflow-y: auto;
        overflow-x: hidden;
        box-sizing: border-box;
        height: 100%
    }
</style>