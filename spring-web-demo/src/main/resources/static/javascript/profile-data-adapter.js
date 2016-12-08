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
}(jQuery));