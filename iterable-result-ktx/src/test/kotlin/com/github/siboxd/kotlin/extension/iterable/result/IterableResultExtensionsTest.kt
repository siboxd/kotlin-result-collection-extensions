package com.github.siboxd.kotlin.extension.iterable.result

import org.junit.Assert.*
import org.junit.Test

internal class IterableResultExtensionsTest {

    private data class MyTestException(
        override val message: String? = null
    ) : RuntimeException(message)

    private val firstSuccessfulResult = Result.success("First")
    private val secondSuccessfulResult = Result.success("Second")
    private val firstFailedResult = Result.failure<String>(MyTestException("First"))
    private val secondFailedResult = Result.failure<String>(MyTestException("Second"))

    private val listWithSuccessfulResults = listOf(
        firstSuccessfulResult,
        secondSuccessfulResult,
    )
    private val listWithFirstSuccessfulAndSecondFailed = listOf(
        firstSuccessfulResult,
        secondFailedResult,
    )
    private val listWithFirstFailedAndSecondSuccessful = listOf(
        firstFailedResult,
        secondSuccessfulResult,
    )
    private val listWithFailedResults = listOf(
        firstFailedResult,
        secondFailedResult,
    )

    @Test
    fun `given lists of mixed results, when mapResult is called, then only successful results are mapped`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(Result.success(1), Result.success(1)),
            listOf(Result.success(1), secondFailedResult),
            listOf(firstFailedResult, Result.success(1)),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(expected, underTest.mapResult { 1 }.toList())
        }
    }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when mapResult is called with it, then the exception is re-thrown`() {
        val exception = MyTestException("Exception")
        listWithSuccessfulResults
            .mapResult { throw exception }
    }

    @Test
    fun `given lists of mixed results, when mapResultCatching is called, then only successful results are mapped`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(Result.success(1), Result.success(1)),
            listOf(Result.success(1), secondFailedResult),
            listOf(firstFailedResult, Result.success(1)),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(expected, underTest.mapResultCatching { 1 }.toList())
        }
    }

    @Test
    fun `given exception throwing code block, when mapResultCatching is called with it, then the exception is caught`() {
        val exception = MyTestException("Exception")
        val result = listWithSuccessfulResults.mapResultCatching { throw exception }

        assertEquals(
            listOf(
                Result.failure<String>(exception),
                Result.failure(exception)
            ),
            result.toList()
        )
    }

    @Test
    fun `given lists of mixed results, when flatMapResult is called, then only successful results are mapped`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(Result.success(1), Result.success(1)),
            listOf(Result.success(1), secondFailedResult),
            listOf(firstFailedResult, Result.success(1)),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(expected, underTest.flatMapResult { Result.success(1) }.toList())
        }
    }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when flatMapResult is called with it, then the exception is caught`() {
        val exception = MyTestException("Exception")
        listWithSuccessfulResults
            .flatMapResult<String, String> { throw exception }
    }

    @Test
    fun `given lists of mixed results, when flatMapResultCatching is called, then only successful results are mapped`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(Result.success(1), Result.success(1)),
            listOf(Result.success(1), secondFailedResult),
            listOf(firstFailedResult, Result.success(1)),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(
                expected,
                underTest.flatMapResultCatching { Result.success(1) }.toList()
            )
        }
    }

    @Test
    fun `given exception throwing code block, when flatMapResultCatching is called with it, then the exception is caught`() {
        val exception = MyTestException("Exception")
        val result =
            listWithSuccessfulResults.flatMapResultCatching<String, String> { throw exception }

        assertEquals(
            listOf(
                Result.failure<String>(exception),
                Result.failure(exception)
            ),
            result.toList()
        )
    }

    @Test
    fun `given lists of mixed results, when mapInnerResults is called, then only successful results are mapped`() {
        val expected = listOf(
            listOf(listOf(Result.success(1), Result.success(1))),
            listOf(listOf(Result.success(1), secondFailedResult)),
            listOf(listOf(firstFailedResult, Result.success(1))),
            listOf(listOf(firstFailedResult, secondFailedResult)),
        )

        val underTest = listOf(
            listOf(listWithSuccessfulResults),
            listOf(listWithFirstSuccessfulAndSecondFailed),
            listOf(listWithFirstFailedAndSecondSuccessful),
            listOf(listWithFailedResults),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(
                expected,
                underTest.mapInnerResults { 1 }
                    .map { it.toList() }
                    .toList()
            )
        }
    }

    @Test
    fun `given exception throwing code block, when mapInnerResults is called with it, then the exception is caught`() {
        val exception = MyTestException("Exception")
        val result = listOf(listWithSuccessfulResults).mapInnerResults { throw exception }

        assertEquals(
            listOf(
                listOf(
                    Result.failure<String>(exception),
                    Result.failure(exception)
                )
            ),
            result.map { it.toList() }.toList()
        )
    }

    @Test
    fun `given lists of mixed results, when flatMapInnerResults is called, then only successful results are mapped`() {
        val expected = listOf(
            listOf(listOf(Result.success(1), Result.success(1))),
            listOf(listOf(Result.success(1), secondFailedResult)),
            listOf(listOf(firstFailedResult, Result.success(1))),
            listOf(listOf(firstFailedResult, secondFailedResult)),
        )

        val underTest = listOf(
            listOf(listWithSuccessfulResults),
            listOf(listWithFirstSuccessfulAndSecondFailed),
            listOf(listWithFirstFailedAndSecondSuccessful),
            listOf(listWithFailedResults),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(
                expected,
                underTest.flatMapInnerResults { Result.success(1) }
                    .map { it.toList() }
                    .toList()
            )
        }
    }

    @Test
    fun `given exception throwing code block, when flatMapInnerResults is called with it, then the exception is caught`() {
        val exception = MyTestException("Exception")
        val result = listOf(listWithSuccessfulResults)
            .flatMapInnerResults<String, String> { throw exception }

        assertEquals(
            listOf(
                listOf(
                    Result.failure<String>(exception),
                    Result.failure(exception)
                )
            ),
            result.map { it.toList() }.toList()
        )
    }

    @Test
    fun `given lists of mixed results, when filterResult is called, then only successful results are actually filtered retaining failures`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(secondSuccessfulResult),
            listOf(secondFailedResult),
            listOf(firstFailedResult, secondSuccessfulResult),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(
                expected,
                underTest.filterResult { it == secondSuccessfulResult.getOrNull() }.toList()
            )
        }
    }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when filterResult is called with it, then the exception is re-thrown`() {
        val exception = MyTestException("Exception")
        listWithSuccessfulResults
            .filterResult { throw exception }
    }

    @Test
    fun `given lists of mixed results, when filterResultNot is called, then only successful results are actually filtered retaining failures`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(firstSuccessfulResult),
            listOf(firstSuccessfulResult, secondFailedResult),
            listOf(firstFailedResult),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(
                expected,
                underTest.filterResultNot { it == secondSuccessfulResult.getOrNull() }.toList()
            )
        }
    }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when filterResultNot is called with it, then the exception is re-thrown`() {
        val exception = MyTestException("Exception")
        listWithSuccessfulResults
            .filterResultNot { throw exception }
    }

    @Test
    fun `given lists of mixed results, when filterResultIsInstance is called, then only successful results are actually filtered retaining failures`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf(),
            listOf(secondFailedResult),
            listOf(firstFailedResult),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            // suppressing this since the actual test is about checking if a condition known to fail, would filter out failures or not
            @Suppress("AssertBetweenInconvertibleTypes")
            assertEquals(
                expected,
                underTest.filterResultIsInstance<Int>().toList()
            )
        }
    }

    @Test
    fun `given lists of mixed results, when filterResultNotNull is called, then only successful results are actually filtered retaining failures`() {
        val underTest = listOf(
            buildList {
                add(Result.success(null))
                addAll(listWithSuccessfulResults)
                add(Result.success(null))
            },
            buildList {
                add(Result.success(null))
                addAll(listWithFirstSuccessfulAndSecondFailed)
                add(Result.success(null))
            },
            buildList {
                add(Result.success(null))
                addAll(listWithFirstFailedAndSecondSuccessful)
                add(Result.success(null))
            },
            buildList {
                add(Result.success(null))
                addAll(listWithFailedResults)
                add(Result.success(null))
            }
        )

        val expected = listOf(
            listOf(firstSuccessfulResult, secondSuccessfulResult),
            listOf(firstSuccessfulResult, secondFailedResult),
            listOf(firstFailedResult, secondSuccessfulResult),
            listOf(firstFailedResult, secondFailedResult),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            assertEquals(
                expected,
                underTest.filterResultNotNull().toList()
            )
        }
    }

    @Test
    fun `given lists of mixed results, when onEachResultSuccess is called, then only successful results are acted on`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            listOf("First", "Second"),
            listOf("First"),
            listOf("Second"),
            emptyList(),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            val tempList = mutableListOf<String>()
            underTest.onEachResultSuccess { tempList.add(it) }
            assertEquals(expected, tempList)
        }
    }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when onEachResultSuccess is called with it, then the exception is re-thrown`() {
        val exception = MyTestException("Exception")

        listWithSuccessfulResults.onEachResultSuccess { throw exception }
    }

    @Test
    fun `given lists of mixed results, when onEachResultFailure is called, then only failure results are acted on`() {
        val underTest = listOf(
            listWithSuccessfulResults,
            listWithFirstSuccessfulAndSecondFailed,
            listWithFirstFailedAndSecondSuccessful,
            listWithFailedResults
        )

        val expected = listOf(
            emptyList(),
            listOf(MyTestException("Second")),
            listOf(MyTestException("First")),
            listOf(MyTestException("First"), MyTestException("Second")),
        )

        underTest.zip(expected).forEach { (underTest, expected) ->
            val tempList = mutableListOf<Throwable>()
            underTest.onEachResultFailure { tempList.add(it) }
            assertEquals(expected, tempList)
        }
    }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when onEachResultFailure is called with it, then the exception is re-thrown`() {
        val exception = MyTestException("Exception")

        listWithFailedResults.onEachResultFailure { throw exception }
    }
}