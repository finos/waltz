<script>

    import DatePicker from "../DatePicker.svelte";
    import Icon from "../Icon.svelte";
    import {timeFormat} from "d3-time-format";

    const format = timeFormat("%Y-%m-%d");

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
        <div class="col-sm-12" style="padding-bottom: 2em;">
            <button class="btn btn-skinny"
                    on:click={() => changeDateSelection = false}>
                Close
                <Icon name="times"/>
            </button>
        </div>
    </div>
{:else}
    <b>Date Range:</b>
    {format(startDate)} to {format(endDate)}.  
    <button class="btn btn-skinny"
            on:click={() => changeDateSelection = true}>
        Change date range
        <Icon name="pencil"/>
    </button>
{/if}