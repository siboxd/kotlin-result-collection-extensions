@file:JvmName("FlowResultExtensions")

package com.github.siboxd.kotlin.extension.flow.result

import io.craigmiller160.kotlin.result.flatMap
import io.craigmiller160.kotlin.result.flatMapCatching
import kotlinx.coroutines.flow.*

/**
 * Applies [transform] function to each [Result] encapsulated value of the given flow.
 *
 * The receiver of the [transform] is [FlowCollector] and thus [transform] is a
 * flexible function that may transform the emitted element, skip it or emit it multiple times.
 *
 * This operator generalizes [filter] and [map] operators and
 * can be used as a building block for other operators.
 *
 * @see Flow.transform for more information
 */
inline fun <T, R> Flow<Result<T>>.transformResult(
    crossinline transform: suspend FlowCollector<Result<R>>.(value: T) -> Unit
): Flow<Result<R>> = transform { result ->
    result
        .onFailure { emit(Result.failure(it)) }
        .onSuccess { transform(it) }
}

/**
 * Applies [transform] function to each [Result] encapsulated value of the given flow while this
 * function returns `true`.
 *
 * All incoming Flow `Result.failure` are automatically transformed emitting the failure and returning `true`,
 * making the flow emit every failure and continue until the next `Result.success` to be actually transformed trough [transform].
 *
 * The receiver of the [transform] is [FlowCollector] and thus [transform] is a
 * flexible function that may transform the emitted element, skip it or emit it multiple times.
 *
 * This operator generalizes [takeWhile] and can be used as a building block for other operators.
 *
 * @see Flow.transformWhile for more information
 */
inline fun <T, R> Flow<Result<T>>.transformResultWhile(
    crossinline transform: suspend FlowCollector<Result<R>>.(value: T) -> Boolean
): Flow<Result<R>> = transformWhile { result ->
    result.fold(
        onFailure = {
            emit(Result.failure(it)) // emit the current failure
            true // Continue collecting while the incoming result is a failure
        },
        onSuccess = { transform(it) }
    )
}

/**
 * Collects the value emitted by the upstream, wrapping it in a [Result.success].
 * This method is not thread-safe and should not be invoked concurrently.
 *
 * @see FlowCollector.emit
 */
suspend fun <T> FlowCollector<Result<T>>.emitResult(value: T) = emit(Result.success(value))

/** Returns a flow containing only the encapsulated values of the original flow that match the given [predicate]. Failures will still pass. */
inline fun <T> Flow<Result<T>>.filterResult(
    crossinline predicate: suspend (T) -> Boolean
): Flow<Result<T>> = transformResult { value ->
    if (predicate(value)) return@transformResult emitResult(value)
}

/** Returns a flow containing only the encapsulated values of the original flow that do not match the given [predicate]. */
inline fun <T> Flow<Result<T>>.filterResultNot(
    crossinline predicate: suspend (T) -> Boolean
): Flow<Result<T>> = transformResult { value ->
    if (!predicate(value)) return@transformResult emitResult(value)
}

/** Returns a flow containing only the encapsulated values that are instances of specified type [R]. */
@Suppress("UNCHECKED_CAST")
inline fun <reified R> Flow<Result<*>>.filterResultIsInstance(): Flow<Result<R>> =
    filterResult { it is R } as Flow<Result<R>>

/** Returns a flow containing only the encapsulated values of the original flow that are not null. */
fun <T : Any> Flow<Result<T?>>.filterResultNotNull(): Flow<Result<T>> =
    transformResult { value ->
        if (value != null) return@transformResult emitResult(value)
    }

/** Returns a flow containing the encapsulated results of applying the given [transform] function to each [Result] encapsulated value of the original flow. */
inline fun <T, R> Flow<Result<T>>.mapResult(
    crossinline transform: suspend (value: T) -> R
): Flow<Result<R>> = map { result -> result.map { transform(it) } }

/** Returns a flow containing the encapsulated results of applying the given [transform] function to each [Result] encapsulated value of the original flow, catching thrown exceptions. */
inline fun <T, R> Flow<Result<T>>.mapResultCatching(
    crossinline transform: suspend (value: T) -> R
): Flow<Result<R>> = map { result -> result.mapCatching { transform(it) } }

/** Returns a flow containing the results of applying the given [transform] function to each [Result] encapsulated value of the original flow. */
inline fun <T, R> Flow<Result<T>>.flatMapResult(
    crossinline transform: suspend (value: T) -> Result<R>
): Flow<Result<R>> = map { result -> result.flatMap { transform(it) } }

/** Returns a flow containing the results of applying the given [transform] function to each [Result] encapsulated value of the original flow, catching thrown exceptions. */
inline fun <T, R> Flow<Result<T>>.flatMapResultCatching(
    crossinline transform: suspend (value: T) -> Result<R>
): Flow<Result<R>> = map { result -> result.flatMapCatching { transform(it) } }

/** Returns a flow that invokes the given [action] on successful results **before** each value of the upstream flow is emitted downstream. */
inline fun <T> Flow<Result<T>>.onEachResultSuccess(
    crossinline action: suspend (value: T) -> Unit
): Flow<Result<T>> = onEach { result -> result.onSuccess { action(it) } }

/** Returns a flow that invokes the given [action] on failure results **before** each value of the upstream flow is emitted downstream. */
inline fun <T> Flow<Result<T>>.onEachResultFailure(
    crossinline action: suspend (value: Throwable) -> Unit
): Flow<Result<T>> = onEach { result -> result.onFailure { action(it) } }

/** Returns a flow containing the flow of encapsulated results after applying the given [transform] function to each [Result] encapsulated value of the original flow of flows, catching thrown exceptions. */
inline fun <T, R> Flow<Flow<Result<T>>>.mapInnerResults(
    crossinline transform: suspend (value: T) -> R
): Flow<Flow<Result<R>>> = map { innerFlow -> innerFlow.mapResultCatching(transform) }

/** Returns a flow containing the flow of results after applying the given [transform] function to each [Result] encapsulated value of the original flow of flows, catching thrown exceptions. */
inline fun <T, R> Flow<Flow<Result<T>>>.flatMapInnerResults(
    crossinline transform: suspend (value: T) -> Result<R>
): Flow<Flow<Result<R>>> = map { innerFlow -> innerFlow.flatMapResultCatching(transform) }