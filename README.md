# Kotlin Result Collection Extensions

[![](https://jitpack.io/v/siboxd/kotlin-result-collection-extensions.svg)](https://jitpack.io/#siboxd/kotlin-result-collection-extensions)

This project adds several new extensions to collection types (`Iterable`, `Sequence` and coroutines
`Flow`) containing the Kotlin Result (`kotlin.Result<T>`) type to facilitate more robust,
functional-style mapping and error handling.

## Usage

1. Add it in your root `build.gradle` at the end of repositories:

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency

```groovy
dependencies {
    implementation("com.github.siboxd:kotlin-result-collection-extensions:${latestVersion}")
}
```

## Functionalities

### Overview

- *[Mapping extensions](#mapping-extensions)*:

    - [`mapResult`](#mapResult)
    - [`mapResultCatching`](#mapResultCatching)
    - [`flatMapResult`](#flatMapResult)
    - [`flatMapResultCatching`](#flatMapResultCatching)
    - [`mapInnerResults`](#mapInnerResults)
    - [`flatMapInnerResults`](#flatMapInnerResults)

- *[Filtering extensions](#filtering-extensions)*:

    - [`filterResult`](#filterResult)
    - [`filterResultNot`](#filterResultNot)
    - [`filterResultIsInstance`](#filterResultIsInstance)
    - [`filterResultNotNull`](#filterResultNotNull)

- *[Actions extensions](#actions-extensions)*:

    - [`onEachResultSuccess`](#onEachResultSuccess)
    - [`onEachResultFailure`](#onEachResultFailure)

#### Mapping Extensions

These extensions make possible to map `Result<T>` internal value avoiding the boilerplate code
needed to unpack, check and re-pack the internal value.

##### mapResult

Before (without extensions):

```kotlin
val myList = listOf(Result.success("Result"), Result.failure(RuntimeException()))
val resultList = myList.map { result ->
    result.map { "My: $it" }
}
println(resultList) // Prints: [Success(My: Result), Failure(java.lang.RuntimeException)]
```

After (with extensions):

```kotlin
val myList = listOf(Result.success("Result"), Result.failure(RuntimeException()))
val resultList = myList.mapResult { "My: $it" }
println(resultList) // Prints: [Success(My: Result), Failure(java.lang.RuntimeException)]
```

##### mapResultCatching

This is the same as [mapResult](#mapResult) but if the mapper function throws exception, it gets
caught.

##### flatMapResult

This is the same as [mapResult](#mapResult) but if the mapper function returns another `Result<T>`
it gets automatically flattened to avoid `Result<Result<T>>` compositions.

##### flatMapResultCatching

This is the mix of [flatMapResult](#flatMapResult) and [mapResultCatching](#mapResultCatching)
taking both advantages.

##### mapInnerResults

Before (without extensions):

```kotlin
val myListOfLists = listOf(
    listOf(Result.success("<0, 0>"), Result.failure(RuntimeException())),
    listOf(Result.success("<1, 0>"), Result.success("<1, 1>")),
)
val resultListOfLists = myListOfLists.map { resultList ->
    resultList.map { result ->
        result.map { "This is $it" }
    }
}
println(resultListOfLists) // Prints: [[Success(This is <0, 0>), Failure(java.lang.RuntimeException)], [Success(This is <1, 0>), Success(This is <1, 1>)]]
```

After (with extensions):

```kotlin
val myListOfLists = listOf(
    listOf(Result.success("<0, 0>"), Result.failure(RuntimeException())),
    listOf(Result.success("<1, 0>"), Result.success("<1, 1>")),
)
val resultListOfLists = myListOfLists.mapInnerResults { "This is $it" }
println(resultListOfLists) // Prints: [[Success(This is <0, 0>), Failure(java.lang.RuntimeException)], [Success(This is <1, 0>), Success(This is <1, 1>)]]
```

##### flatMapInnerResults

This is the same as [mapInnerResults](#mapInnerResults) but if the mapper function returns another
`Result<T>`
it gets automatically flattened to avoid `Result<Result<T>>` compositions.

#### Filtering Extensions

These extensions make possible to filter `Result<T>` internal value avoiding the boilerplate to
unpack the internal value.

##### filterResult

Before (without extensions):

```kotlin
val myList = listOf(Result.success("Result"), Result.failure(RuntimeException()))
val resultList = myList.filter { result ->
    result.getOrNull()
        ?.startsWith("Res")
        ?: true
}
println(resultList) // Prints: [Success(Result), Failure(java.lang.RuntimeException)]
```

After (with extensions):

```kotlin
val myList = listOf(Result.success("Result"), Result.failure(RuntimeException()))
val resultList = myList.filterResult { it.startsWith("Res") }
println(resultList) // Prints: [Success(Result), Failure(java.lang.RuntimeException)]
```

##### filterResultNot

This is the same as [filterResult](#filterResult) but returns the elements that *do not match* the
given predicate. Failures are not touched.

##### filterResultIsInstance

This is the same as [filterResult](#filterResult) but returns the elements that are instance of the
given class. Failures are not touched.

##### filterResultNotNull

This is the same as [filterResult](#filterResult) but returns only not null elements. Failures are
not touched.

#### Actions Extensions

These extensions make possible to take action when `Result<T>` is failure or success, encapsulating
the boilerplate code needed to do the check.

##### onEachResultSuccess

Before (without extensions):

```kotlin
val myList = listOf(Result.success("Result"), Result.failure(RuntimeException()))
myList.forEach { result ->
    result.onSuccess { println("My: $it") }
}
// Prints: My: Result
```

After (with extensions):

```kotlin
val myList = listOf(Result.success("Result"), Result.failure(RuntimeException()))
myList.onEachResultSuccess { println("My: $it") }
// Prints: My: Result
```

##### onEachResultFailure

This is the same as [onEachResultSuccess](#onEachResultSuccess) but executes the action on every
failure instead.