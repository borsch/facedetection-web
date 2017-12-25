(function(exports){

    var csrf = {};

    exports.setCSRF = function(name, val){
        csrf = {
            name: name,
            val: val
        };
    };

    exports.get = function(options){
        options = options || {};
        options.type = 'GET';
        _ajax(options);
    };

    exports.put = function(options){
        putAndPost(options, 'PUT');
    };

    exports.post = function(options){
        putAndPost(options, 'POST');
    };

    exports.delete = function(options){
        options = options || {};
        options.type = 'DELETE';
        _ajax(options);
    };

    exports.uploadFile = function(url, data, onload) {
        var payload = new FormData();
        for (var key in data) {
            payload.append(key, data[key]);
        }

        var request = new XMLHttpRequest();
        request.open('POST', url);
        //request.setRequestHeader(csrf.name, csrf.val);

        request.onload = function () {
            var response = request.response;
            if (typeof response === 'string') {
                response = JSON.parse(response);
            }

            onload && onload(response);
        };
        request.send(payload);
    };

    function _ajax(options){
        options.beforeSend = function(xhr){
            //xhr.setRequestHeader(csrf.name, csrf.val);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.setRequestHeader('Accept', 'application/json');
        };
        $.ajax(options);
    }

    function putAndPost(options, type) {
        $.ajax({
            url: options.url,
            type: type,
            data: JSON.stringify(options.data),
            dataType: 'json',
            beforeSend: function(xhr){
                //xhr.setRequestHeader(csrf.name, csrf.val);
                xhr.setRequestHeader('Accept', 'application/json');
                xhr.setRequestHeader('Content-Type', 'application/json');
            },
            success: options.success,
            error: options.error || console.log
        });
    }

})(window.Ajax = {});