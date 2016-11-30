(function ($) {

    'use strict';

    var $container = $('.timeline-scroll-container');
    var $canvas = $container.find('.timeline-canvas');
    var $helperCanvas = $container.find('.timeline-helper-canvas');
    var $rowPrototype = $($container.find('.timeline-template').html());

    VirtualScroller({
        container: $container,
        canvas: $canvas,
        rowHeight: 200,
        rowCount: 100,
        dynamicRowHeight: true,
        preRenderRows: 5,
        renderRow: function (params) {
            var $row = $rowPrototype.clone();
            $row.find('.media-heading').first().text(params.rowIndex);
            $helperCanvas.append($row); // force calculation of height
            return $row;
        }
    });


}(jQuery));