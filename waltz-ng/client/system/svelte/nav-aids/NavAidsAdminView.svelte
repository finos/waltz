<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import {svgDiagramStore} from "../../../svelte-stores/svg-diagram-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import NavAidEditView from "./NavAidEditView.svelte";
    import NavAidRemovalConfirmation from "./NavAidRemovalConfirmation.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import _ from "lodash";
    import {termSearch} from "../../../common";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";


    const Modes = {
        EDIT: "EDIT",
        VIEW: "VIEW",
        REMOVE: "REMOVE"
    }

    let activeMode = Modes.VIEW;
    let selectedDiagram;
    let diagramsCall = svgDiagramStore.findAll();
    let qry;

    function edit(diagram) {
        selectedDiagram = diagram;
        activeMode = Modes.EDIT
    }

    function loadDiagrams() {
        diagramsCall = svgDiagramStore.findAll(true);
    }

    function onSave(evt) {
        svgDiagramStore
            .save(evt.detail)
            .then(() => {
                loadDiagrams();
                cancel();
                toasts.success("Successfully saved diagram");
            })
            .catch(e => displayError("Could not save diagram.", e));
    }

    function remove(diagram) {
        selectedDiagram = diagram;
        activeMode = Modes.REMOVE
    }

    function onRemove(evt) {
        const diagramId = evt.detail.id;
        svgDiagramStore
            .remove(diagramId)
            .then(() => {
                loadDiagrams();
                cancel();
                toasts.success("Successfully removed diagram");
            })
            .catch(e => displayError("Could not remove diagram.", e));
    }

    function cancel() {
        selectedDiagram = null;
        activeMode = Modes.VIEW
    }

    $: diagrams = $diagramsCall?.data;

    $: diagramList = _.isEmpty(qry)
        ? diagrams
        : termSearch(diagrams, qry, ["name", "description", "group"]);

</script>


<PageHeader icon="picture-o"
            name="Navigation Aids">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Navigation Aids</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p>Navigation aids are used across waltz to display static diagrams</p>
        </div>
    </div>

    {#if activeMode === Modes.VIEW}
        <SearchInput bind:value={qry}/>
        <table class="table table-condensed table-hover table-striped">
            <thead>
            <tr>
                <th>Name</th>
                <th>Group</th>
                <th>Description</th>
                <th>Priority</th>
                <th>Key Property</th>
                <th>Product</th>
                <th>Display Width Percent</th>
                <th>Display Height Percent</th>
                <th>Operations</th>
            </tr>
            </thead>
            <tbody>
            {#each diagramList as diagram}
                <tr>
                    <td>{diagram.name}</td>
                    <td>{diagram.group}</td>
                    <td style="width: 25%">{diagram.description || "-"}</td>
                    <td>{diagram.priority}</td>
                    <td>{diagram.keyProperty}</td>
                    <td>{diagram.product }</td>
                    <td style="width: 5%">{diagram.displayWidthPercent || "-"}</td>
                    <td style="width: 5%">{diagram.displayHeightPercent || "-"}</td>
                    <td>
                        <span>
                            <button class="btn btn-skinny"
                                    on:click={() => edit(diagram)}>
                                <Icon name="pencil"/>Edit
                            </button>
                            |
                            <button class="btn btn-skinny"
                                    on:click={() => remove(diagram)}>
                                <Icon name="trash"/>Remove
                            </button>
                        </span>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>

        <div>
            <button class="btn btn-default"
                    on:click={() => activeMode = Modes.EDIT}>
                <Icon name="plus"/>Add new
            </button>
        </div>

    {:else if activeMode === Modes.EDIT}
       <NavAidEditView diagram={selectedDiagram}
                       on:save={onSave}
                       on:cancel={cancel}/>

    {:else if activeMode === Modes.REMOVE}
        <NavAidRemovalConfirmation diagram={selectedDiagram}
                                   on:remove={onRemove}
                                   on:cancel={cancel}/>
    {/if}
</div>