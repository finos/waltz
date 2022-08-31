<script>
    import {getContext} from "svelte";
    import DatePicker from "../../../../../common/svelte/DatePicker.svelte";
    import {threshold, cutoff} from "./store";
    import moment from "moment";
    import {writable} from "svelte/store";

    const datePickerOpts = {
        maxDate: new Date()
    };

    const widgetParameters = getContext("widgetParameters");
    const attestationType = writable("LOGICAL_DATA_FLOW");

    function setToSixMonthsAgo() {
        $cutoff = moment().subtract(6, "months");
    }

    function setToOneYearAgo() {
        $cutoff = moment().subtract(1, "year");
    }

    $: $widgetParameters = {
        attestedEntityKind: $attestationType
    };

</script>

<div class="help-block">
    Configure the attestation overlay by selecting:
    <ul>
        <li>the type of attestation you are interested in</li>
        <li>the cutoff date for considering an attestation as passing</li>
        <li>the passing threshold value</li>
    </ul>
</div>

<hr>


<label for="attestation_type">
    Attestation Type:
</label>
<select style="display: block"
        id="attestation_type"
        bind:value={$attestationType}>
    <option value={"LOGICAL_DATA_FLOW"}>
        Logical Flow
    </option>
    <option value={"PHYSICAL_FLOW"}>
        Physical Flow
    </option>
</select>
<div class="help-block">
    Defines which kind of attestation you want to show data for.
</div>

<label for="cutoff">
    Cutoff Date:
</label>
<DatePicker id="cutoff"
            canEdit={true}
            origDate={$cutoff}
            options={datePickerOpts}
            on:change={d => $cutoff = d.detail}/>

<div class="help-block">
    The oldest attestation date you consider for an application to be marked as passing.
    If the attestation happened before this date (or has never occured) the application is marked as failing.
    For convenience you can use the following presets:
    <button class="btn-skinny"
            on:click={setToOneYearAgo}>
        1 year ago
    </button>
    or
    <button class="btn-skinny"
            on:click={setToSixMonthsAgo}>
        6 months ago.
    </button>
</div>

<label for="threshold">
    Threshold Percentage  (<span>{$threshold} %</span>):
</label>
<input id="threshold"
       type="range"
       min="0"
       max="100"
       bind:value={$threshold}>
<div class="help-block">
    The threshold percentage determines if the cell is shown as an overall pass or fail.
</div>



