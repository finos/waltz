<script>

    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import TaxonomyNavAidBuilder from "./TaxonomyNavAidBuilder.svelte";
    import DataTypeNavAidBuilder from "./DataTypeNavAidBuilder.svelte";
    import MeasurableCategoryNavAidBuilder from "./MeasurableCategoryNavAidBuilder.svelte";
    import PersonNavAidBuilder from "./PersonNavAidBuilder.svelte";

    const Modes = {
        NONE: {},
        DATA_TYPE: {
            component: DataTypeNavAidBuilder
        },
        MEASURABLE_CATEGORY: {
            component: MeasurableCategoryNavAidBuilder
        },
        PERSON: {component: PersonNavAidBuilder},
        ORG_UNIT: {component: TaxonomyNavAidBuilder}
    };

    let mode = Modes.PERSON;

    function switchMode(newMode) {
        mode = newMode;
    }

</script>


<PageHeader icon="picture-o"
            name="Navigation Aid Builder">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.system.list">System Admin</ViewLink>
            </li>
            <li>Navigation Aid Builder</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p>
                Navigation aids are used in Waltz to help users navigate between high level entities.
                Navigation aids can created for showing simplified views of:
            </p>
            <ul>
                <li>
                    <i>
                        Taxonomy based
                    </i>
                    <ul>
                        <button class="btn btn-skinny"
                                on:click={() => switchMode(Modes.DATA_TYPE)}>
                            <li>Data Types</li>
                        </button>
                        <li>
                            <button class="btn btn-skinny"
                                    on:click={() => switchMode(Modes.MEASURABLE_CATEGORY)}>
                                Measurable Categories
                            </button>
                        </li>
                    </ul>
                <li>
                    <i>Curated from hierarchies</i>
                    <ul>
                        <li>Person Hierarchies</li>
                        <li>Organisational Units</li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <hr>
    <div class="row">
        <div class="col-md-12">
            {#if mode === Modes.NONE}
                <p>Select a type of navigation aid to build from the list above</p>
            {:else}
                <svelte:component this={mode.component}
                                  {...mode.props}/>
            {/if}

        </div>
    </div>
</div>