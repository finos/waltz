<script>

    import DiagramBuildView from "./DiagramBuildView.svelte";
    import {diagramService} from "../entity-diagram-store";
    import {diagramMode, DiagramModes} from "./diagram-builder-store";
    import DiagramControls from "../builder-controls/DiagramControls.svelte";
    import DiagramTreeSelector from "./DiagramTreeSelector.svelte";
    import DiagramView from "../DiagramView.svelte";
    import GroupDetailsPanel from "../builder-controls/GroupDetailsPanel.svelte";
    import {prettyHTML} from "../../../../system/svelte/nav-aid-builder/nav-aid-utils";
    import PageHeader from "../../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../../common/svelte/ViewLink.svelte";
    import GroupControls from "../builder-controls/GroupControls.svelte";

    let items = [];

    let html = "";
    let data = "";
    let vizElem;

    const {
        reset,
        selectedDiagram,
        selectedGroup,
        groups,
        groupsWithData,
        diagramLayout,
        selectGroup,
    } = diagramService;

    function selectOverlayGroup(group) {
        selectGroup(group)
    }

    function deselectGroup() {
        selectOverlayGroup(null);
    }


    $: {
        if (vizElem && $groups) {
            // using a timeout to give innerHTML chance to be updated
            // before we copy it
            setTimeout(() => {
                html = prettyHTML(vizElem.innerHTML);
                data = JSON.stringify($groups, "", 2);
            });
        }
    }

</script>



<PageHeader icon="picture-o"
            name="Diagram Builder">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Diagram Builder</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">

        <div class="col-sm-12">
            <DiagramControls></DiagramControls>
            <hr>
        </div>


        {#if $selectedDiagram}
            <div style="padding-top: 1em">
                <div class="col-sm-8"
                     bind:this={vizElem}>
                    {#if $diagramMode === DiagramModes.EDIT}
                        <DiagramBuildView group={$diagramLayout}>
                        </DiagramBuildView>
                    {:else if $diagramMode === DiagramModes.VIEW}
                        <DiagramView group={$diagramLayout}>
                        </DiagramView>
                    {/if}
                </div>

                <div class="col-sm-4">
                    <h4>Structure</h4>
                    <DiagramTreeSelector groups={$groupsWithData}
                                         onSelect={selectOverlayGroup}
                                         onDeselect={deselectGroup}/>
                    <hr>
                    {#if $diagramMode === DiagramModes.EDIT}
                        <GroupControls/>
                    {:else}
                        <GroupDetailsPanel/>
                    {/if}
                </div>
            </div>

            {#if $diagramMode === DiagramModes.VIEW}
                <div class="col-sm-6"
                     style="margin-top: 2em">
                    <div class="waltz-scroll-region-350">
                        <pre>{html}</pre>
                    </div>
                </div>
                <div class="col-sm-6"
                     style="margin-top: 2em">
                    <div class="waltz-scroll-region-350">
                        <pre>{data}</pre>
                    </div>
                </div>
            {/if}
        {/if}
    </div>
</div>