<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=10,9">
    <meta charset="utf-8">
    <title>Application Group Recalculation</title>
    <link href='https://fonts.googleapis.com/css?family=Oxygen|Hind|Maven+Pro' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
          integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css" rel="stylesheet"
          type="text/css">
    <link href="/style/style.scss" rel="stylesheet" type="text/css">
</head>

<body>
<div class="container-fluid" style="padding: 3em 4em">
    <#if success>
        <div style="border: 1px solid lightgreen; border-radius: 5px; padding: 10px">
            <i style="color: lightgreen" class="fa fa-check fa-2x" aria-hidden="true"></i>
            <span style="font-size: x-large">Successfully updated application group</span>
            <div style="padding-top: 1em">This group now contains ${appCount} applications passing the filter criteria
            </div>
        </div>
    <#else>
        <div style="border: 1px solid lightcoral; border-radius: 5px; padding: 10px">
            <i style="color: lightcoral" class="fa fa-exclamation-circle fa-2x" aria-hidden="true"></i>
            <span style="font-size: x-large">Failed to update application group</span>
            <div style="padding-top: 1em">Error: ${errorMessage}</div>
        </div>
    </#if>

    <div style="padding-top: 2em">
        <a href="/app-group/${appGroupId}?sections=10" target="_blank">
            <i class="fa fa-share-square-o fa-flip-horizontal" aria-hidden="true"></i>
            Back to Application Group
        </a>
    </div>
</div>
</body>