(function ($) {

    function getParameterByName (name, url) {
        if (!url) {
          url = window.location.href;
        }
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    if (location.pathname === 'searchFollowed' || location.pathname === 'searchFollowers') {
        var url = location.href;
        var username = getParameterByName('username', url);
        $("#searchUser").val(username);
    }

}(jQuery));
