<script>
    import BookmarkTable from "./BookmarkTable.svelte";
    import BookmarkCategoryMenu from "./BookmarkCategoryMenu.svelte";
    import SearchInput from "../common/SearchInput.svelte";

    import {CORE_API} from "../../../common/services/core-api-utils";
    import {nestEnums} from "./enum-utils";
    import {mkBookmarkKinds, nestBookmarks} from "./bookmark-utils";
    import {bookmarks, loadBookmarks} from "./bookmark-store";
    import {mkUserStore} from "../common/user-store";

    import roles from "../../../user/system-roles";
    import BookmarkListItem from "./BookmarkListItem.svelte";

    export let serviceBroker = null;
    export let primaryEntityRef = null;

    let user;
    let nestedEnums = {};
    let bookmarkKinds = {};
    let bookmarkGroups = [];

    let removalCandidate = null;
    let editCandidate = null;

    let selectedKind = null;
    let qry = "";
    let actions = [];

    const editAction = {
        icon: "pencil",
        name: "Edit",
        handleAction: (d) => editCandidate = d
    };

    const removeAction = {
        icon: "trash",
        name: "Remove",
        handleAction: (d) => removalCandidate = d
    };

    $: {
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => nestedEnums = nestEnums(r.data));

        user = mkUserStore(serviceBroker);
    }

    $: loadBookmarks(serviceBroker, primaryEntityRef);

    $: actions = _.includes($user.roles, roles.BOOKMARK_EDITOR.key)
        ? [editAction, removeAction]
        : [];

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

    function onKindSelect(e) {
        selectedKind = e.detail.kind;
    }

</script>


<div class="row">
    <div class="col-sm-4">
        <BookmarkCategoryMenu on:kindSelect={onKindSelect}
                              bookmarkKinds={bookmarkKinds}/>
    </div>

    <div class="col-sm-8">

        {#if removalCandidate}
            <div class="alert alert-warning removal-warning">
                <h3>Confirm bookmark removal</h3>
                Are you sure you want to this bookmark ?
                <table class="table">
                    <BookmarkListItem bookmark={removalCandidate}/>
                </table>
            </div>
        {:else if editCandidate}
            <h1>Edit</h1>
        {:else}
            {#if $bookmarks.length > 5}
                <SearchInput bind:value={qry}
                             placeholder="Search bookmarks..."/>
                <br>
            {/if}
            {#if !bookmarkGroups}
                Loading
            {:else}
                <BookmarkTable {bookmarkGroups} {actions}/>
            {/if}
        {/if}

    </div>
</div>


<style>
    .removal-warning {
        background-color: #ffe6ff
    }
    .removal-warning table {
        padding: 1em;
        border: 1px solid #ccc;

    }
</style>

