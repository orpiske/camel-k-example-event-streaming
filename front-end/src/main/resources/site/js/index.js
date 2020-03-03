function siteDataTableOrdered(element, url, dbColumns, idx, order) {
    $(element).DataTable({
        columns: dbColumns,
        ajax: {
            url: url,
            dataSrc:  ''
        },
        order: [[ idx, order ]],
    });
};

function renderTimelineAlertIncident(obj) {
    var text = "<div class=\"alert alert-danger\"><span class=\"pficon pficon-error-circle-o\"></span>"

    text += obj.text;

    text += "</div>";

    return text;
}

function renderTimelineWarningIncident(obj) {
    var text = "<div class=\"alert alert-warning\"><span class=\"pficon pficon-warning-triangle-o\"></span>"

    text += obj.text;

    text += "</div>";

    return text;
}

function renderTimelineInfoIncident(obj) {
    var text = "<div class=\"alert alert-info\"><span class=\"pficon pficon-info\"></span>"

    text += obj.text;

    text += "</div>";

    return text;
}

$(document).ready(function () {
    var path = $('[timeline]').attr('data-api')

    var url = "http://timeline-bridge.camel-k-event-streaming-dev.apps.camel-k.rhmw-integrations.net" + path;

    axios.get(url).then(function (response) {
        // console.log("Received " + response.data);

        var container = document.getElementById('timeline');
        var myHtml = '';

        for(var i = 0; i < response.data.length; i++) {
            var obj = JSON.parse(response.data[i]);

            if ((i % 3) == 0) {
                myHtml += renderTimelineAlertIncident(obj)
            }
            else {
                if ((i % 2) == 0) {
                    myHtml += renderTimelineWarningIncident(obj)
                }
                else {
                    myHtml += renderTimelineInfoIncident(obj)
                }
            }

            // console.log("1: " + obj);
            // console.log("2: " + );
            // console.log("3: " + obj["text"]);
        }

        container.innerHTML = myHtml
    });
})