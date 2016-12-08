(function ($) {
    window.dataAdapter = VirtualScroller.DataAdapter({
        loadRange: function (range) {
            return $.ajax({
                url: 'api/timeline/posts?offset=' + range.start + '&limit=' + (range.end - range.start) + 1,
                method: 'GET'
            });
        }
    });
}(jQuery));