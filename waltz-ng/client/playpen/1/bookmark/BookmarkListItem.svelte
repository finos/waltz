<script>
    import Icon from "./Icon.svelte";
    import moment from "moment";

    export let bookmark;

    const timeAgo = t => moment.utc(t).fromNow();
</script>


<style>
    .muted {
        font-size: smaller;
        font-weight: lighter;
    }
</style>


<div class="row">
    <div class="col-sm-9">
        <div>
            <Icon name={bookmark.icon}/>
            <a href={bookmark.url}>
                {bookmark.title || bookmark.domain}
            </a>
            {#if bookmark.title}
                <small class="muted">
                    ({bookmark.domain})
                </small>
            {/if}
        </div>
        <div class="text-muted">
            <Icon name="fw"/>
            {bookmark.description}
        </div>
    </div>
    <div class="col-sm-3">
        <div class="text-muted small">
            Last updated:
            <br>
            {bookmark.lastUpdatedBy}, {timeAgo(bookmark.lastUpdatedAt)}
        </div>
    </div>
</div>