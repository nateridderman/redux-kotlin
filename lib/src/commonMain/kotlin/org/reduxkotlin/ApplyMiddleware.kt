package org.reduxkotlin

/**
 * Creates a store enhancer that applies middleware to the dispatch method
 * of the Redux store. This is handy for a variety of tasks, such as expressing
 * asynchronous actions in a concise manner, or logging every action payload.
 *
 * See `redux-thunk` package as an example of the Redux middleware.
 *
 * Because middleware is potentially asynchronous, this should be the first
 * store enhancer in the composition chain.
 *
 * Note that each middleware will be given the `dispatch` and `getState` functions
 * as named arguments.
 *
 * @param {vararg Middleware} [middleware] The middleware chain to be applied.
 * @returns {StoreEnhancer} A store enhancer applying the middleware.
 */
fun applyMiddleware(vararg middlewares: Middleware): StoreEnhancer {
    return { storeCreator ->
        { reducer, initialState, en ->
            val store = storeCreator(reducer, initialState, en)
            var dispatch: Dispatcher = { action: Any ->
                throw Exception(
                        """Dispatching while constructing your middleware is not allowed.
                    Other middleware would not be applied to this dispatch.""")
            }
            store.dispatch = dispatch
            val chain = middlewares.map { middleware -> middleware(store) }
            dispatch = compose(chain)(store.dispatch)

            Store(getState = store.getState,
                    dispatch = dispatch,
                    subscribe = store.subscribe,
                    replaceReducer = store.replaceReducer)
        }
    }
}
