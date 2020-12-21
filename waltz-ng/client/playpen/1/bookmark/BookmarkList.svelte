<script>
    import BookmarkListItem from "./BookmarkListItem.svelte";
    import {CORE_API} from "../../../common/services/core-api-utils";
    import {nestEnums} from "./enum-utils";
    import {mkBookmarkKinds, nestBookmarks} from "./bookmark-utils";
    import {bookmarks, loadBookmarks} from "./stores";
    import BookmarkCategoryMenu from "./BookmarkCategoryMenu.svelte";
    import SearchInput from "../common/SearchInput.svelte";

    export let serviceBroker;
    export let primaryEntityRef;

    let nestedEnums = {};
    let bookmarkKinds = {};
    let bookmarkGroups = [];

    let selectedKind = null;
    let qry = "";

    $: serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => nestedEnums = nestEnums(r.data));

    $: {
        const xs = _
            .chain($bookmarks)
            .filter(selectedKind
                ? b => b.bookmarkKind === selectedKind.key
                : () => true)
            .filter(_.isEmpty(qry)
                ? () => true
                : b => _.join([b.title, b.url, b.description]).toLowerCase().indexOf(qry) > -1)
            .value()

        bookmarkGroups = nestBookmarks(nestedEnums, xs);
    }

    $: bookmarkKinds = mkBookmarkKinds(nestedEnums, $bookmarks);

    $: loadBookmarks(serviceBroker, primaryEntityRef);

    function onKindSelect(e) {
        selectedKind = e.detail.kind;
    }

    const actions = [
        {
            icon: "pencil",
            name: "Edit",
            isEnabled: (d) => true,
            handleAction: (d) => console.log("edit", d)
        }, {
            icon: "trash",
            name: "Remove",
            isEnabled: (d) => true,
            handleAction: (d) => console.log("remove", d)
        }
    ];
</script>


<div class="row">
    <div class="col-sm-4">
        <BookmarkCategoryMenu on:kindSelect={onKindSelect}
                              bookmarkKinds={bookmarkKinds}/>
    </div>

    <div class="col-sm-8">
        {#if $bookmarks.length > 5}
            <SearchInput bind:value={qry}
                         placeholder="Search bookmarks..."/>
            <br>
        {/if}

        {#if !bookmarkGroups}
            Loading
        {:else}
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
        {/if}
    </div>
</div>


<style>

</style>

