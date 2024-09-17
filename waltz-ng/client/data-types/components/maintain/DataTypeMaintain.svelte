<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    //import {actorStore} from "../../../svelte-stores/actor-store";
    //import {termSearch} from "../../../common";
    import _ from "lodash";
    //import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import DataExtractLink from "../../../common/svelte/DataExtractLink.svelte";
    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import DataTypePicker from "../../../common/svelte/entity-pickers/DataTypePicker.svelte";
    import DataTypeTreeNode from "../../../common/svelte/DataTypeTreeNode.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";

    let sourceDataType = null;
    let targetDataType = null;
    let canApply = false;

    function onSourceDataTypeSelect(evt) {
        console.log("Hi Dad", evt.detail);
        sourceDataType = evt.detail;
    }

    function onTargetDataTypeSelect(evt) {
        const dataType = evt.detail;
        if (!dataType.concrete){
            console.log("No way jose")
            return;
        }
        console.log("Hi Mum", dataType);
        targetDataType = dataType;
    }

    $: canApply = targetDataType && targetDataType.concrete && !targetDataType.deprecrated 
</script>

<PageHeader icon="wrench"
            name="Data Types"
            small="maintain">

    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.data-type.list">Data Types</ViewLink>
            </li>
            <li>
                Maintain
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">
        <h1>
            Hello World
        </h1>
        <div class="row">
            <div class="col-md-5">
                    <b>From</b>
                    
                    {#if sourceDataType} 
                        <EntityLabel ref={sourceDataType}/>
                        <button class="btn" on:click={() => sourceDataType=null}>Oops..</button>
                    {:else}
                        <DataTypeTreeSelector multiSelect={false}
                    on:select={onSourceDataTypeSelect}/>
                    {/if}
            </div>
            <div class="col-md-2">
                Migrate &raquo;
            </div>
            <div class="col-md-5">
                <b>To</b>

                {#if targetDataType} 
                <EntityLabel ref={targetDataType}/>
                <button class="btn" on:click={() => targetDataType=null}>Oops..</button>
            {:else}
                <DataTypeTreeSelector multiSelect={false}
            on:select={onTargetDataTypeSelect}/>
            {/if}
            </div>
        </div>
        <h1> {canApply} </h1>
        
    </div>
</div>
