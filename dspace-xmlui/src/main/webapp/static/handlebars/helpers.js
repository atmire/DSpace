$(document).ready(function() {
    $.getJSON(window.publication.contextPath + "/JSON/translate/i18n", undefined, function (json) {
        $(document).data('i18n', json);
    });
});

Handlebars.registerHelper('message', function(message) {

    return new Handlebars.SafeString($(document).data('i18n')[message]);
});