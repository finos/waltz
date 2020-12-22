<script>
    import Icon from "../common/Icon.svelte";
    import MiniActions from "../common/MiniActions.svelte";
    import DateTime from "../common/DateTime.svelte";

    import _ from "lodash";
    import LastEdited from "../common/LastEdited.svelte";

    export let bookmark;
    export let actions = [];
</script>


<tr class="waltz-visibility-parent">
    <td style="vertical-align: middle">
        <Icon name={bookmark.icon}/>
    </td>
    <td>
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
    <td style="text-align: right; padding-right: 1em;">

        <div class="waltz-visibility-child-20">
            <MiniActions ctx={bookmark}
                         actions={actions}/>
        </div>

        <div class="text-muted small">
            Last updated:
            <LastEdited entity={bookmark}/>
        </div>

    </td>
</tr>

