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
        var renderManager = {};
        renderManager.state = {};
        renderManager.rowStates = []; // rowIndex => rowState
        renderManager.sizeCache = {
            rows: [],
            columns: []
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
            params.scrollOffset = Math.max(0, params.scrollOffset);
            params.scrollOffset = Math.min(
                sumSizes({
                    startIndex: 0,
                    amount: params.itemCount,
                    sizeCache: sizeCache,
                    itemSize: params.itemSize
                }) - params.containerSize,
                params.scrollOffset
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
                    startIndex: numberOfItemsNotToRender,
                    amount: params.itemCount - numberOfItemsNotToRender,
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
                canvasSize = (params.itemCount - numberOfItemsNotToRender) * params.itemSize
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

        (function tests () {

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

            if (typeof window.testUtils !== 'undefined') {
                testUtils.describeFn({
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
                                    canvasSize: 1900
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
                                    canvasSize: (60 + 70 + 80 + 90 + 100) + (20 * 90) // 90 = itemSize * (itemCount - childrenCount - not rendered)
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
            }
        }());


        var renderChildren = function (canvas, newChildren, oldChildren) {
            oldChildren = oldChildren || [];
            var oldStartIndex = oldChildren.length - _.compact(oldChildren).length;

            var removedIndices = [];

            _.each(oldChildren, function (child, index) {
                if (!child) {
                    return;
                }
                if (_.indexOf(newChildren, child) === -1) {
                    child.remove();
                    removedIndices.push(index);
                }
            });

            var prepend = [];
            var append = [];

            _.each(newChildren, function (child, index) {
                if (!child) {
                    return;
                }
                if (_.indexOf(oldChildren, child) === -1) {
                    if (index < oldStartIndex) {
                        prepend.push(child);
                    } else {
                        append.push(child);
                    }
                }
            });

            canvas.prepend(prepend).append(append);
            return removedIndices;
        };

        var renderStateY = function (canvas, state, oldState) {
            canvas.css({
                transform: 'translateY(' + state.offset + 'px)',
                height: state.canvasSize + 'px'
            });
            var removedIndices = renderChildren(canvas, state.children, oldState.children);
            _.each(removedIndices, function (index) {
                renderManager.rowStates[index] = undefined;
            });
            return canvas;
        };

        var renderStateX = function (row, state, oldState) {

            row.css({
                transform: 'translateX(' + state.offset + 'px)'
            });

            renderChildren(row, state.children, oldState.children);

            return row;
        };

        renderManager.render = function () {

            var oldState = renderManager.state;

            var updateStateResult = updateState({
                oldState: oldState,
                sizeCache: renderManager.sizeCache.rows,
                containerSize: config.container.height(),
                scrollOffset: config.container.scrollTop(),
                itemCount: config.rowCount,
                itemSize: config.rowHeight,
                dynamicItemSize: config.dynamicRowHeight,
                preRender: config.preRenderRows,
                renderItem: function (params) {
                    var row = config.renderRow({
                        rowIndex: params.index
                    });
                    return {
                        item: row,
                        size: config.dynamicRowHeight ? row.outerHeight() : config.rowHeight
                    };
                }
            });
            renderManager.state = updateStateResult.state;
            renderManager.sizeCache.rows = updateStateResult.sizeCache;

            if (config.renderColumn) {
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
                            containerSize: config.container.outerWidth(),
                            scrollOffset: config.container.scrollLeft(),
                            itemCount: config.columnCount,
                            itemSize: config.columnWidth,
                            dynamicItemSize: config.dynamicColumnWidth,
                            preRender: config.preRenderColumns,
                            renderItem: function (params) {
                                var column = config.renderColumn({
                                    rowIndex: rowIndex,
                                    columnIndex: params.index
                                });
                                return {
                                    item: column,
                                    size: config.dynamicColumnWidth ? column.width() : config.columnWidth
                                };
                            }
                        });
                        renderManager.rowStates[rowIndex] = updateRowStateResult.state;
                        renderManager.sizeCache.columns = updateRowStateResult.sizeCache;
                        renderStateX(child, renderManager.rowStates[rowIndex], oldRowState);
                        return child;
                    }
                );

                var someChildState = _.first(_.compact(renderManager.rowStates));
                config.canvas.css('width', someChildState.canvasSize);
                if (someChildState.adjustedScrollOffsetBy > 0) {
                    config.container.scrollLeft(config.container.scrollLeft() + someChildState.adjustedScrollOffsetBy);
                }

            }

            renderStateY(config.canvas, renderManager.state, oldState);
            if (renderManager.state.adjustedScrollOffsetBy > 0) {
                config.container.scrollTop(
                    config.container.scrollTop() + renderManager.state.adjustedScrollOffsetBy
                );
            }
        };

        renderManager.updateRowSizeAtIndex = function (params) {

            params = validateParameters(params, {
                rowIndex: {type: 'number'}
            });

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
        renderManager.updateColumnSizeAtIndex = function (params) {

            params = validateParameters(params, {
                rowIndex: {type: ['number', 'undefined'], default: undefined},
                columnIndex: {type: 'number'}
            });

            var firstRenderedRowState = _.first(_.compact(renderManager.rowStates));
            var firstRenderedColumn = _.first(_.compact(firstRenderedRowState.children));
            renderManager.sizeCache.columns[params.columnIndex] = firstRenderedColumn.width();
        };

        /*
        renderManager.getFirstRowIndexInViewport = function () {
            return howManyItemsFitIn({
                startIndex: 0,
                containerSize: renderManager.state.scrollOffset,
                sizeCache: renderManager.state.sizeCache,
                itemSize: config.rowHeight
            });
        };
        */

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
            _.each(renderManager.state.children, function (child) {
                if (child) {
                    child.remove();
                }
            });
            renderManager.state = {};
            renderManager.rowStates = [];
            renderManager.sizeCache = {rows: [], columns: []};
        };

        renderManager.resetRenderedState = function () {

            /*
                Note: We don't reset the sizeCache for now because that would result in nasty jumps.
                The newly calculated offsets will be different from the previous ones and the user can't
                keep his scroll position.
            */

            _.each(renderManager.state.children, function (child) {
                if (child) {
                    child.remove();
                }
            });

            /*
            if (config.dynamicRowHeight) {
                renderManager.sizeCache.rows = _.map(
                    renderManager.sizeCache.rows,
                    function (size, index) {
                        if (renderManager.state.children && renderManager.state.children[index]) {
                            // index was rendered, we want to reset this size
                            return undefined;
                        }
                        return size;
                    }
                );
            }
            */
            renderManager.state = {};

            /*
            if (config.dynamicColumnWidth) {
                var firstRenderedRowState = _.first(_.compact(renderManager.rowStates));
                renderManager.sizeCache.columns = _.map(
                    renderManager.sizeCache.columns,
                    function (size, index) {
                        if (firstRenderedRowState.children && firstRenderedRowState.children[index]) {
                            // index was rendered, we want to reset this size
                            return undefined;
                        }
                        return size;
                    }
                );
            }
            */
            renderManager.rowStates = [];
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
            return _.map(children, function (child, index) {
                if (child) {
                    child.attr('virtual-scroller-index', index);
                }
                return child;
            });
        };

        renderManager.removeItemAt = function (params) {

            params = validateParameters(params, rowOrColumnIndexIsSet(params));

            if (params.rowIndex !== undefined) {

                config.rowCount -= 1; // TODO: Should this be done in here?

                if (renderManager.state.children[params.rowIndex]) {
                    renderManager.state.children[params.rowIndex].remove();
                }
                renderManager.state.children.splice(params.rowIndex, 1);
                renderManager.state.children = updateIndexAttributes(renderManager.state.children);
                renderManager.rowStates.splice(params.rowIndex, 1);

                if (config.dynamicRowHeight) {
                    renderManager.sizeCache.rows.splice(params.rowIndex, 1);
                }
            }
            else if (params.columnIndex !== undefined) {

                config.columnCount -= 1; // TODO: Should this be done in here?

                renderManager.rowStates = _.map(renderManager.rowStates, function (rowState) {
                    if (!rowState) {
                        return rowState;
                    }
                    if (rowState.children[params.columnIndex]) {
                        rowState.children[params.columnIndex].remove();
                    }
                    rowState.children.splice(params.columnIndex, 1);
                    rowState.children = updateIndexAttributes(rowState.children);
                    if (config.dynamicColumnWidth) {
                        renderManager.sizeCache.columns.splice(params.columnIndex, 1);
                    }
                    return rowState;
                });
            }
        };

        renderManager.addItemAt = function (params) {

            params = validateParameters(params, rowOrColumnIndexIsSet(params));

            if (params.rowIndex !== undefined) {

                config.rowCount += 1; // TODO: Should this be done in here?

                // Only render the row if it is inside the viewport
                if (!renderManager.state.children[params.rowIndex]) {
                    return;
                }

                // Actually render the row and insert it
                var row = config.renderRow({
                    rowIndex: params.rowIndex
                });
                renderManager.state.children.splice(params.rowIndex, 0, row);
                renderManager.state.children = updateIndexAttributes(renderManager.state.children);

                if (renderManager.state.children[params.rowIndex - 1]) {
                    row.insertAfter(renderManager.state.children[params.rowIndex - 1]);
                } else {
                    config.canvas.prepend(row);
                }

                if (config.dynamicRowHeight) {
                    renderManager.sizeCache.rows.splice(params.rowIndex, 0, row.outerHeight());
                }

                // If the user specified how to render columns inside the row, go ahead and do that
                if (config.renderColumn) {
                    var rowIndex = params.rowIndex;
                    var resultOfUpdateStateForRowState = updateState({
                        oldState: {},
                        sizeCache: renderManager.sizeCache.columns,
                        containerSize: config.container.outerWidth(),
                        scrollOffset: config.container.scrollLeft(),
                        itemCount: config.columnCount,
                        itemSize: config.columnWidth,
                        dynamicItemSize: config.dynamicColumnWidth,
                        preRender: config.preRenderColumns,
                        renderItem: function (params) {
                            var column = config.renderColumn({
                                rowIndex: rowIndex,
                                columnIndex: params.index
                            });
                            return {
                                item: column,
                                size: config.dynamicColumnWidth ? column.width() : config.columnWidth
                            };
                        }
                    });
                    var rowState = resultOfUpdateStateForRowState.state;
                    renderManager.sizeCache.columns = resultOfUpdateStateForRowState.sizeCache;
                    renderManager.rowStates.splice(params.rowIndex, 0, rowState);
                    renderStateX(row, rowState, {});
                }

            } else if (params.columnIndex !== undefined) {

                config.columnIndex += 1; // TODO: Should this be done in here?

                var isFirstRenderedColumn = true;

                // Insert a new column in every row
                renderManager.rowStates = _.map(renderManager.rowStates, function (rowState, rowIndex) {
                    if (!rowState) {
                        return rowState;
                    }

                    // Only render the column if it is inside the viewport
                    if (!rowState.children[params.columnIndex]) {
                        return;
                    }

                    var column = config.renderColumn({
                        rowIndex: rowIndex,
                        columnIndex: params.columnIndex
                    });

                    rowState.children.splice(params.columnIndex, 0, column);
                    rowState.children = updateIndexAttributes(rowState.children);
                    if (rowState.children[params.columnIndex - 1]) {
                        column.insertAfter(rowState.children[params.columnIndex - 1]);
                    } else {
                        renderManager.state.children[rowIndex].prepend(column);
                    }

                    // Optimization: Only do this for the first rendered column because they will
                    // all have the same width
                    if (isFirstRenderedColumn) {
                        if (config.dynamicColumnWidth) {
                            renderManager.sizeCache.columns.splice(params.columnIndex, 0, column.outerWidth());
                        }
                    }

                    isFirstRenderedColumn = false;
                    return rowState;
                });

            }

        };

        return renderManager;
    };

    var VirtualScroller = function (config) {

        var EVENT_NAMESPACE = 'VirtualScroller-' + _.uniqueId();

        /*
            Idee:

            config in rows und columns aufteilen

            rows: {
                render: function,
                count: number,
            },
            columns: {
                render: function,
                count: number
            }

            TODO:

            * Github Repo
            * addedRow/Column => to call after dragging/dropping a new row into the scroller
            * Firefox support

            * workaround for the maximum height of elements
            * prevent tab from crashing if rowCount/columnCount is > 50000000
            * How to handle async loading of data (enable user to do batch requests)
            * Check if nested scrollers work
            * Find a way to get rid of the required min height for dynamic items
            * dynamicRowHeight + dynamicColumnWidth => weird things happen when scrolling fast
        */

        config = validateParameters(config, {

            /*
                Required
            */
            container: {type: 'object', customCheck: function (container) {
                return container instanceof $;
            }},
            canvas: {type: 'object', customCheck: function (container) {
                return container instanceof $;
            }},

            /*
                Optional
            */
            rowCount: {type: 'number', default: 1000},
            columnCount: {type: ['number', 'undefined'], default: undefined},

            rowHeight: {type: 'number', default: 50},
            columnWidth: {type: ['number', 'undefined'], default: undefined},

            /*
                preRender values should always be >= 1 if you plan to scroll in that direction.
                Otherwise the topmost item will be cleaned up as soon as you start scrolling.

                The only time that you should use preRenderRows: 0 is if you want to do only x-scrolling
                and you have exactly 1 row.
            */
            preRenderRows: {type: 'number', default: 1},
            preRenderColumns: {type: 'number', default: 1},

            dynamicRowHeight: {type: 'boolean', default: false},
            dynamicColumnWidth: {type: 'boolean', default: false},

            renderRow: {type: 'function', default: function (params) {
                return $('<div>')
                    .text('Item ' + (params.index + 1))
                    .css('height', config.rowHeight + 'px');
            }},
            renderColumn: {type: ['function', 'undefined'], default: undefined}
        });


        if (config.rowCount > 50000000 || config.columnCount > 50000000) {
            throw new Error('Virtual Scroller: Please keep rowCount and columnCount below 50000000. Otherwise the whole page might crash. Passed rowCount: ' + config.rowCount + ', passed columnCount: ' + config.columnCount);
        }


        var renderManager = RenderManager(config);

        // render initial screen
        renderManager.render();

        // react to scroll events
        config.container.on('scroll.' + EVENT_NAMESPACE, _.throttle(function () {
            renderManager.render();
        }, 16, { // aim for 60 fps => 1000 / 60 = 16,666667
            trailing: true
        }));

        // react to resize events
        $(window).on('resize.' + EVENT_NAMESPACE, _.throttle(function () {
            renderManager.render();
        }, 16, { // aim for 60 fps => 1000 / 60 = 16,666667
            trailing: true
        }));

        // provide API
        var API = {

            // Call these if you moved dom nodes yourself
            itemMoved: function (params) {},
            itemAdded: function (params) {},
            itemRemoved: function (params) {},

            // Call this if the dimensions of one or multiple rendered dom nodes change
            sizeChanged: function (params) {

                params = validateParameters(params, {
                    rowHeight: {type: ['number', 'undefined'], default: undefined},
                    columnWidth: {type: ['number', 'undefined'], default: undefined},
                    rowIndex: {type: ['number', 'undefined'], default: undefined},
                    columnIndex: {type: ['number', 'undefined'], default: undefined}
                });

                if (params.rowHeight !== undefined) {
                    config.rowHeight = params.rowHeight;
                }

                if (params.columnWidth !== undefined) {
                    config.columnWidth = params.columnWidth;
                }

                if (params.rowIndex !== undefined && params.columnIndex === undefined) {
                    if (!config.dynamicRowHeight) {
                        throw new Error('Virtual Scroller: You can\'t update the height of specific rows in static mode. Please set dynamicRowHeight to true in order to do that. Or pass rowHeight if the height of all rows changed.');
                    }
                    renderManager.updateRowSizeAtIndex({
                        rowIndex: params.rowIndex
                    });
                }

                if (params.columnIndex !== undefined) {
                    if (!config.dynamicColumnWidth) {
                        throw new Error('Virtual Scroller: You can\'t update the height of specific columns in static mode. Please set dynamicColumnWidth to true in order to do that. Or pass columnWidth if the width of all columns changed.');
                    }
                    renderManager.updateColumnSizeAtIndex({
                        rowIndex: params.rowIndex, // might be undefined
                        columnIndex: params.columnIndex
                    });
                }

                renderManager.render();
            },

            // Call these to make the scroller move dom nodes
            moveItem: function (params) {},
            addItem: function (params) {},
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

                config.container.animate({
                    scrollTop: scrollTop,
                    scrollLeft: scrollLeft
                }, params.animationDuration);
            },
            rerender: function (params) {
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

            destroy: function () {
                config.container.off('.' + EVENT_NAMESPACE);
                $(window).off('.' + EVENT_NAMESPACE);
                renderManager.resetState();
                renderManager = null;
                config.canvas
                    .empty()
                    .css('height', '');
            }
        };

        return API;

    };


    return VirtualScroller;
}(jQuery, _));