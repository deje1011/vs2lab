(function ($) {
    window.dataAdapter = VirtualScroller.DataAdapter({
        // TODO: Get user id
        loadRange: function (range) {
            return $.ajax({
                url: 'api/users/1/timeline/posts?offset=' + range.start + '&limit=' + (range.end - range.start) + 1,
                method: 'GET'
            });
        }
    });

    $('body').on('click', "#follow-button", function (event) {
        console.log($(event.target).attr('user-id'));
        return $.ajax({
            url: 'api/users/1/follow/2',// + $(event.target).attr('user-id'),
            method: 'POST',
            contentType: 'application/json'
        }).then(function () {
           location.reload();
        });
    });

    $('body').on('click', "#unfollow-button", function (event) {
        console.log($(event.target).attr('user-id'));
        return $.ajax({
            url: 'api/users/1/unfollow/2',// + $(event.target).attr('user-id'),
            method: 'POST'
        }).then(function () {
            location.reload();
        });
    });

}(jQuery));