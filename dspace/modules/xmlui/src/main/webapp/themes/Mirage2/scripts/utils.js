(function($) {
    DSpace.getTemplate = function(name) {
        if (DSpace.templates === undefined || DSpace.templates[name] === undefined) {
            $.ajax({
                url : DSpace.theme_path + 'templates/' + name + '.handlebars',
                success : function(data) {
                    if (DSpace.templates === undefined) {
                        DSpace.templates = {};
                    }
                    DSpace.templates[name] = Handlebars.compile(data);
                },
                async : false
            });
        }
        return DSpace.templates[name];
    };
})(jQuery);