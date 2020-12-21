<script>
    import BookmarkListItem from "./BookmarkListItem.svelte";
    export let bookmarkGroups = {};
    export let actions = [];
</script>

<table class="table table-condensed table-hover">
    <colgroup>
        <col width="5%">
        <col width="65%">
        <col width="30%">
    </colgroup>
    {#each bookmarkGroups as group}
        <tbody>
        {#each group.value as bookmark}
            <BookmarkListItem {actions} {bookmark}/>
        {/each}
        </tbody>
    {/each}
</table>