<script>
    import Icon from "../../common/svelte/Icon.svelte";

    export let selected = false;
</script>


<span class:selected>
    <Icon name={selected ? "check-square-o" : "square-o"}/>
</span>

<style type="text/scss">
    @import "../../../style/variables";

    .selected {
        color: $waltz-green;
    }
</style>