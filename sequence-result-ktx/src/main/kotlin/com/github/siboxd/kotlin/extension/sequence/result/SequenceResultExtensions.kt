@file:JvmName("SequenceResultExtensions")

package com.github.siboxd.kotlin.extension.sequence.result

import io.craigmiller160.kotlin.result.flatMap
import io.craigmiller160.kotlin.result.flatMapCatching

/** Returns a sequence containing the encapsulated results of applying the given [transform] function to each [Result] encapsulated value in the original sequence. */
inline fun <T, R> Sequence<Result<T>>.mapResult(
    crossinline transform: (value: T) -> R
): Sequence<Result<R>> = map { result -> result.map { transform(it) } }

/** Returns a sequence containing the encapsulated results of applying the given [transform] function to each [Result] encapsulated value in the original sequence, catching thrown exceptions. */
inline fun <T, R> Sequence<Result<T>>.mapResultCatching(
    crossinline transform: (value: T) -> R
): Sequence<Result<R>> = map { result -> result.mapCatching { transform(it) } }

/** Returns a sequence containing the results of applying the given [transform] function to each [Result] encapsulated value in the original sequence. */
inline fun <T, R> Sequence<Result<T>>.flatMapResult(
    crossinline transform: (value: T) -> Result<R>
): Sequence<Result<R>> = map { result -> result.flatMap { transform(it) } }

/** Returns a sequence containing the results of applying the given [transform] function to each [Result] encapsulated value in the original sequence, catching thrown exceptions. */
inline fun <T, R> Sequence<Result<T>>.flatMapResultCatching(
    crossinline transform: (value: T) -> Result<R>
): Sequence<Result<R>> = map { result -> result.flatMapCatching { transform(it) } }

/** Returns a sequence containing the sequence of encapsulated results after applying the given [transform] function to each [Result] encapsulated value in the original sequence of sequences, catching thrown exceptions. */
inline fun <T, R> Sequence<Sequence<Result<T>>>.mapInnerResults(
    crossinline transform: (value: T) -> R
): Sequence<Sequence<Result<R>>> =
    map { innerSequence -> innerSequence.mapResultCatching(transform) }

/** Returns a sequence containing the sequence of results after applying the given [transform] function to each [Result] encapsulated value in the original sequence of sequences, catching thrown exceptions. */
inline fun <T, R> Sequence<Sequence<Result<T>>>.flatMapInnerResults(
    crossinline transform: (value: T) -> Result<R>
): Sequence<Sequence<Result<R>>> =
    map { innerSequence -> innerSequence.flatMapResultCatching(transform) }

/** Returns a sequence containing only the encapsulated values matching the given [predicate]. Failures will not be evaluated and will pass through. */
inline fun <T> Sequence<Result<T>>.filterResult(
    crossinline predicate: (T) -> Boolean
): Sequence<Result<T>> = filter { result ->
    result.fold(
        onSuccess = { predicate(it) },
        onFailure = { true }
    )
}

/** Returns a sequence containing all encapsulated values not matching the given [predicate]. Failures will not be evaluated and will pass through. */
inline fun <T> Sequence<Result<T>>.filterResultNot(
    crossinline predicate: (T) -> Boolean
): Sequence<Result<T>> = filterResult { !predicate(it) }

/** Returns a sequence containing all encapsulated values that are instances of specified type parameter [R]. Failures will not be evaluated and will pass through. */
@Suppress("UNCHECKED_CAST")
inline fun <reified R> Sequence<Result<*>>.filterResultIsInstance(): Sequence<Result<R>> =
    filterResult { it is R } as Sequence<Result<R>>

/** Returns a sequence containing all encapsulated values that are not `null`. Failures will not be evaluated and will pass through. */
fun <T> Sequence<Result<T?>>.filterResultNotNull(): Sequence<Result<T & Any>> =
    mapNotNull { result ->
        result.fold(
            onSuccess = { it?.let { Result.success(it) } },
            onFailure = { Result.failure(it) }
        )
    }

/** Returns a sequence that invokes the given [action] on successful results. */
inline fun <T> Sequence<Result<T>>.onEachResultSuccess(
    crossinline action: (value: T) -> Unit
): Sequence<Result<T>> = onEach { result -> result.onSuccess { action(it) } }

/** Returns a sequence that invokes the given [action] on failure results. */
inline fun <T> Sequence<Result<T>>.onEachResultFailure(
    crossinline action: (value: Throwable) -> Unit
): Sequence<Result<T>> = onEach { result -> result.onFailure { action(it) } }