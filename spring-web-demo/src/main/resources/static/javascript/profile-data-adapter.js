(function ($) {
    window.dataAdapter = VirtualScroller.DataAdapter({
        // TODO: Get user id
        loadRange: function (range) {
            return $.ajax({
                url: 'api/current-user/timeline/posts?offset=' + range.start + '&limit=' + (range.end - range.start) + 1,
                method: 'GET'
            });
        }
    });

     window.countPosts = function () {
        return $.ajax({
            url: 'api/current-user/timeline/posts/count',
            method: 'GET'
        });
    };

    $('body').on('click', "#follow-button", function (event) {
        return $.ajax({
            url: 'api/current-user/follow/' + $('#profile-user-id').text(),
            method: 'POST',
            contentType: 'application/json'
        }).then(function () {
           location.reload();
        });
    });

    $('body').on('click', "#unfollow-button", function (event) {
        return $.ajax({
            url: 'api/current-user/unfollow/' + $('#profile-user-id').text(),
            method: 'POST'
        }).then(function () {
            location.reload();
        });
    });

}(jQuery));