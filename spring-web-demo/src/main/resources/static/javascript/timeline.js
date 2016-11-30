(function ($) {

    'use strict';

    var $container = $('.timeline-container');
    var $canvasContainer = $container.find('.timeline-canvas-container');
    var $canvas = $container.find('.timeline-canvas');
    var $rowPrototype = $($container.find('.timeline-template').html());

    var dataAdapter = VirtualScroller.DataAdapter({
        loadRange: function (range) {
            console.log('get range', range);
            return $.ajax({
                url: 'api/users/1/timeline/posts?offset=' + range.start + '&limit=' + (range.end - range.start) + 1,
                method: 'GET'
            });
        }
    });


    VirtualScroller({
        container: $container,
        canvasContainer: $canvasContainer,
        canvas: $canvas,
        rowHeight: 200,
        rowCount: 100,
        dynamicRowHeight: true,
        preRenderRows: 5,
        renderRow: function (params) {
            var $row = $rowPrototype.clone();
            $row.find('.media-heading').first().text('Loading...');
            dataAdapter.get({index: params.rowIndex}).then(function (item) {
                $row.find('.media-heading').first().text(item.content);
            }).always(function () {
                // faster clean up (garbage collector)
                $row = null;
            });
            return $row;
        }
    });




    /*
        Create posts
    */

    var $createPostInput = $('#create-post-input');
    var $createPostButton = $('#create-post-button');
    var createPost = function () {
        var content = $createPostInput.val();
        console.log('content:', content);
        if (!content) {
            return;
        }
        $createPostInput.val('');
        return $.ajax({
            url: 'api/users/1/timeline/posts',
            method: 'POST',
            data: escape(content)
        }).fail(function () {
            alert('Ups, that didn\'t work. Please try again.');
            $createPostInput.val(content);
        });
    };

    $createPostInput.on('keydown', function (event) {
        var keyCode = event.keyCode || event.which || 0;
        if (keyCode === 13)  { // Enter
            createPost();
        }
    });

    $createPostButton.on('click', createPost);

}(jQuery));