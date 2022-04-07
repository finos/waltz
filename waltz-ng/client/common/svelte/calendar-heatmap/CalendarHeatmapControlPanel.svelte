<script>

    import DatePicker from "../DatePicker.svelte";
    import Icon from "../Icon.svelte";


    export let setStartDate = (d) => console.log("setting start date", d)
    export let setEndDate = (d) => console.log("setting end date", d)
    export let startDate;
    export let endDate;

    let changeDateSelection = false;

</script>


{#if changeDateSelection}
    <div class="row">
        <div class="col-sm-6">
            <strong>Start Date:</strong>
            <DatePicker canEdit={true}
                        origDate={startDate}
                        on:change={d => setStartDate(d.detail)}/>
        </div>
        <div class="col-sm-6">
            <strong>End Date:</strong>
            <DatePicker canEdit={true}
                        origDate={endDate}
                        on:change={d => setEndDate(d.detail)}/>
        </div>
        <div class="col-sm-6"></div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <button class="btn btn-skinny"
                    on:click={() => changeDateSelection = false}>
                Cancel
                <Icon name="times"/>
            </button>
        </div>
    </div>
{:else}
    <button class="btn btn-skinny"
            on:click={() => changeDateSelection = true}>
        Change date selection
        <Icon name="pencil"/>
    </button>
{/if}