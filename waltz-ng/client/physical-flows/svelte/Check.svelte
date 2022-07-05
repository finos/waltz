<script>
    import Icon from "../../common/svelte/Icon.svelte";

    export let selected = false;
</script>


<span class={selected ? "selected" : "unselected"}>
    <Icon name={selected ? "check-square-o" : "square-o"}
          fixedWidth="true"/>
</span>

<style type="text/scss">
    @import "../../../style/variables";

    .selected {
        color: $waltz-green;
    }

    .unselected {
        color: $waltz-amber;
    }
</style>