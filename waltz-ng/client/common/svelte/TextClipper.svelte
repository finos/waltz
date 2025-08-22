<script>
    export let text = '';
</script>

<div class="fit-text" title={text}>
    { text }
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