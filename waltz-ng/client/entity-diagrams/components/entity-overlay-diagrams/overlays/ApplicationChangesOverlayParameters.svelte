<script>

    import {diagramService} from "../entity-diagram-store";
    import {onMount} from "svelte";
    import {timeFormat} from "d3-time-format";
    import _ from "lodash";
    import moment from "moment";
    import {appChangeAdditionalYears} from "./overlay-store";

    const {updateOverlayParameters} = diagramService;

    let futureDate = null;

    const fmt = timeFormat("%Y-%m-%d");

    onMount(() => {
        updateOverlayParameters({targetDate: fmt(futureDate)});
    })


    function onSelect(futureDate) {
        updateOverlayParameters({targetDate: fmt(futureDate)});
    }

    const debouncedOnSelect = _.debounce(onSelect, 500);

    $: futureDate = moment().set({"date": 1, "month": 0}).add($appChangeAdditionalYears, "years");
    $: debouncedOnSelect(futureDate);

</script>

<div class="content">

    <label for="future-date">Projected application changes until:</label>
    <span>{fmt(futureDate)}</span>

    <input id="future-date"
           type="range"
           min="1"
           max="10"
           bind:value={$appChangeAdditionalYears}>

    <div class="help-block">
        Use the slider to adjust how far in the future to project app changes.
        This is calculated by incorporating app retirement and commission dates along with rating planned decommissions and replacements.
    </div>

</div>
