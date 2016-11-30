window.VirtualScroller = (function ($, _) {

    var validateParameters =  (function () {

        var typeChecks = {
            number : _.isNumber,
            string : _.isString,
            object : _.isObject,
            array : _.isArray,
            function : _.isFunction,
            boolean: _.isBoolean,
            'undefined': _.isUndefined,
            'null': _.isNull,
            'any': function () {return true;}
        };

        return function (paramsToCheck, paramDescriptions) {

            if (paramDescriptions === undefined) {
                throw new Error('paramDescriptions is required');
            }

            if (paramsToCheck) {
                paramsToCheck = _.clone(paramsToCheck);
            } else {
                paramsToCheck = {};
            }

            _.each(_.keys(paramsToCheck), function (nameOfParam) {
                if (!paramDescriptions[nameOfParam]) {
                    console.warn('You passed an unexpected parameter:', nameOfParam);
                    console.trace();
                }
            });

            var paramNames = _.keys(paramDescriptions);
            _.each(paramNames, function (nameOfParam) {

                var description = paramDescriptions[nameOfParam];
                var value = paramsToCheck[nameOfParam];

                if (!description) {
                    console.warn('Invalid description of parameter. ', nameOfParam, ':', description);
                    return;
                }

                /*
                        NOTE:

                        setting the 'default' property (to undefined) makes the parameter optional:
                        validateParameters({}, {
                            foo: {type: 'undefined', default: undefined}
                        });

                        Only using type: 'undefined' still causes the parameter to be required but
                        allows the value to be undefined:
                        validateParameters({foo: undefined}, {
                            foo: {type: 'undefined'}
                        });
                    */

                description = _.defaults(description || {}, {
                    type : undefined,
                    customCheck : undefined
                });

                // normalize description.type to always be an array (or undefined)
                if (_.isString(description.type)) {
                    description.type = [].concat(description.type.toLowerCase());
                }

                if (value === undefined) {
                    if (description.hasOwnProperty('default')) {
                        paramsToCheck[nameOfParam] = description.default;
                        value = description.default;
                    } else if (_.includes(description.type, 'undefined') === false) {
                        console.error('invalid parameters', paramsToCheck);
                        throw new Error(nameOfParam + ' is required');
                    }
                }

                if (description.type) {

                    var isValueOfRightType = _.some(description.type, function (typeString) {
                        if (typeChecks[typeString] === undefined) {
                            console.error('error validating', nameOfParam, 'with value:', value);
                            throw new Error('Unsupported type: ' + typeString);
                        }
                        return typeChecks[typeString](value);
                    });

                    if (isValueOfRightType === false) {
                        console.error('error validating', nameOfParam, 'with value:', value);
                        throw new Error(
                            nameOfParam + ' is not of type ' + description.type.join(' or ')
                        );
                    }
                }

                if (description.customCheck && description.customCheck(value) === false) {
                    throw new Error(nameOfParam + ' did not pass custom check');
                }
            });

            return paramsToCheck;

        };
    }());

    var RenderManager = function (config) {

        var renderManager = {
            id: _.uniqueId()
        };
        renderManager.state = {};
        renderManager.rowStates = []; // rowIndex => rowState
        renderManager.sizeCache = {
            rows: [],
            columns: []
        };

        // For items we need to know the outer boundries in order to calculate how many items
        // fit into the container. However, for the container itself we want to use the inner boundries
        // in order totake paddings into account.
        var DETECT_ITEM_HEIGHT_FN = 'outerHeight';
        var DETECT_ITEM_WIDTH_FN = 'outerWidth';
        var DETECT_CONTAINER_HEIGHT_FN = 'height';
        var DETECT_CONTAINER_WIDTH_FN = 'width';

        var FOLLOW_X_CLASS = 'virtual-scroller-follow-x';
        var FOLLOW_Y_CLASS = 'virtual-scroller-follow-y';
        var FOLLOW_BOTH_CLASS = 'virtual-scroller-follow-both';

        renderManager.stickyState = {};
        renderManager.stickyRowStates = [];
        renderManager.stickyColumnState = {};
        renderManager.stickyColumnRowStates = [];
        renderManager.fixedState = {};
        renderManager.fixedRowStates = [];

        /*
            Set translation for class names instead of dom-elements, so other elements can hook in by
            just adding the class name
        */
        var styleElement = $('<style>')
            .attr('created-by-virtual-scroller', '')
            .attr('inserted-by', 'virtualScroller:renderManager');
        styleElement.appendTo(config.container);
        var updateStyles = function (params) {
            params = validateParameters(params, {
                translateX: {type: 'number'},
                translateY: {type: 'number'}
            });
            styleElement.text(
                '.' + FOLLOW_X_CLASS + ' {' +
                ' transform: translate3d(' + params.translateX + 'px, 0, 0);' +
                '}' +
                '.' + FOLLOW_Y_CLASS + ' {' +
                ' transform: translate3d(0, ' + params.translateY + 'px, 0);' +
                '}' +
                '.' + FOLLOW_BOTH_CLASS + ' {' +
                ' transform: translate3d(' + params.translateX + 'px, ' + params.translateY + 'px, 0);' +
                '}'
            );
        };

        config.canvas.addClass(FOLLOW_BOTH_CLASS);
        config.stickyColumnsCanvas && config.stickyColumnsCanvas.addClass(FOLLOW_Y_CLASS);
        config.stickyRowsCanvas && config.stickyRowsCanvas.addClass(FOLLOW_X_CLASS);

        var removeFromDom = function (params) {
            params = validateParameters(params, {
                item: {type: 'object', customCheck: function (item) {
                    return item instanceof jQuery;
                }},
                rowIndex: {type: 'number'},
                columnIndex: {type: ['number', 'undefined'], default: undefined}
            });
            var shouldBeRemoved;
            if (params.columnIndex === undefined) {
                shouldBeRemoved = config.onBeforeRowRemoved({
                    row: params.item,
                    rowIndex: params.rowIndex
                });
            } else {
                shouldBeRemoved = config.onBeforeColumnRemoved({
                    column: params.item,
                    rowIndex: params.rowIndex,
                    columnIndex: params.columnIndex
                });
            }
            if (shouldBeRemoved !== false) {
                params.item.remove();
            }
        };

        var howManyItemsFitIn = function (params) {

            params = validateParameters(params, {
                sizeCache: {type: 'array'},
                itemSize: {type: 'number'},
                containerSize: {type: 'number'},
                startIndex: {type: 'number'}
            });

            var index = params.startIndex;
            var total = 0;
            var sizeSum = 0;

            while (sizeSum < params.containerSize) {
                sizeSum += params.sizeCache[index] || params.itemSize;
                index += 1;
                total += 1;
            }

            return total;
        };

        var sumSizes = function (params) {

            params = validateParameters(params, {
                startIndex: {type: 'number'},
                itemSize: {type: 'number'},
                amount: {type: 'number'},
                sizeCache: {type: 'array'}
            });

            var allSizes = _.map(new Array(params.amount), function (undef, index) {
                index += params.startIndex;
                return params.sizeCache[index] || params.itemSize;
            });

            return _.reduce(allSizes, function (sum, size) {
                return sum + size;
            }, 0);
        };

        var updateState = function (params) {

            params = validateParameters(params, {
                oldState: {type: ['object'], default: {}},
                sizeCache: {type: 'array', default: []},
                scrollOffset: {type: 'number'},
                itemSize: {type: 'number'},
                itemCount: {type: 'number'},
                preRender: {type: 'number'},
                containerSize: {type: 'number'},
                renderItem: {type: 'function'}, // should return {item: jQuery, size: Number}
                dynamicItemSize: {type: 'boolean', default: false}
            });

            params.oldState.children = params.oldState.children || [];

            var sizeCache = _.clone(params.sizeCache);
            var oldSizeCache = _.clone(sizeCache);

            var numberOfItemsNotToRender;
            var numberOfItemsToRender;

            /*
                Normalize scrollOffset
                * can't be < 0
                * can't be bigger than the summed size of all items - containerSize

                The last case happens if the user scrolls to the end of the container and
                the item size decreases. The browser keeps the scroll position and increases
                the scrollWidth/scrollHeight of the container.
            */
            params.scrollOffset = Math.max(
                0,
                Math.min(
                    sumSizes({
                        startIndex: 0,
                        amount: params.itemCount,
                        sizeCache: sizeCache,
                        itemSize: params.itemSize
                    }) - params.containerSize,
                    params.scrollOffset
                )
            );
            params.preRender = params.preRender || 0;

            if (params.dynamicItemSize) {
                numberOfItemsNotToRender = howManyItemsFitIn({
                    startIndex: 0,
                    containerSize: params.scrollOffset,
                    sizeCache: sizeCache,
                    itemSize: params.itemSize
                });
                numberOfItemsToRender = howManyItemsFitIn({
                    startIndex: numberOfItemsNotToRender,
                    containerSize: params.containerSize,
                    sizeCache: sizeCache,
                    itemSize: params.itemSize
                });
            } else {
                numberOfItemsNotToRender = Math.floor(params.scrollOffset / params.itemSize);
                numberOfItemsToRender = Math.ceil(params.containerSize / params.itemSize);
            }

            var preRenderTop = Math.min(
                params.preRender,
                numberOfItemsNotToRender
            );
            var preRenderBottom = Math.min(
                params.preRender,
                params.itemCount - numberOfItemsNotToRender - numberOfItemsToRender
            );
            numberOfItemsNotToRender -= preRenderTop;
            numberOfItemsToRender += preRenderTop + preRenderBottom;

            var newChildren = _.map(new Array(numberOfItemsToRender), function (undef, domIndex) {
                var index = domIndex + numberOfItemsNotToRender;
                var existingChild = params.oldState.children[index];

                if (existingChild) {
                    return existingChild;
                }

                var itemContainer = params.renderItem({index: index});
                itemContainer = validateParameters(itemContainer, {
                    item: {type: 'object', customCheck: function (item) {
                        return item instanceof jQuery;
                    }},
                    size: {type: 'number', default: params.itemSize}
                });
                var item = itemContainer.item;
                item.attr('virtual-scroller-index', index);
                if (params.dynamicItemSize) {
                    sizeCache[index] = itemContainer.size;
                }
                return item;
            });

            // fill up children array to have correct indices
            newChildren = (new Array(numberOfItemsNotToRender)).concat(newChildren);

            var canvasSize;
            var offset;

            if (params.dynamicItemSize) {
                canvasSize = sumSizes({
                    startIndex: 0,
                    amount: params.itemCount,
                    sizeCache: sizeCache,
                    itemSize: params.itemSize
                });
                offset = sumSizes({
                    startIndex: 0,
                    amount: numberOfItemsNotToRender,
                    sizeCache: sizeCache,
                    itemSize: params.itemSize
                });
            } else {
                canvasSize = params.itemCount * params.itemSize
                offset = numberOfItemsNotToRender * params.itemSize;
            }

            /*
                Adjust offset if we rendered new items on top who's size was unknown
                before and that are bigger than expected. Usually that would push down the items
                the user can see at this moment. So we have to adjust the scrollOffset to reflect that
                and keep the items at the same position.
            */
            var adjustOffset = 0;
            if (params.dynamicItemSize) {
                adjustOffset = _.reduce(new Array(preRenderTop), function (total, undef, index) {

                    // start at first rendered item
                    index += numberOfItemsNotToRender;

                    // child is not new, was already rendered before
                    if (oldSizeCache[index]) {
                        return total;
                    }

                    total += sizeCache[index] - params.itemSize;
                    return total;
                }, 0);
            }

            // new state
            return {
                state: {
                    children: newChildren,
                    offset: offset,
                    scrollOffset: params.scrollOffset + adjustOffset,
                    adjustedScrollOffsetBy: adjustOffset,
                    canvasSize: canvasSize
                },
                sizeCache: sizeCache,
            };
        };

        (function updateStateTests () {

            if (typeof window.describeFn === 'undefined') {
                return;
            }

            var defaultParams = {
                oldState: {},
                scrollOffset: 0,
                containerSize: 100,
                itemSize: 20,
                itemCount: 100,
                preRender: 0,
                renderItem: function () {
                    return {
                        item: $('<div>')
                    };
                }
            };

            describeFn({
                fn: updateState,
                fnName: 'updateState',
                tests: {
                    'correct state for initial call': {
                        params: _.clone(defaultParams),
                        transformResult: function (result) {
                            return {
                                childrenCount: _.compact(result.state.children).length,
                                offset: result.state.offset,
                                canvasSize: result.state.canvasSize
                            };
                        },
                        result: {
                            equals: {
                                childrenCount: 5,
                                offset: 0,
                                canvasSize: 2000
                            }
                        }
                    },
                    'correct state after scrolling': {
                        params: _.extend(_.clone(defaultParams), {
                            scrollOffset: 100
                        }),
                        transformResult: function (result) {
                            return {
                                childrenCount: _.compact(result.state.children).length,
                                offset: result.state.offset,
                                canvasSize: result.state.canvasSize
                            };
                        },
                        result: {
                            equals: {
                                childrenCount: 5,
                                offset: 100,
                                canvasSize: 2000
                            }
                        }
                    },
                    'pre-renders the correct amount of items for the initial call': {
                        params: _.extend(_.clone(defaultParams), {preRender: 2}),
                        transformResult: function (result) {
                            return _.compact(result.state.children).length;
                        },
                        result: {
                            equals: 7
                        }
                    },
                    'pre-renders the correct amount of items after scrolling': {
                        params: _.extend(_.clone(defaultParams), {
                            preRender: 2,
                            scrollOffset: 100
                        }),
                        transformResult: function (result) {
                            return _.compact(result.state.children).length;
                        },
                        result: {
                            equals: 9
                        }
                    },
                    'can handle dynamic item sizes for initial call': {
                        params: _.extend(_.clone(defaultParams), {
                            dynamicItemSize: true,
                            renderItem: function (params) {
                                var height = (params.index + 1) * 10;
                                return {
                                    item: $('<div>').height(height),
                                    size: height
                                };
                            }
                        }),
                        transformResult: function (result) {
                            return {
                                childrenCount: _.compact(result.state.children).length,
                                offset: result.state.offset,
                                canvasSize: result.state.canvasSize
                            };
                        },
                        result: {
                            equals: {
                                /*
                                    Even though, 4 items would be enough to fill the containerSize,
                                    the scroller will render 5 items because he does not know anything
                                    about the itemSize before rendering the items.
                                    If we would pass in a heightCache with the correct heights already
                                    set, the scroller would only render 4.
                                */
                                childrenCount: 5,
                                offset: 0,
                                canvasSize: (10 + 20 + 30 + 40 + 50) + (20 * 95) // 95 = itemSize * (itemCount - childrenCount)
                            }
                        }
                    },
                    'can handle dynamic item sizes after scrolling': {
                        params: _.extend(_.clone(defaultParams), {
                            dynamicItemSize: true,
                            scrollOffset: 100,
                            renderItem: function (params) {
                                var height = (params.index + 1) * 10;
                                return {
                                    item: $('<div>').height(height),
                                    size: height
                                };
                            }
                        }),
                        transformResult: function (result) {
                            return {
                                childrenCount: _.compact(result.state.children).length,
                                offset: result.state.offset,
                                canvasSize: result.state.canvasSize
                            };
                        },
                        result: {
                            equals: {
                                /*
                                    Even though, 2 items would be enough to fill the containerSize,
                                    the scroller will render 5 items because he does not know anything
                                    about the itemSize before rendering the items.
                                    If we would pass in a heightCache with the correct heights already
                                    set, the scroller would only render 2.
                                */
                                childrenCount: 5,
                                offset: 100,
                                canvasSize: (60 + 70 + 80 + 90 + 100) + (20 * 95) // 90 = itemSize * (itemCount - childrenCount)
                            }
                        }
                    },
                    'uses a given size cache': {
                        params: _.extend(_.clone(defaultParams), {
                            dynamicItemSize: true,
                            renderItem: function (params) {
                                var height = (params.index + 1) * 10;
                                return {
                                    item: $('<div>').height(height),
                                    size: height
                                };
                            },
                            sizeCache: [10, 20, 30, 40, 50]
                        }),
                        transformResult: function (result) {
                            return {
                                childrenCount: _.compact(result.state.children).length,
                                offset: result.state.offset,
                                canvasSize: result.state.canvasSize
                            };
                        },
                        result: {
                            equals: {
                                childrenCount: 4,
                                offset: 0,
                                canvasSize: (10 + 20 + 30 + 40 + 50) + (20 * 95) // 95 = itemSize * (itemCount - sizeCache.length)
                            }
                        }
                    },
                    // only for items that are rendered on top/on the left of the current items
                    'adjust scrollOffset if a pre-rendered size is bigger than expected': {
                        params: _.extend(_.clone(defaultParams), {
                            dynamicItemSize: true,
                            scrollOffset: 100,
                            preRender: 1,
                            renderItem: function (params) {
                                var height = 30; // 10 more than itemSize
                                return {
                                    item: $('<div>').height(height),
                                    size: height
                                };
                            },
                            oldState: {scrollOffset: 100},
                            sizeCache: [undefined, undefined, undefined, undefined, undefined,
                            20, 20, 20, 20, 20]
                        }),
                        transformResult: function (result) {
                            return {
                                scrollOffset: result.state.scrollOffset,
                                adjustedScrollOffsetBy: result.state.adjustedScrollOffsetBy
                            };
                        },
                        result: {
                            equals: {
                                scrollOffset: 110,
                                adjustedScrollOffsetBy: 10
                            }
                        }
                    }
                }
            }).then(function (results) {
                console.log(JSON.stringify(results, null, 2));
            });
        }());


        var renderChildren = function (params) {

            params = validateParameters(params, {
                canvas: {type: 'object'},
                newChildren: {type: 'array'},
                oldChildren: {type: 'array', default: []},
                /*
                    Required if you want to render columns. We need this information in removeFromDom.
                */
                rowIndex: {type: ['number', 'undefined'], default: undefined}
            });

            var oldStartIndex = params.oldChildren.length - _.compact(params.oldChildren).length;
            var removedIndices = [];

            _.each(params.oldChildren, function (child, index) {
                if (!child) {
                    return;
                }
                if (_.indexOf(params.newChildren, child) === -1) {
                    removeFromDom({
                        item: child,
                        rowIndex: params.rowIndex === undefined ? index : params.rowIndex,
                        columnIndex: params.rowIndex === undefined ? undefined : index
                    });
                    removedIndices.push(index);
                }
            });

            var prepend = [];
            var append = [];

            _.each(params.newChildren, function (child, index) {
                if (!child) {
                    return;
                }
                if (_.indexOf(params.oldChildren, child) === -1) {
                    if (index < oldStartIndex) {
                        prepend.push(child);
                    } else {
                        append.push(child);
                    }
                }
            });

            params.canvas.prepend(prepend).append(append);
            return removedIndices;
        };

        var renderStateY = function (canvas, scrollCanvas, state, oldState) {
            if (oldState.canvasSize !== state.canvasSize) {
                canvas
                    .add(scrollCanvas)
                    .add(config.stickyColumnsCanvas)
                    .css({
                        height: state.canvasSize + 'px',
                        minHeight: state.canvasSize + 'px'
                    });
            }
            var removedIndices = renderChildren({
                canvas: canvas,
                newChildren: state.children,
                oldChildren: oldState.children
            });
            _.each(removedIndices, function (index) {
                renderManager.rowStates[index] = undefined;
                renderManager.stickyColumnRowStates[index] = undefined;
            });
            return canvas;
        };

        var renderStateX = function (row, state, oldState, rowIndex) {
            var canvas = config.columnCanvasSelector ? row.find(config.columnCanvasSelector) : row;

            renderChildren({
                canvas: canvas,
                newChildren: state.children,
                oldChildren: oldState.children,
                rowIndex: rowIndex
            });

            return row;
        };

        var _renderRow = function (params) {

            params = validateParameters(params, {
                rowIndex: {type: 'number'}
            });

            var row = config.renderRow({
                rowIndex: params.rowIndex
            });

            var height = config.rowHeight;

            /*
                As long as dom elements are not attached, their dimensions cannot be determined.
                So we append it to the canvas before we read the height (it has to be appended to
                the canvas or a clone of it because the dimensions of the element might change
                depending on the dimensions of the canvas).
            */
            if (config.dynamicRowHeight) {
                if (!config.rowHeightIsAccurate) {
                    row.appendTo(config.canvas);
                }
                height = row[DETECT_ITEM_HEIGHT_FN]();
            }

            return {item: row, size: height};
        };

        var _renderColumn = function (params) {

            params = validateParameters(params, {
                rowIndex: {type: 'number'},
                columnIndex: {type: 'number'},
                row: {type: 'object'}
            });

            var column = config.renderColumn({
                rowIndex: params.rowIndex,
                columnIndex: params.columnIndex
            });

            var width = config.columnWidth;
            /*
                As long as dom elements are not attached, their dimensions cannot
                be determined. So we append it to the row before we read the height
                (it has to be appended to the row or a clone of it because the dimensions
                of the element might change depending on the dimensions of the row).
            */
            if (config.dynamicColumnWidth) {
                if (!config.columnWidthIsAccurate) {
                    params.row.append(column);
                }
                width = column[DETECT_ITEM_WIDTH_FN]();
            }

            return {item: column, size: width};
        };

        renderManager.render = function () {

            var sumRowSizes = function (amount) {
                if (config.dynamicRowHeight === false) {
                    return amount * config.rowHeight;
                }
                return sumSizes({
                    startIndex: 0,
                    amount: amount,
                    sizeCache: renderManager.sizeCache.rows,
                    itemSize: config.rowHeight
                });
            };

            var sumColumnSizes = function (amount) {
                if (config.dynamicColumnWidth === false) {
                    return amount * config.columnWidth;
                }
                return sumSizes({
                    startIndex: 0,
                    amount: amount,
                    sizeCache: renderManager.sizeCache.columns,
                    itemSize: config.columnWidth
                });
            };

            if (config.stickyRows > 0) {
                var oldStickyState = renderManager.stickyState;
                var updateStickyStateResult = updateState({
                    oldState: oldStickyState,
                    sizeCache: renderManager.sizeCache.rows,
                    containerSize: sumRowSizes(config.stickyRows),
                    scrollOffset: 0,
                    itemCount: config.stickyRows,
                    itemSize: config.rowHeight,
                    dynamicItemSize: config.dynamicRowHeight,
                    preRender: 0,
                    renderItem: function (params) {
                        return _renderRow({rowIndex: params.index});
                    }
                });
                renderManager.stickyState = updateStickyStateResult.state;
                renderManager.sizeCache.rows = updateStickyStateResult.sizeCache;
                renderStateY(config.stickyRowsCanvas, config.stickyRowsCanvas, renderManager.stickyState, oldStickyState);
            }

            var scrollTop = config.scrollContainer.scrollTop();
            var scrollContainerHeight = config.scrollContainer[DETECT_CONTAINER_HEIGHT_FN]();
            var scrollLeft; // only set if needed
            var scrollContainerWidth; // only set if needed


            var oldState = renderManager.state;
            var updateStateResult = updateState({
                oldState: oldState,
                sizeCache: renderManager.sizeCache.rows,
                containerSize: scrollContainerHeight,
                scrollOffset: scrollTop,
                itemCount: config.rowCount,
                itemSize: config.rowHeight,
                dynamicItemSize: config.dynamicRowHeight,
                preRender: config.preRenderRows,
                renderItem: function (params) {
                    if (config.stickyRows > params.index) {
                        var size = renderManager.sizeCache.rows[params.index] || config.rowHeight;
                        return {
                            item: $('<div>').css('height', size),
                            size: size
                        };
                    }

                    return _renderRow({rowIndex: params.index});
                }
            });

            renderManager.state = updateStateResult.state;
            renderManager.sizeCache.rows = updateStateResult.sizeCache;

            if (config.renderColumn) {

                scrollLeft = config.scrollContainer.scrollLeft();
                scrollContainerWidth = config.scrollContainer[DETECT_CONTAINER_WIDTH_FN]();

                if (config.stickyColumns > 0) {
                    var oldStickyColumnState = renderManager.stickyColumnState;
                    var updateStickyColumnStateResult = updateState({
                        oldState: oldStickyColumnState,
                        sizeCache: renderManager.sizeCache.rows,
                        containerSize: scrollContainerHeight,
                        scrollOffset: scrollTop,
                        itemCount: config.rowCount,
                        itemSize: config.rowHeight,
                        dynamicItemSize: config.dynamicRowHeight,
                        preRender: config.preRenderRows,
                        renderItem: function (params) {
                            if (config.stickyRows > params.index) {
                                var size = renderManager.sizeCache.rows[params.index] || config.rowHeight;
                                return {
                                    item: $('<div>').css('height', size),
                                    size: size
                                };
                            }
                            return _renderRow({rowIndex: params.index});
                        }
                    });
                    renderManager.sizeCache.rows = updateStickyColumnStateResult.sizeCache;
                    renderManager.stickyColumnState = updateStickyColumnStateResult.state;
                    renderStateY(config.stickyColumnsCanvas, config.stickyColumnsCanvas, renderManager.stickyColumnState, oldStickyColumnState);

                    renderManager.stickyColumnState.children = _.map(
                        renderManager.stickyColumnState.children,
                        function (child, rowIndex) {
                            if (!child) {
                                return child;
                            }
                            var oldStickyColumnRowState = renderManager.stickyColumnRowStates[rowIndex] || {};
                            var updateStickyColumnRowStateResult = updateState({
                                oldState: oldStickyColumnRowState,
                                sizeCache: renderManager.sizeCache.columns,
                                containerSize: sumColumnSizes(config.stickyColumns),
                                scrollOffset: 0,
                                itemCount: config.stickyColumns,
                                itemSize: config.columnWidth,
                                dynamicItemSize: config.dynamicColumnWidth,
                                preRender: 0,
                                renderItem: function (params) {
                                    if (config.stickyRows > rowIndex) {
                                        var size = renderManager.sizeCache.columns[params.index] || config.columnWidth;
                                        return {
                                            item: $('<div>').css('width', size),
                                            size: size
                                        };
                                    }
                                    return _renderColumn({
                                        rowIndex: rowIndex,
                                        columnIndex: params.index,
                                        row: child
                                    });
                                }
                            });
                            renderManager.stickyColumnRowStates[rowIndex] = updateStickyColumnRowStateResult.state;
                            renderManager.sizeCache.columns = updateStickyColumnRowStateResult.sizeCache;
                            renderStateX(child, renderManager.stickyColumnRowStates[rowIndex], oldStickyColumnRowState, rowIndex);
                            return child;
                        }
                    );
                }

                if (config.stickyRows > 0) {
                    renderManager.stickyState.children = _.map(
                        renderManager.stickyState.children,
                        function (child, rowIndex) {
                            var oldStickyRowState = renderManager.stickyRowStates[rowIndex] || {};
                            var updateStickyRowStateResult = updateState({
                                oldState: oldStickyRowState,
                                sizeCache: renderManager.sizeCache.columns,
                                containerSize: scrollContainerWidth,
                                scrollOffset: scrollLeft,
                                itemCount: config.columnCount,
                                itemSize: config.columnWidth,
                                dynamicItemSize: config.dynamicColumnWidth,
                                preRender: config.preRenderColumns,
                                renderItem: function (params) {
                                    if (config.stickyColumns > params.index) {
                                        var size = renderManager.sizeCache.columns[params.index] || config.columnWidth;
                                        return {
                                            item: $('<div>').css('width', size),
                                            size: size
                                        };
                                    }
                                    return _renderColumn({
                                        rowIndex: rowIndex,
                                        columnIndex: params.index,
                                        row: child
                                    });
                                }
                            });
                            renderManager.stickyRowStates[rowIndex] = updateStickyRowStateResult.state;
                            renderManager.sizeCache.columns = updateStickyRowStateResult.sizeCache;
                            renderStateX(child, renderManager.stickyRowStates[rowIndex], oldStickyRowState, rowIndex);
                            return child;
                        }
                    );
                }

                if (config.stickyRows > 0 && config.stickyColumns > 0) {
                    var oldFixedState = renderManager.fixedState;
                    var updateFixedStateResult = updateState({
                        oldState: oldFixedState,
                        sizeCache: renderManager.sizeCache.rows,
                        containerSize: sumRowSizes(config.stickyRows),
                        scrollOffset: 0,
                        itemCount: config.stickyRows,
                        itemSize: config.rowHeight,
                        dynamicItemSize: config.dynamicRowHeight,
                        preRender: 0,
                        renderItem: function (params) {
                            return _renderRow({rowIndex: params.index});
                        }
                    });
                    renderManager.fixedState = updateFixedStateResult.state;
                    renderManager.sizeCache.rows = updateFixedStateResult.sizeCache;

                    renderManager.fixedState.children = _.map(
                        renderManager.fixedState.children,
                        function (child, rowIndex) {
                            var oldFixedRowState = renderManager.fixedRowStates[rowIndex] || {};
                            var updateStateResult = updateState({
                                oldState: oldFixedRowState,
                                sizeCache: renderManager.sizeCache.columns,
                                containerSize: sumColumnSizes(config.stickyColumns),
                                scrollOffset: 0,
                                itemCount: config.stickyColumns,
                                itemSize: config.columnWidth,
                                dynamicItemSize: config.dynamicColumnWidth,
                                preRender: 0,
                                renderItem: function (params) {
                                    return _renderColumn({
                                        rowIndex: rowIndex,
                                        columnIndex: params.index,
                                        row: child
                                    });
                                }
                            });
                            renderManager.fixedRowStates[rowIndex] = updateStateResult.state;
                            renderManager.sizeCache.columns = updateStateResult.sizeCache;
                            renderStateX(child, renderManager.fixedRowStates[rowIndex], oldFixedRowState, rowIndex);
                            return child;
                        }
                    );

                    renderStateY(config.fixedCanvas, config.fixedCanvas, renderManager.fixedState, oldFixedState);
                }

                var someOldChildState = _.last(_.compact(renderManager.rowStates));

                renderManager.state.children = _.map(
                    renderManager.state.children,
                    function (child, rowIndex) {
                        if (!child) {
                            return child;
                        }
                        var oldRowState = renderManager.rowStates[rowIndex] || {};
                        var updateRowStateResult = updateState({
                            oldState: oldRowState,
                            sizeCache: renderManager.sizeCache.columns,
                            containerSize: scrollContainerWidth,
                            scrollOffset: scrollLeft,
                            itemCount: config.columnCount,
                            itemSize: config.columnWidth,
                            dynamicItemSize: config.dynamicColumnWidth,
                            preRender: config.preRenderColumns,
                            renderItem: function (params) {
                                if (config.stickyColumns > params.index || config.stickyRows > rowIndex) {
                                    var size = renderManager.sizeCache.columns[params.index] || config.columnWidth;
                                    return {
                                        item: $('<div>').css('width', size),
                                        size: size
                                    };
                                }
                                return _renderColumn({
                                    rowIndex: rowIndex,
                                    columnIndex: params.index,
                                    row: child
                                });
                            }
                        });
                        renderManager.rowStates[rowIndex] = updateRowStateResult.state;
                        renderManager.sizeCache.columns = updateRowStateResult.sizeCache;
                        renderStateX(child, renderManager.rowStates[rowIndex], oldRowState, rowIndex);
                        return child;
                    }
                );

                /*
                    We are using last instead of first here because the first rendered state might be
                    the one of a sticky row which might be filled with fake dom nodes and might have
                    wrong canvas size after the first rendering.
                */
                var someChildState = _.last(_.compact(renderManager.rowStates));
                if (!someOldChildState || someChildState.canvasSize !== someOldChildState.canvasSize) {
                    config.scrollCanvas
                        .add(config.canvas)
                        .add(config.stickyRowsCanvas)
                        .add(config.stickyCanvasContainer)
                        .css('width', someChildState.canvasSize);
                }

                if (someChildState.adjustedScrollOffsetBy > 0) {
                    config.scrollContainer.scrollLeft(scrollLeft + someChildState.adjustedScrollOffsetBy);
                }
            }

            renderStateY(config.canvas, config.scrollCanvas, renderManager.state, oldState);
            if (renderManager.state.adjustedScrollOffsetBy > 0) {
                config.scrollContainer.scrollTop(scrollTop + renderManager.state.adjustedScrollOffsetBy);
            }

            updateStyles({
                translateY: (renderManager.state.scrollOffset - renderManager.state.offset) * -1,
                translateX: someChildState ? (someChildState.scrollOffset - someChildState.offset) * -1 : 0
            });
        };


        renderManager.isRowRendered = function (params) {
            params = validateParameters(params, {
                rowIndex: {type: 'number'}
            });
            return !!(config.stickyRows > params.rowIndex || renderManager.state.children[params.rowIndex]);
        };

        renderManager.isColumnRendered = function (params) {
            params = validateParameters(params, {
                rowIndex: {type: 'number', default: _.findIndex(renderManager.rowStates, function (state) {
                    return !!state;
                })},
                columnIndex: {type: 'number'}
            });
            var rowState = renderManager.rowStates[params.rowIndex];
            return !!(
                config.stickyRows > params.rowIndex ||
                config.stickyColumns > params.columnIndex ||
                (rowState && rowState.children[params.columnIndex])
            );
        };

        renderManager.reevaluateRowSizeAtIndex = function (params) {

            params = validateParameters(params, {
                rowIndex: {type: 'number'}
            });

            if (!config.dynamicRowHeight) {
                return;
            }

            var rowElement = renderManager.state.children && renderManager.state.children[params.rowIndex];
            if (rowElement === undefined) {
                return;
            }
            renderManager.sizeCache.rows[params.rowIndex] = rowElement.height();
        };

        /*
            Either updates the width of a specific column at the given rowIndex or
            all columns at the given columnIndex in every row.
        */
        renderManager.reevaluateColumnSizeAtIndex = function (params) {

            params = validateParameters(params, {
                rowIndex: {type: ['number', 'undefined'], default: undefined},
                columnIndex: {type: 'number'}
            });

            if (!config.dynamicColumnWidth) {
                return;
            }

            var lastRenderedRowState = _.last(_.compact(renderManager.rowStates));
            var lastRenderedColumn = _.last(_.compact(lastRenderedRowState.children));
            renderManager.sizeCache.columns[params.columnIndex] = lastRenderedColumn.width();
        };

        renderManager.reevaluateAllSizes = function () {

            if (config.dynamicRowHeight) {
                renderManager.sizeCache.rows = _.map(
                    renderManager.sizeCache.rows,
                    function (size, index) {
                        var child = renderManager.state.children[index];
                        if (!child) {
                            return size;
                        }
                        return child[DETECT_ITEM_HEIGHT_FN]();
                    }
                );
            }

            if (config.dynamicColumnWidth) {
                var lastRenderedRowState = _.last(_.compact(renderManager.rowStates));
                renderManager.sizeCache.columns = _.map(
                    renderManager.sizeCache.columns,
                    function (size, index) {
                        var child = lastRenderedRowState.children[index];
                        if (!child) {
                            return size;
                        }
                        return child[DETECT_ITEM_WIDTH_FN]();
                    }
                );
            }

        };

        renderManager.getScrollTopForRowIndex = function (params) {
            params = validateParameters(params, {
                rowIndex: {type: 'number', customCheck: function (value) {
                    return value >= 0;
                }}
            });
            return sumSizes({
                startIndex: 0,
                amount: params.rowIndex,
                sizeCache: renderManager.sizeCache.rows,
                itemSize: config.rowHeight
            });
        };

        renderManager.getScrollLeftForColumnIndex = function (params) {
            params = validateParameters(params, {
                rowIndex: {type: 'number', default: 0, customCheck: function (value) {
                    return value >= 0;
                }},
                columnIndex: {type: 'number', customCheck: function (value) {
                    return value >= 0;
                }},
            });

            return sumSizes({
                startIndex: 0,
                amount: params.columnIndex,
                sizeCache: renderManager.sizeCache.columns,
                itemSize: config.columnWidth
            });
        };

        renderManager.resetState = function () {
            var removeChildAtIndex = function (child, index) {
                if (child) {
                    removeFromDom({
                        item: child,
                        rowIndex: index
                    });
                }
            };
            _.each(renderManager.state.children, removeChildAtIndex);
            _.each(renderManager.stickyState.children, removeChildAtIndex);
            _.each(renderManager.stickyColumnState.children, removeChildAtIndex);
            _.each(renderManager.fixedState.children, removeChildAtIndex);

            renderManager.sizeCache = {rows: [], columns: []};
            renderManager.state = {};
            renderManager.rowStates = [];
            renderManager.stickyState = {};
            renderManager.stickyRowStates = [];
            renderManager.stickyColumnState = {};
            renderManager.stickyColumnRowStates = [];
            renderManager.fixedState = {};
            renderManager.fixedRowStates = [];
        };

        renderManager.resetRenderedState = function () {

            /*
                Note: We don't reset the sizeCache for now because that would result in nasty jumps.
                The newly calculated offsets will be different from the previous ones and the user can't
                keep his scroll position.
            */

            var sizeCache = renderManager.sizeCache;
            renderManager.resetState();
            renderManager.sizeCache = sizeCache;
        };

        var rowOrColumnIndexIsSet = function (params) {
            return {
                rowIndex: {
                    type: ['number', 'undefined'],
                    default: undefined,
                    customCheck: function (rowIndex) {
                        return rowIndex === undefined || rowIndex >= 0;
                    }
                },
                columnIndex: {
                    type: ['number', 'undefined'],
                    default: undefined,
                    customCheck: function (columnIndex) {
                        // one of them must be undefined, one has to be set
                        return (params.rowIndex !== undefined || columnIndex !== undefined) &&
                            (params.rowIndex === undefined || columnIndex === undefined) &&
                            (columnIndex === undefined || columnIndex >= 0);
                    }
                }
            };
        };


        var updateIndexAttributes = function (children) {
            children = _.map(children, function (child, index) {
                if (child) {
                    child.attr('virtual-scroller-index', index);
                }
                return child;
            });
            config.onAfterIndicesChanged();
            return children;
        };

        renderManager.removeItemAt = function (params) {

            params = validateParameters(params, rowOrColumnIndexIsSet(params));

            if (params.rowIndex !== undefined) {

                var oldRowCount = config.rowCount;
                config.rowCount -= 1;

                var statesToRemoveRowFrom;
                var rowStatesToRemoveRowFrom;

                if (config.stickyRows > params.rowIndex) {
                    statesToRemoveRowFrom = [renderManager.stickyState, renderManager.fixedState];
                    rowStatesToRemoveRowFrom = [renderManager.stickyRowStates, renderManager.fixedRowStates];
                } else {
                    statesToRemoveRowFrom = [renderManager.state, renderManager.stickyColumnState];
                    rowStatesToRemoveRowFrom = [renderManager.rowStates, renderManager.stickyColumnRowStates];
                }

                _.each(statesToRemoveRowFrom, function (state) {
                    if (!state.children) {
                        return;
                    }
                    if (state.children[params.rowIndex]) {
                        removeFromDom({
                            item: state.children[params.rowIndex],
                            rowIndex: params.rowIndex
                        });
                    }
                    state.children.splice(params.rowIndex, 1);
                    state.children = updateIndexAttributes(state.children);
                });

                _.each(rowStatesToRemoveRowFrom, function (rowStates) {
                    rowStates.splice(params.rowIndex, 1);
                });

                if (config.dynamicRowHeight) {
                    renderManager.sizeCache.rows.splice(params.rowIndex, 1);
                }

                config.onAfterRowCountChanged({
                    oldRowCount: oldRowCount,
                    newRowCount: config.rowCount,
                });
            }
            else if (params.columnIndex !== undefined) {

                var oldColumnCount = config.columnCount;
                config.columnCount -= 1;

                var rowStatesToRemoveColumnFrom;

                if (config.stickyColumns > params.columnIndex) {
                    rowStatesToRemoveColumnFrom = ['fixedRowStates', 'stickyColumnRowStates'];
                } else {
                    rowStatesToRemoveColumnFrom = ['stickyRowStates', 'rowStates'];
                }

                _.each(rowStatesToRemoveColumnFrom, function (rowStatesName) {
                    renderManager[rowStatesName] = _.map(
                        renderManager[rowStatesName],
                        function (rowState, rowIndex) {
                            if (!rowState || !rowState.children) {
                                return rowState;
                            }
                            if (rowState.children[params.columnIndex]) {
                                removeFromDom({
                                    item: rowState.children[params.columnIndex],
                                    rowIndex: rowIndex,
                                    columnIndex: params.columnIndex
                                });
                            }
                            rowState.children.splice(params.columnIndex, 1);
                            rowState.children = updateIndexAttributes(rowState.children);
                            return rowState;
                        }
                    );
                });

                if (config.dynamicColumnWidth) {
                    renderManager.sizeCache.columns.splice(params.columnIndex, 1);
                }

                config.onAfterColumnCountChanged({
                    oldColumnCount: oldColumnCount,
                    newColumnCount: config.columnCount
                });

            }
        };

        renderManager.addItemAt = function (params) {

            params = validateParameters(params, rowOrColumnIndexIsSet(params));

            if (params.rowIndex !== undefined) {

                var oldRowCount = config.rowCount;
                config.rowCount += 1;

                /*
                    Adding a row as a sticky row would requires us to do a lot of delta management.
                    So we just rerender the viewport for now.
                */
                if (config.stickyRows > params.rowIndex) {
                    renderManager.resetRenderedState();
                    renderManager.render();
                }
                // Only render the row if it is inside the viewport
                else if (!renderManager.state.children[params.rowIndex]) {

                    // If it was added as the last row, we can just call render and it will show up
                    // because it will be appended
                    if (params.rowIndex === config.rowCount - 1) {
                        renderManager.render();
                    }
                }
                else {

                    var statePairsToAddRowTo = [{
                        state: renderManager.state,
                        rowStates: renderManager.rowStates,
                        canvas: config.canvas,
                        scrollLeft: config.scrollContainer.scrollLeft(),
                        containerSize: config.scrollContainer[DETECT_CONTAINER_WIDTH_FN](),
                        preRenderColumns: config.preRenderColumns,
                        columnCount: config.columnCount,
                        renderColumn: function (params) {
                            if (config.stickyColumns > params.columnIndex) {
                                var size = renderManager.sizeCache.columns[params.columnIndex] || config.columnWidth;
                                return {
                                    item: $('<div>').css('width', size),
                                    size: size
                                };
                            }
                            return _renderColumn({
                                rowIndex: params.rowIndex,
                                columnIndex: params.columnIndex,
                                row: params.row
                            });
                        }
                    }];
                    if (config.stickyColumns > 0) {
                        statePairsToAddRowTo.push({
                            state: renderManager.stickyColumnState,
                            rowStates: renderManager.stickyColumnRowStates,
                            canvas: config.stickyColumnsCanvas,
                            scrollLeft: 0,
                            containerSize: config.stickyColumnsCanvas[DETECT_ITEM_WIDTH_FN](),
                            preRenderColumns: 0,
                            columnCount: config.stickyColumns,
                            renderColumn: function (params) {
                                return _renderColumn({
                                    rowIndex: params.rowIndex,
                                    columnIndex: params.columnIndex,
                                    row: params.row
                                });
                            }
                        });
                    }

                    _.each(statePairsToAddRowTo, function (renderParams) {
                        var state = renderParams.state;
                        var rowStates = renderParams.rowStates;
                        var canvas = renderParams.canvas;

                        // Actually render the row and insert it
                        var row = _renderRow({rowIndex: params.rowIndex}).item;

                        state.children.splice(params.rowIndex, 0, row);
                        state.children = updateIndexAttributes(state.children);

                        if (state.children[params.rowIndex - 1]) {
                            row.insertAfter(state.children[params.rowIndex - 1]);
                        } else {
                            canvas.prepend(row);
                        }

                        // If the user specified how to render columns inside the row, go ahead and do that
                        if (config.renderColumn) {
                            // TODO: Unify with the calls in renderManager.render
                            var rowIndex = params.rowIndex;
                            var resultOfUpdateStateForRowState = updateState({
                                oldState: {},
                                sizeCache: renderManager.sizeCache.columns,
                                containerSize: renderParams.containerSize,
                                scrollOffset: renderParams.scrollLeft,
                                itemCount: renderParams.columnCount,
                                itemSize: config.columnWidth,
                                dynamicItemSize: config.dynamicColumnWidth,
                                preRender: renderParams.preRenderColumns,
                                renderItem: function(params) {
                                    return renderParams.renderColumn({
                                        row: row,
                                        rowIndex: rowIndex,
                                        columnIndex: params.index
                                    });
                                }
                            });
                            var rowState = resultOfUpdateStateForRowState.state;
                            renderManager.sizeCache.columns = resultOfUpdateStateForRowState.sizeCache;
                            rowStates.splice(params.rowIndex, 0, rowState);
                            renderStateX(row, rowState, {}, rowIndex);
                        }

                    });

                    if (config.dynamicRowHeight) {
                    renderManager.sizeCache.rows.splice(
                        params.rowIndex,
                        0,
                        renderManager.state.children[params.rowIndex][DETECT_ITEM_HEIGHT_FN]()
                    );
                }

                    renderManager.render(); // just to set the canvas size correctly
                }

                config.onAfterRowCountChanged({
                    oldRowCount: oldRowCount,
                    newRowCount: config.rowCount
                });

            } else if (params.columnIndex !== undefined) {

                var oldColumnCount = config.columnCount;
                config.columnCount += 1;

                /*
                    Adding a row as a sticky column would requires us to do a lot of delta management.
                    So we just rerender the viewport for now.
                */
                if (config.stickyColumns > params.columnIndex) {
                    renderManager.resetRenderedState();
                    renderManager.render();
                }
                else if (params.columnIndex === config.columnCount - 1) {
                    // If it was added as the last column, we can just call render and it will show up
                    // because the new columns will be appended
                    renderManager.render();
                }
                else {

                    var statePairsToAddRowTo = [{
                        state: 'state',
                        rowStates: 'rowStates'
                    }];

                    if (config.stickyRows > 0) {
                        statePairsToAddRowTo.push({
                            state: 'stickyState',
                            rowStates: 'stickyRowStates'
                        });
                    }

                    _.each(statePairsToAddRowTo, function (propNames) {
                        // Insert a new column in every row
                        renderManager[propNames.rowStates] = _.map(
                            renderManager[propNames.rowStates],
                            function (rowState, rowIndex) {
                                if (!rowState) {
                                    return rowState;
                                }

                                // Only render the column if it is inside the viewport
                                if (!rowState.children[params.columnIndex]) {
                                    return;
                                }

                                var row = renderManager[propNames.state].children[rowIndex];

                                var column = _renderColumn({
                                    rowIndex: rowIndex,
                                    columnIndex: params.columnIndex,
                                    row: row
                                }).item;

                                rowState.children.splice(params.columnIndex, 0, column);
                                rowState.children = updateIndexAttributes(rowState.children);

                                if (rowState.children[params.columnIndex - 1]) {
                                    column.insertAfter(rowState.children[params.columnIndex - 1]);
                                } else {
                                    row.prepend(column);
                                }

                                return rowState;
                            }
                        );
                    });

                    if (config.dynamicColumnWidth) {
                        renderManager.sizeCache.columns.splice(
                            params.columnIndex,
                            0,
                            _.last(_.compact(renderManager.rowStates)).children[params.columnIndex][DETECT_ITEM_WIDTH_FN]()
                        );
                    }
                }

                config.onAfterColumnCountChanged({
                    oldColumnCount: oldColumnCount,
                    newColumnCount: config.columnCount
                });

            }

        };

        return renderManager;
    };

    var VirtualScroller = function (config) {

        var EVENT_NAMESPACE = 'VirtualScroller-' + _.uniqueId();

        config = validateParameters(config, {

            /*
                Required
            */
            container: {type: 'object', customCheck: function (container) {
                return container instanceof $;
            }},

            /*
                Optional
            */
            canvasContainer: {type: ['object', 'undefined'], customCheck: function (canvasContainer) {
                return canvasContainer === undefined || canvasContainer instanceof $;
            }},
            canvas: {type: ['object', 'undefined'], customCheck: function (canvas) {
                return canvas === undefined || canvas instanceof $;
            }},
            scrollContainer: {type: ['object', 'undefined'], customCheck: function (canvasContainer) {
                return canvasContainer === undefined || canvasContainer instanceof $;
            }},
            scrollCanvas: {type: ['object', 'undefined'], customCheck: function (canvas) {
                return canvas === undefined || canvas instanceof $;
            }},

            rowCount: {type: 'number', default: 1000},
            columnCount: {type: ['number', 'undefined'], default: undefined},

            rowHeight: {type: 'number', default: 50},
            columnWidth: {type: ['number', 'undefined'], default: undefined},

            /*
                If your row elements are more complex, you may need to specify where exactly the columns
                should be inserted. That's what columnCanvasSelector is all about. You can pass a jQuery
                selector and we will search for that inside the row element and use it as the canvas
                for your columns.
            */
            columnCanvasSelector: {type: ['string', 'undefined'], default: undefined},

            /*
                preRender values should always be >= 1 if you plan to scroll in that direction.
                Otherwise the topmost item will be cleaned up as soon as you start scrolling.

                The only time that you should use preRenderRows: 0 is if you want to do only x-scrolling
                and you have exactly 1 row.
            */
            preRenderRows: {type: 'number', default: 1},
            preRenderColumns: {type: 'number', default: 1},

            stickyRows: {type: 'number', default: 0},
            stickyColumns: {type: 'number', default: 0},

            /*
                Callbacks
            */
            onBeforeRowRemoved: {type: 'function', default: function (params) {
                return true;
            }},
            onBeforeColumnRemoved: {type: 'function', default: function (params) {
                return true;
            }},
            onAfterIndicesChanged: {type: 'function', default: function () {}},
            onAfterRowCountChanged: {type: 'function', default: function () {}},
            onAfterColumnCountChanged: {type: 'function', default: function () {}},

            dynamicRowHeight: {type: 'boolean', default: false},
            dynamicColumnWidth: {type: 'boolean', default: false},

            /*
                Pass wether or not the size of the rendered items is accurate before adding them to
                the dom. You can achieve that by setting the size explicity (row.css('height', ...)).
                If this is not to case, we have to do a workaround that may cause performance issues
                or cause flickering.
            */
            rowHeightIsAccurate: {type: 'boolean', default: false},
            columnWidthIsAccurate: {type: 'boolean', default: false},

            renderRow: {type: 'function', default: function (params) {
                return $('<div>')
                    .text('Item ' + (params.rowIndex + 1))
                    .css('height', config.rowHeight + 'px');
            }},
            renderColumn: {type: ['function', 'undefined'], default: undefined}
        });


        if (config.rowCount > 50000000 || config.columnCount > 50000000) {
            throw new Error('Virtual Scroller: Please keep rowCount and columnCount below 50000000. Otherwise the whole page might crash. Passed rowCount: ' + config.rowCount + ', passed columnCount: ' + config.columnCount);
        }


        /*
            Instead of scrolling inside config.container, we create sub elements in which we can scroll.
            The passed canvasContainer will not be scrollable itself but we move the canvas using translate,
            so it looks like you would be scrolling inside the canvasContainer.
            The actual scrolling is performed in config.scrollContainer.
            That way we have complete control over the scrolling and can do things like sticky rows/columns
            without flickering.

            You can take more control over the structure of the dom (or things like class names) if
            you pass these elements youself. Expected structure:

            container
                scrollContainer
                    scrollCanvas
                canvasContainer
                    [stickyCanvasContainer]
                    canvas

            We used to add the translation to every row but that caused some problems when using z-index
            inside the scroller as css-transforms create a new stacking context.
        */

        if (!config.scrollContainer) {
            config.scrollContainer = $('<div>')
                .attr('created-by-virtual-scroller', '')
                .prependTo(config.container);
        }

        if (!config.scrollCanvas) {
            config.scrollCanvas = $('<div>')
                .attr('created-by-virtual-scroller', '')
                .appendTo(config.scrollContainer);
        }

        if (!config.canvasContainer) {
            config.canvasContainer = $('<div>')
                .attr('created-by-virtual-scroller', '')
                .appendTo(config.container);
        }

        if (!config.canvas) {
            config.canvas = $('<div>')
                .attr('created-by-virtual-scroller', '')
                .appendTo(config.canvasContainer);
        }


        config.container.css({
            position: 'relative',
            overflow: 'hidden'
        }).attr('virtual-scroller-container', '')

        config.scrollContainer.css({
            overflow: 'auto',
            position: 'relative',
            zIndex: 2,
            pointerEvents: 'none',
            maxHeight: '100%',
            height: '100%',
            maxWidth: '100%',
            width: '100%'
        }).attr('virtual-scroller-scroll-container', '');

        config.canvasContainer.css({
            position: 'absolute',
            top: 0,
            zIndex: 1,
            overflow: 'hidden',
            maxHeight: '100%',
            height: '100%',
            maxWidth: '100%',
            width: '100%'
        }).attr('virtual-scroller-canvas-container', '')

        /*
            Problem: The scrollContainer has to be on top of config.canvasContainer in order to be able
            to respond to scroll events. But config.canvasContainer has to be on top in order to react
            to mouse events.
            Solution: We let scrollContainer be on top of config.canvasContainer via zIndex but make
            it completely unresponsive to events via pointerEvents: 'none'.
            That way mouse events still reach the config.canvasContainer underneith.
            Then, we set pointerEvents to 'auto' as soon as the user starts
            scrolling and only as long as he is scrolling.
        */

        var scrollContainerIsActive = false;
        var releaseScrollContainer = _.debounce(function () {
            config.scrollContainer.css('pointer-events', 'none');
            scrollContainerIsActive = false;
        }, 300);

        /*
            DOMMouseScroll is needed for Firefox
            http://stackoverflow.com/questions/16788995/mousewheel-event-is-not-triggering-in-firefox-browser
        */
        config.container.on(
            _.map(['mousewheel', 'DOMMouseScroll'], function (eventName) {
                return eventName + '.' + EVENT_NAMESPACE;
            }).join(' '),
            function () {
                if (!scrollContainerIsActive) {
                    config.scrollContainer.css('pointer-events', 'auto');
                    scrollContainerIsActive = true;
                }
                releaseScrollContainer();
            }
        );


        if (config.stickyRows > 0 || config.stickyColumns > 0) {
            /*
                This container is needed if you want to center the canvas inside the canvasContainer.
                You will need to set margin:auto on config.canvas and on config.stickyCanvasContainer.
            */
            config.stickyCanvasContainer = $('<div virtual-scroller-sticky-canvas-container>')
                .css({
                    position: 'relative',
                    zIndex: 3
                })
                .attr('created-by-virtual-scroller', '')
                .prependTo(config.canvasContainer);
        }

        /*
            Insert stickyColumnsCanvas first, so stickyRowsCanvas is on top of it and things like
            box-shadows of the columns don't show on top of the sticky row on the top.
        */
        if (config.stickyColumns > 0) {
            config.stickyColumnsCanvas = $('<div virtual-scroller-sticky-columns-canvas>')
                .css({
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    minWidth: config.stickyColumns * config.columnWidth
                })
                .attr('created-by-virtual-scroller', '')
                .appendTo(config.stickyCanvasContainer);
        }

        if (config.stickyRows > 0) {
            config.stickyRowsCanvas = $('<div virtual-scroller-sticky-rows-canvas>')
                .css({
                    position: 'absolute',
                    top: 0,
                    minHeight: config.stickyRows * config.rowHeight,
                    width: '100%'
                })
                .attr('created-by-virtual-scroller', '')
                .appendTo(config.stickyCanvasContainer);
        }

        if (config.stickyRows > 0 && config.stickyColumns > 0) {
            config.fixedCanvas = $('<div virtual-scroller-fixed-canvas>')
                .css({
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    minHeight: config.stickyRows * config.rowHeight,
                    minWidth: config.stickyColumns * config.columnWidth
                })
                .attr('created-by-virtual-scroller', '')
                .appendTo(config.stickyCanvasContainer);
        }



        var renderManager = RenderManager(config);

        // render initial screen
        renderManager.render();

        // react to scroll events
        config.scrollContainer.on('scroll.' + EVENT_NAMESPACE, _.throttle(function () {
            renderManager.render();
        }, 16, { // aim for 60 fps => 1000 / 60 = 16,666667
            trailing: true
        }));

        var onResize = _.throttle(function () {
            // We could consider debouncing this more for performance reasons
            // But there might be edge cases were re-rerendering with an outdated sizeCache
            // results in the wrong amount of items being rendered. So for now, we'll always do both.
            renderManager.reevaluateAllSizes(); // sizes of items might have changed due to resizing
            renderManager.render();
        }, 16, { // aim for 60 fps => 1000 / 60 = 16,666667
            trailing: true
        });

        // react to resize events
        $(window).on('resize.' + EVENT_NAMESPACE, onResize);

        // provide API
        var API = {

            // Call these if you moved dom nodes yourself
            itemMoved: function (params) {},
            itemAdded: function (params) {},
            itemRemoved: function (params) {},

            // Call this if the dimensions of one or multiple rendered dom nodes change
            sizeChanged: function (params) {

                params = validateParameters(params || {}, {
                    rowHeight: {type: ['number', 'undefined'], default: undefined},
                    columnWidth: {type: ['number', 'undefined'], default: undefined},
                    rowIndex: {type: ['number', 'undefined'], default: undefined},
                    columnIndex: {type: ['number', 'undefined'], default: undefined}
                });

                if (_.every(params, _.isUndefined)) {
                    return onResize();
                }


                /*
                    For static sizes
                */

                if (params.rowHeight !== undefined) {
                    config.rowHeight = params.rowHeight;
                }

                if (params.columnWidth !== undefined) {
                    config.columnWidth = params.columnWidth;
                }


                /*
                    For dynamic sizes
                */

                if (params.rowIndex !== undefined && params.columnIndex === undefined) {
                    if (!config.dynamicRowHeight) {
                        throw new Error('Virtual Scroller: You can\'t update the height of specific rows in static mode. Please set dynamicRowHeight to true in order to do that. Or pass rowHeight if the height of all rows changed.');
                    }
                    renderManager.reevaluateRowSizeAtIndex({
                        rowIndex: params.rowIndex
                    });
                }

                if (params.columnIndex !== undefined) {
                    if (!config.dynamicColumnWidth) {
                        throw new Error('Virtual Scroller: You can\'t update the height of specific columns in static mode. Please set dynamicColumnWidth to true in order to do that. Or pass columnWidth if the width of all columns changed.');
                    }
                    renderManager.reevaluateColumnSizeAtIndex({
                        rowIndex: params.rowIndex, // might be undefined
                        columnIndex: params.columnIndex
                    });
                }

                renderManager.render();
            },

            // Call these to make the scroller move dom nodes
            removeRowAt: function (params) {
                params = validateParameters(params, {
                    rowIndex: {type: 'number'}
                });
                renderManager.removeItemAt({rowIndex: params.rowIndex});
                renderManager.render(); // render more items underneith
            },
            removeColumnAt: function (params) {
                params = validateParameters(params, {
                    columnIndex: {type: 'number'}
                });
                renderManager.removeItemAt({columnIndex: params.columnIndex});
                renderManager.render(); // render more items to the right
            },

            addRowAt: function (params) {
                params = validateParameters(params, {
                    rowIndex: {type: 'number'}
                });
                renderManager.addItemAt(params);
            },

            addColumnAt: function (params) {
                params = validateParameters(params, {
                    columnIndex: {type: 'number'}
                });
                renderManager.addItemAt(params);
            },

            scrollTo: function (params) {

                params = validateParameters(params, {
                    rowIndex: {type: 'number', default: 0},
                    columnIndex: {type: ['number', 'undefined'], default: undefined},
                    animationDuration: {type: 'number', default: 0}
                });

                var scrollTop = renderManager.getScrollTopForRowIndex({rowIndex: params.rowIndex});
                var scrollLeft = 0;

                if (params.columnIndex !== undefined) {
                    scrollLeft = renderManager.getScrollLeftForColumnIndex({
                        rowIndex: params.rowIndex,
                        columnIndex: params.columnIndex
                    });
                }

                config.scrollContainer.animate({
                    scrollTop: scrollTop,
                    scrollLeft: scrollLeft
                }, params.animationDuration);
            },
            rerender: function (params) {

                params = validateParameters(params, {
                    newRowCount: {type: 'number', default: config.rowCount},
                    newColumnCount: {type: 'number', default: config.columnCount}
                });

                config.rowCount = params.newRowCount;
                config.columnCount = params.newColumnCount;

                API.scrollTo({
                    rowIndex: 0,
                    columnIndex: 0
                });

                renderManager.resetState();
                renderManager.render();
            },
            rerenderViewport: function (params) {
                renderManager.resetRenderedState();
                renderManager.render();
            },

            destroy: (function () {

                var isDestroyed = false;

                return function () {

                    if (isDestroyed) {
                        return;
                    }

                    config.container
                        .add(config.canvasContainer)
                        .add(config.canvas)
                        .add(config.scrollContainer)
                        .add(config.scrollCanvas)
                        .attr('style', '');

                    config.canvas.empty();
                    config.container.find('[created-by-virtual-scroller]').remove();

                    config.container.off('.' + EVENT_NAMESPACE);
                    config.scrollContainer.off('.' + EVENT_NAMESPACE);


                    $(window).off('.' + EVENT_NAMESPACE);
                    renderManager.resetState();
                    renderManager = null;
                    config.canvas
                        .empty()
                        .css('height', '');

                    isDestroyed = true;
                };
            }()),

            isRowRendered: renderManager.isRowRendered,
            isColumnRendered: renderManager.isColumnRendered,


        };

        return API;

    };


    /*
        Data Adapter
    */

    VirtualScroller.DataAdapter = function (config) {

        config = validateParameters(config, {
            loadRange: {type: 'function'}
        });

        var Range = function (params) {

            params = validateParameters(params, {
                start: {type: 'number'},
                end: {type: 'number'}
            });

            var range = {
                start: params.start,
                end: params.end
            };

            var internalPromise = config.loadRange(range);

            range.getPromiseForIndex = function (params) {

                params = validateParameters(params, {
                    index: {type: 'number'}
                });

                return internalPromise.then(function (data) {
                    return (_.isArray(data) && data[params.index - range.start]) || undefined;
                });
            };

            return range;
        };

        var dataDapter = {};

        var requestedRanges = [];

        var getRangeForIndex = function (params) {

            params = validateParameters(params, {
                ranges: {type: 'array'},
                index: {type: 'number'}
            });

            return _.find(params.ranges, function (range) {
                return range.start <= params.index && range.end >= params.index;
            });
        };

        dataDapter.get = function (params) {

            params = validateParameters(params, {
                index: {type: 'number'}
            });

            var range = getRangeForIndex({ranges: requestedRanges, index: params.index});
            if (!range) {

                range = Range({
                    start: params.index,
                    end: params.index + 25
                });

                requestedRanges = requestedRanges.concat(range);

            }

            return range.getPromiseForIndex({index: params.index});

        };

        dataDapter.reset = function () {
            requestedRanges = [];
        };


        return dataDapter;
    };


    /*
        Dom Cache Adapter
        TODO: React to all dynamic changes like rowRemoved, rowAdded, rerender, ...
    */

//    VirtualScroller.DomCacheAdapter = function (scrollerConfig) {
//
//        var rowCache = {};
//        var columnCache = {};
//
//        var oldOnBeforeRowRemoved = scrollerConfig.onBeforeRowRemoved;
//        scrollerConfig.onBeforeRowRemoved = function (params) {
//            if (oldOnBeforeRowRemoved) {
//                oldOnBeforeRowRemoved(params);
//            }
//            rowCache[params.rowIndex] = params.row;
//            params.row.detach();
//            params.row.find('[virtual-scroller-index]').each(function (index, column) {
//                column = $(column);
//                var columnIndex = parseInt(column.attr('virtual-scroller-index'));
//                columnCache[params.rowIndex] = columnCache[params.rowIndex] || {};
//                columnCache[params.rowIndex][columnIndex] = column;
//            });
//            return false;
//        };
//
//        var oldRenderRow = scrollerConfig.renderRow;
//        scrollerConfig.renderRow = function (params) {
//            if (rowCache[params.rowIndex]) {
//                return rowCache[params.rowIndex];
//            }
//            return oldRenderRow(params);
//        };
//
//        if (scrollerConfig.renderColumn) {
//
//            var oldOnBeforeColumnRemoved = scrollerConfig.onBeforeColumnRemoved;
//            scrollerConfig.onBeforeColumnRemoved = function (params) {
//                if (oldOnBeforeRowRemoved) {
//                    oldOnBeforeRowRemoved(params);
//                }
//                columnCache[params.rowIndex] = columnCache[params.rowIndex] || {};
//                columnCache[params.rowIndex][params.columnIndex] = params.column;
//                params.column.detach();
//                return false;
//            };
//
//            var oldRenderColumn = scrollerConfig.renderColumn;
//            scrollerConfig.renderColumn = function (params) {
//                if (columnCache[params.rowIndex] && columnCache[params.rowIndex][params.columnIndex]) {
//                    return columnCache[params.rowIndex][params.columnIndex];
//                }
//                return oldRenderColumn(params);
//            };
//        }
//
//
//        return scrollerConfig;
//    };


    /*
        Keep in Dom Adapter
    */

    VirtualScroller.KeepInDomAdapter = function (scrollerConfig, config) {
        /*
            Idee:
            rowIndex 1 soll im dom behalten werden (weil er grad per drag and drop bewegt wird)
            onBeforeRowRemoved => return false for index 1, cache row
            renderRow => return cached row for index 1

            Muss wahrscheinlich zu einem anderen Canvas hinzugefügt werden.
            Sonst wird das folgende passieren
            // init:
            0,1,2,3,4,5
            // scroll right:
            1,2,3,4,5,6
            1,2,3,4,5,6,7 // keep 1 in dom
            1,3,4,5,6,7,8
            // scroll left:
            2,1,3,4,5,6,7 // 2 is prepended => 1 is at the wrong place

        */

        return scrollerConfig;
    };


    return VirtualScroller;
}(jQuery, _));