<script>
    export let dataTypes = "";
</script>

<div class="fit-text" title={dataTypes}>
    { dataTypes }
</div>

<style>
    .fit-text {
        width: max-content;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
</style>