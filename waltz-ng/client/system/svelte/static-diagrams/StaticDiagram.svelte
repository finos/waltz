<script>
    import _ from "lodash";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {svgDiagramStore} from "../../../svelte-stores/svg-diagram-store";
    import toasts from "../../../svelte-stores/toast-store";
    import { displayError } from "../../../common/error-utils";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    

    export let diagramId;
    let blankDiagram = {name: "", group: "", priority: 1, keyProperty: "none", svg: "<h1>Change me. You can use HTML or SVG code</h1>"};
    let diagram = blankDiagram;
    let diagramLoadCall = null;
    let invalid = true;

    function goToAdminView() {
        $pageInfo = {
            state: "main.system.static-diagrams"
        };
    }

    function doSave(diagram) {
        return svgDiagramStore
            .save(diagram)
            .then(() => {
                goToAdminView();
                svgDiagramStore.findAll(true);
                if (isEdit){
                    toasts.success(`${diagram.group}: ${diagram.name} has been updated successfully`)
                } else {
                    toasts.success(`${diagram.group}: ${diagram.name} has been created successfully`)
                }})
            .catch(e => {
                displayError("Could not save diagram", e)
            });        
    }

    $: isNew = diagramId && diagramId === -1;

    $: isEdit = diagramId && diagramId >= 0;

    $: { 
        if (isEdit){
            diagramLoadCall = svgDiagramStore.getById(diagramId)
        }
    };

    $: diagram = $diagramLoadCall?.data || blankDiagram;

    $: {
        const badTextFields = _.some(
            [diagram.group, diagram.name], 
            v => _.isEmpty(v));

        invalid = badTextFields || diagram.priority <= 0;
    };

</script>


<PageHeader icon="picture-o"
            name="Static Diagram">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li><ViewLink state="main.system.static-diagrams">Static Diagrams</ViewLink></li>
            <li>{diagram ? diagram.name : ""}</li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">

            {#if diagram}
            <form autocomplete="off">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="group">
                                Group
                                <small class="text-muted">(required)</small>
                            </label>
                            <br/>
                            <input class="form-control" 
                                    id="group"
                                    required="required"
                                    placeholder="Group e.g., NAVAID.MEASURABLE.33"
                                    bind:value={diagram.group}>
                            <div class="help-block">
                                Group determines what screen the diagram will appear. Some example naming conventions include
                                <ul>
                                    <li>DATA_TYPE to appear on the Data screen</li>
                                    <li>ORG_TREE to appear on the People screen</li>
                                    <li>ORG_UNIT to appear on the Org Units screen</li>
                                    <li>NAVAID.MEASURABLE.id to appear on one of the measurable screens, where id denotes the category (which can be found in the url of the category)</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="desc">
                                Description
                            </label>
                            <br/>
                            <input class="form-control"
                                    id="desc" 
                                    placeholder="Description"
                                    bind:value={diagram.description}>
                            <div class="help-block">Description of this diagram</div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="name">
                                Name
                                <small class="text-muted">(required)</small>
                            </label>
                            <br/>
                            <input class="form-control" 
                                    id="name" 
                                    required="required"
                                    placeholder="Name"
                                    bind:value={diagram.name}>
                            <div class="help-block">Short name that describes this diagram</div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="priority">
                                Priority
                                <small class="text-muted">(required)</small>
                            </label>
                            <br/>
                            <input class="form-control" 
                                    type="number" 
                                    id="priority"
                                    required="required"
                                    bind:value={diagram.priority}>
                            <div class="help-block">If multiple diagrams belong to the same group, priority will be used to order them (ascending, where 1 is the highest priority)</div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="form-group">
                            <label for="svg">
                                Diagram Content
                                <small class="text-muted">(required)</small>
                            </label>
                            <br/>
                            <textarea class="form-control" 
                                      id="svg" 
                                      rows="10" 
                                      bind:value={diagram.svg}></textarea>
                            <div class="help-block">HTML or SVG code, any paths will be resolved against the context root of this waltz installation, e.g., /waltz</div>
                        </div>
                    </div>
                </div>
                <button type="submit" 
                        class="btn btn-success"
                        disabled={invalid}
                        on:click|preventDefault={() => doSave(diagram)}>
                    {isNew? "Create" : "Update"}
                </button>
                
                <button type="submit" 
                        class="btn btn-link" 
                        on:click|preventDefault={() => goToAdminView()}>
                    Cancel
                </button>
            </form>
            {:else}
                <h1>not found</h1>
            {/if}
        </div>
    </div>
</div>    

<style>
    input:invalid {
        border: 2px solid red;
    }

    textarea:invalid {
        border: 2px solid red;
    }
</style>
