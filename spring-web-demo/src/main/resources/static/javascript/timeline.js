(function ($) {

    'use strict';

    var $container = $('.timeline-container');
    var $canvasContainer = $container.find('.timeline-canvas-container');
    var $canvas = $container.find('.timeline-canvas');
    var $rowPrototype = $($container.find('.timeline-template').html());

    var dataAdapter = VirtualScroller.DataAdapter({
        loadRange: function (range) {
            return $.ajax({
                url: 'api/users/1/timeline/posts?offset=' + range.start + '&limit=' + (range.end - range.start) + 1,
                method: 'GET'
            });
        }
    });

    var virtualScroller = VirtualScroller({
          container: $container,
          canvasContainer: $canvasContainer,
          canvas: $canvas,
          rowHeight: 130,
          rowCount: 0,
          dynamicRowHeight: true,
          preRenderRows: 5,
          renderRow: function (params) {
              var $row = $rowPrototype.clone();
              $row.find('.timeline-post-content').text('Loading...');
              dataAdapter.get({index: params.rowIndex}).then(function (post) {
                  $row.attr('timeline-post-id', post.id);
                  $row.find('.timeline-post-content').text(post.content);
              }).always(function () {
                  // faster clean up (garbage collector)
                  $row = null;
              });
              return $row;
          }
    });

    var countPosts = function () {
        return $.ajax({
            url: 'api/users/1/timeline/posts/count',
            method: 'GET'
        });
    };

    var rerender = function () {
        dataAdapter.reset();
        return countPosts().then(function (rowCount) {
            virtualScroller.rerender({newRowCount: rowCount});
        });
    };

    var rerenderViewport = function () {
        dataAdapter.reset();
        return countPosts().then(function (rowCount) {
            virtualScroller.rerenderViewport({newRowCount: rowCount});
        });
    };


    /*
        Initial render / load initial data
    */
    rerender();



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
            data: content
        }).then(rerender).fail(function () {
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


    /*
        Delete Posts
    */

    $container.on('click', '.timeline-post-delete-button', function (event) {

        event.preventDefault();

        var $button = $(this);
        var id = parseInt($button.closest('[timeline-post-id]').attr('timeline-post-id'));
        var index = parseInt($button.closest('[virtual-scroller-index]').attr('virtual-scroller-index'));

        virtualScroller.removeRowAt({rowIndex: index});
        return $.ajax({
            url: 'api/posts/' + id,
            method: 'DELETE'
        }).fail(function () {
            alert('Ups, that didn\'t work. Please try again.');
            rerenderViewport();
        });

    });

}(jQuery));