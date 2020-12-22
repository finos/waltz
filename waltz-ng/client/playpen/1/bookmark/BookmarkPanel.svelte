<script>
    import BookmarkTable from "./BookmarkTable.svelte";
    import BookmarkCategoryMenu from "./BookmarkCategoryMenu.svelte";
    import SearchInput from "../common/SearchInput.svelte";

    import {CORE_API} from "../../../common/services/core-api-utils";
    import {nestEnums} from "../common/enum-utils";
    import {mkBookmarkKinds, nestBookmarks} from "./bookmark-utils";
    import {mkBookmarkStore} from "./bookmark-store";
    import {mkUserStore} from "../common/user-store";

    import roles from "../../../user/system-roles";
    import NoData from "../common/NoData.svelte";

    import _ from "lodash";
    import Icon from "../common/Icon.svelte";
    import BookmarkRemovalConfirmation from "./BookmarkRemovalConfirmation.svelte";
    import BookmarkEditor from "./BookmarkEditor.svelte";

    export let serviceBroker = null;
    export let primaryEntityRef = null;

    let user;
    let bookmarks;
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
        bookmarks = mkBookmarkStore(serviceBroker);
    }

    $: bookmarks.load(primaryEntityRef);

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

    function doRemove() {
        bookmarks
            .remove(removalCandidate)
            .then(() => removalCandidate = null);
    }

    function doSave(bookmark) {
        bookmarks
            .save(bookmark)
            .then(() => editCandidate = null);
    }

</script>


<div class="row">
    <div class="col-sm-4">
        <BookmarkCategoryMenu on:kindSelect={onKindSelect}
                               bookmarkKinds={bookmarkKinds}/>
    </div>

    <div class="col-sm-8">

        {#if removalCandidate}
            <BookmarkRemovalConfirmation bookmark={removalCandidate}
                                          {doRemove}
                                          doCancel={() => removalCandidate = null}/>
        {:else if editCandidate}
            <BookmarkEditor bookmark={editCandidate}
                            {doSave}
                            doCancel={() => editCandidate = null} />
        {:else}
            {#if $bookmarks.length > 5}
                <SearchInput bind:value={qry}
                             placeholder="Search bookmarks..."/>
                <br>
            {/if}
            {#if _.isEmpty(bookmarkGroups)}
                <NoData>
                    No bookmarks
                </NoData>
            {:else}
                <BookmarkTable {bookmarkGroups} {actions}>
                    <tfoot slot="footer">
                    {#if actions.length > 0}
                        <tr>
                            <td colspan="3">
                                <button class="btn btn-link">
                                    <Icon name="plus"/>
                                    Add bookmark
                                </button>
                            </td>
                        </tr>
                    {/if}
                    </tfoot>
                </BookmarkTable>
            {/if}
        {/if}

    </div>
</div>

