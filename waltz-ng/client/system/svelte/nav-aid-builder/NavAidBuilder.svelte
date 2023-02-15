<script>

import PageHeader from "../../../common/svelte/PageHeader.svelte";
import ViewLink from "../../../common/svelte/ViewLink.svelte";
</script>


<PageHeader icon="picture-o"
            name="Navigation Aid Builder">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
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
                <li><i>Taxonomy based</i></li>
                <ul>
                    <li>Data Types</li>
                    <li>Measurable Categories</li>
                </ul>
                <li><i>Curated from hierarchies</i></li>
                <ul>
                    <li>Person Hierarchies</li>
                    <li>Organisational Units</li>
                </ul>
            </ul>
        </div>
    </div>
    <div class="row">
    </div>
</div>