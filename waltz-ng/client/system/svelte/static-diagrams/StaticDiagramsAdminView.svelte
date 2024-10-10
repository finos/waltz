<script>
    import _ from "lodash";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {svgDiagramStore} from "../../../svelte-stores/svg-diagram-store";
    import {termSearch} from "../../../common";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import StaticDiagram from "./StaticDiagram.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import { displayError } from "../../../common/error-utils";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    
    let qry = "";

    function onRemoveDiagram(diagram) {
        if (!confirm("Are you sure you wish to delete?")){
            return;
        }
        return svgDiagramStore
            .remove(diagram.id)
            .then(() => {
                svgDiagramStore.findAll(true);
                toasts.success(`${diagram.group}: ${diagram.name} has been deleted successfully`);
            })
            .catch(e => displayError("Could not delete diagram", e));
    }

    $: diagramsLoadCall = svgDiagramStore.findAll();
    $: diagrams = _.orderBy($diagramsLoadCall.data, ["group", "priority", "name"]);
    $: visibleDiagrams = termSearch(diagrams, qry, ["group", "name"]);

</script>


<PageHeader icon="picture-o"
            name="Static Diagrams">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Static Diagrams</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">

                <SearchInput bind:value={qry} 
                             placeholder="Search..."/>
            <br>
            <table class="table table-hover table-condensed">
                <thead>
                    <tr>
                        <th>Group</th>
                        <th>Diagram Name</th>
                    </tr>
                </thead>
                <tbody>
                    {#each visibleDiagrams as diagram}
                    <tr>
                        <td>
                            <ViewLink state="main.system.static-diagram" ctx={{id: diagram.id}}>
                                {diagram.group}
                            </ViewLink>
                        </td>
                        <td> 
                            {diagram.name}
                        </td>
                        <td>
                            <button class="btn-link" 
                                    on:click|preventDefault={() => onRemoveDiagram(diagram)}>
                                    <Icon name="trash"/>
                                Remove
                            </button>
                        </td>
                    </tr>
                    {/each}
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="5">
                            <ViewLink state="main.system.static-diagram" ctx={{id: -1}}>
                                <Icon name="plus"/>
                                Add new
                            </ViewLink>
                        </td>
                    </tr>
                </tfoot>
            </table>
        </div>
    </div>
</div>