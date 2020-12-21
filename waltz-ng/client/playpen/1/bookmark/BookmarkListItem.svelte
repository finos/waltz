<script>
    import Icon from "../common/Icon.svelte";
    import moment from "moment";
    import MiniActions from "../common/MiniActions.svelte";
    import _ from "lodash";

    export let bookmark;
    export let actions = [];

    const timeAgo = t => moment.utc(t).fromNow();

    const modes = {
        EDITING: "EDITING",
        VIEWING: "VIEWING"
    };

    let hovering = false;
    let mode = modes.EDITING;
</script>


<tr>
    <td style="vertical-align: middle">
        <Icon name={bookmark.icon}/>
    </td>
    <td class:hovering>
        <a href={bookmark.url}>
            {bookmark.title || bookmark.domain}
        </a>
        {#if bookmark.title}
            <small class="text-muted">
                ({bookmark.domain})
            </small>
        {/if}
        {#if bookmark.isRestricted}
            <small class="text-muted">
                <Icon name="lock"/>
                Permissions may be required
            </small>
        {/if}

        <div class="text-muted"
             class:italics={_.isNil(bookmark.description)}>
            {bookmark.description || "No description provided"}
        </div>
    </td>
    <td>
        {#if mode === modes.EDITING}
            <MiniActions ctx={bookmark}
                         actions={actions}/>
        {:else if mode === modes.VIEWING}
            <div class="text-muted small">
                Last updated:
                <br>
                {bookmark.lastUpdatedBy},
                {timeAgo(bookmark.lastUpdatedAt)}
            </div>
        {/if}
    </td>
</tr>

