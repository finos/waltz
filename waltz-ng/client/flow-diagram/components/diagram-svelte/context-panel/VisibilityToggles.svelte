<script>
    import visibility from "../store/visibility";
</script>

<p class="help-block">Use filters to selectively hide or show elements on the flow diagram</p>
<form>
    <div class="checkbox">
        <label>
            <input type="checkbox"
                   on:change={() => visibility.toggleAnnotations()}
                   checked={$visibility.annotations}>
            Annotations
        </label>
    </div>
    <div class="checkbox">
        <label>
            <input type="checkbox"
                   on:change={() => visibility.toggleFlowBuckets()}
                   checked={$visibility.flowBuckets}>
            Physical Flows
        </label>
    </div>
    <div class="checkbox">
        <label>
            <input type="checkbox"
                   on:change={() => visibility.togglePendingFlows()}
                   checked={$visibility.pendingFlows}>
            Pending Flows
        </label>
    </div>
    <div class="checkbox">
        <label>
            <input type="checkbox"
                   on:change={() => visibility.toggleRemovedFlows()}
                   checked={$visibility.removedFlows}>
            Removed Flows
        </label>
    </div>
</form>