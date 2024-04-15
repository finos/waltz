<script>
    import Icon from "../../../common/svelte/Icon.svelte";

    export let color;
    export let name;
    export let ratingGroup = null;
    export let description;
    export let showName = true;
    export let showGroup = false;

</script>

<div title={description}
     style="display: inline-block;">
    <div style="display: inline-block"
         class="stack-box fa-stack">
        <span style={`color: ${color}`}>
            <Icon size="xl" stack="1x" name="square"/>
        </span>
        <span style="color: #aaa">
            <Icon size="xl" stack="1x" name="square-o"/>
        </span>
    </div>
    <span>
        {#if showGroup && ratingGroup}
            <span class="group text-muted">
                {ratingGroup} /
            </span>
        {/if}
        {#if showName}
            <span class="name">
                {name}
            </span>
        {/if}
    </span>
</div>




<style>

    .stack-box {
        position: relative;
        vertical-align: middle;
        height: 1.25em !important;
        line-height: 1.25em !important;
        width: 1.25em !important;
    }

</style>