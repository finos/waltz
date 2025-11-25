<script>
    export let subtask;
</script>

<div style="padding: 0.5em">
    <strong>{subtask.subtask_id}</strong>
    <hr>
    <dl class="">
        <dt>Summary</dt>
        <dd>{subtask.subtask_summary}</dd>
        <dt>Assignee</dt>
        <dd>{subtask.subtask_assignee}</dd>
        <dt>Status</dt>
        <dd>{subtask.subtask_status}</dd>
        <dt>Resolution</dt>
        <dd>{subtask.subtask_resolution}</dd>
    </dl>
</div>
