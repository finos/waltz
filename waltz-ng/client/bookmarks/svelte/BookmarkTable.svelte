<script>
    import BookmarkListItem from "./BookmarkListItem.svelte";
    export let bookmarkGroups = {};
    export let actions = [];
</script>

<table class="table table-condensed table-hover">
    {#each bookmarkGroups as group}
        <tbody>
        {#each group.value as bookmark}
            <BookmarkListItem {actions} {bookmark}/>
        {/each}
        </tbody>
    {/each}
    <slot name="footer">
    </slot>
</table>