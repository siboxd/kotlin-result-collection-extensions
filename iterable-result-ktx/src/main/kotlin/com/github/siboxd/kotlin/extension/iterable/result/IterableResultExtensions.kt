@file:JvmName("IterableResultExtensions")

package com.github.siboxd.kotlin.extension.iterable.result

import io.craigmiller160.kotlin.result.flatMap
import io.craigmiller160.kotlin.result.flatMapCatching

/** Returns a list containing the encapsulated results of applying the given [transform] function to each [Result] encapsulated value in the original collection. */
inline fun <T, R> Iterable<Result<T>>.mapResult(
    transform: (value: T) -> R
): List<Result<R>> = map { result -> result.map { transform(it) } }

/** Returns a list containing the encapsulated results of applying the given [transform] function to each [Result] encapsulated value in the original collection, catching thrown exceptions. */
inline fun <T, R> Iterable<Result<T>>.mapResultCatching(
    transform: (value: T) -> R
): List<Result<R>> = map { result -> result.mapCatching { transform(it) } }

/** Returns a list containing the results of applying the given [transform] function to each [Result] encapsulated value in the original collection. */
inline fun <T, R> Iterable<Result<T>>.flatMapResult(
    transform: (value: T) -> Result<R>
): List<Result<R>> = map { result -> result.flatMap { transform(it) } }

/** Returns a list containing the results of applying the given [transform] function to each [Result] encapsulated value in the original collection, catching thrown exceptions. */
inline fun <T, R> Iterable<Result<T>>.flatMapResultCatching(
    transform: (value: T) -> Result<R>
): List<Result<R>> = map { result -> result.flatMapCatching { transform(it) } }

/** Returns a list containing the list of encapsulated results after applying the given [transform] function to each [Result] encapsulated value in the original collection of collections, catching thrown exceptions. */
inline fun <T, R> Iterable<Iterable<Result<T>>>.mapInnerResults(
    transform: (value: T) -> R
): List<List<Result<R>>> = map { innerIterable -> innerIterable.mapResultCatching(transform) }

/** Returns a list containing the list of results after applying the given [transform] function to each [Result] encapsulated value in the original collection of collections, catching thrown exceptions. */
inline fun <T, R> Iterable<Iterable<Result<T>>>.flatMapInnerResults(
    transform: (value: T) -> Result<R>
): List<List<Result<R>>> = map { innerIterable -> innerIterable.flatMapResultCatching(transform) }

/** Returns a list containing only the encapsulated values matching the given [predicate]. Failures will not be evaluated and will pass through. */
inline fun <T> Iterable<Result<T>>.filterResult(
    predicate: (T) -> Boolean
): List<Result<T>> = filter { result ->
    result.fold(
        onSuccess = { predicate(it) },
        onFailure = { true }
    )
}

/** Returns a list containing all encapsulated values not matching the given [predicate]. Failures will not be evaluated and will pass through. */
inline fun <T> Iterable<Result<T>>.filterResultNot(
    predicate: (T) -> Boolean
): List<Result<T>> = filterResult { !predicate(it) }

/** Returns a list containing all encapsulated values that are instances of specified type parameter [R]. Failures will not be evaluated and will pass through. */
@Suppress("UNCHECKED_CAST")
inline fun <reified R> Iterable<Result<*>>.filterResultIsInstance(): List<Result<R>> =
    filterResult { it is R } as List<Result<R>>

/** Returns a list containing all encapsulated values that are not `null`. Failures will not be evaluated and will pass through. */
fun <T> Iterable<Result<T?>>.filterResultNotNull(): List<Result<T & Any>> =
    mapNotNull { result ->
        result.fold(
            onSuccess = { it?.let { Result.success(it) } },
            onFailure = { Result.failure(it) }
        )
    }

/** Performs the given action on each successful result and returns the collection itself afterward. */
inline fun <T> Iterable<Result<T>>.onEachResultSuccess(
    action: (value: T) -> Unit
): Iterable<Result<T>> = onEach { result -> result.onSuccess { action(it) } }

/** Performs the given action on each failure result and returns the collection itself afterward. */
inline fun <T> Iterable<Result<T>>.onEachResultFailure(
    action: (value: Throwable) -> Unit
): Iterable<Result<T>> = onEach { result -> result.onFailure { action(it) } }