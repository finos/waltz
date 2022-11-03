<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=10,9">
    <meta charset="utf-8">
    <title>Application Group Recalculation</title>

    <style>

        a {
            text-decoration: none
        }

        * {
            font-size: 14px;
            font-family: sans-serif, Arial, Helvetica
        }
    </style>
</head>

<body>
<div class="container-fluid" style="padding: 3em 4em">
    <#if success>
        <div style="border: 1px solid lightgreen; border-radius: 5px; padding: 10px">
            <span style="color: lightgreen; font-size: x-large; padding-right: 0.5em">&#10004;</span>
            <span style="font-size: x-large">Successfully updated application group</span>
            <div style="padding-top: 1em">This group now contains ${appCount} applications passing the filter criteria
            </div>
        </div>
    <#else>
        <div style="border: 1px solid lightcoral; border-radius: 5px; padding: 10px">
            <span style="color: lightcoral; font-size: x-large; padding-right: 0.5em">!</span>
            <span style="font-size: x-large">Failed to update application group</span>
            <div style="padding-top: 1em">Error: ${errorMessage}</div>
        </div>
    </#if>

    <div style="padding-top: 2em">
        <a style="text-decoration: none;" href="../../../../app-group/${appGroupId}?sections=10" target="_blank">
            &#8617; Back to Application Group
        </a>
    </div>
</div>
</body>