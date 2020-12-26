<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    import {mkStaticPanelStore} from "../../../svelte-stores/static-panel-store" ;
    import _ from "lodash";
    import StaticPanelEditor from "./StaticPanelEditor.svelte";
    import {mkUserStore} from "../../../svelte-stores/user-store";
    import roles from "../../../user/system-roles";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";

    export let serviceBroker;

    let panels;
    let panelList = [];
    let user;
    let qry = "";
    let selectedPanel = null;
    let canEdit = false;

    $: {
        panels = mkStaticPanelStore(serviceBroker);
        user = mkUserStore(serviceBroker);
    }

    $: panelList = _
        .chain($panels)
        .filter(p => _.isEmpty(qry)
            ? true
            : _.toLower(p.group + p.title).indexOf(_.toLower(qry)) > -1)
        .orderBy(["group", "title"])
        .value();

    $: canEdit = _.includes($user.roles, roles.ADMIN.key);

    function onSelectPanel(p) {
        selectedPanel = p;
    }

    function onAddPanel() {
        selectedPanel = {
            icon: "info",
            content: `You can use html or markdown. For example:
# This is a header
## And a smaller header

- lists can be
- created using hyphens
  - and indented

Links look like: \[Google\](https://www.google.com)`,
            width: 12,
            priority: 1
        };
    }

    function doSave(panel) {
        return panels
            .save(panel)
            .then(() => selectedPanel = null);
    }
</script>


<PageHeader icon="code"
            name="Static Panels">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Static Panels</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">

            {#if selectedPanel}

                <StaticPanelEditor panel={selectedPanel}
                                   doCancel={() => selectedPanel = null}
                                   {doSave}/>

            {:else}

                <SearchInput bind:value={qry} placeholder="Search..."/>
                <br>
                <table class="table table-hover table-condensed">
                    <thead>
                    <tr>
                        <th>Group</th>
                        <th>Title</th>
                        <th>Icon</th>
                        <th>Priority</th>
                        <th>Width</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each panelList as panel}
                        <tr>
                            <td>
                                {#if canEdit}
                                    <button class="btn-link"
                                            on:click={() => onSelectPanel(panel)}>
                                        {panel.group}
                                    </button>
                                {:else}
                                    {panel.group}
                                {/if}
                            </td>
                            <td>{panel.title}</td>
                            <td>
                                <Icon name={panel.icon}/>
                                {panel.icon}
                            </td>
                            <td>{panel.priority}</td>
                            <td>{panel.width}</td>
                        </tr>
                    {/each}
                    </tbody>
                    {#if canEdit}
                        <tbody>
                        <tr>
                            <td colspan="5">
                                <button class="btn-link"
                                        on:click={onAddPanel}>
                                    <Icon name="plus"/>
                                    Add new
                                </button>
                            </td>
                        </tr>
                        </tbody>
                    {/if}
                </table>

           {/if}
        </div>
    </div>

</div>