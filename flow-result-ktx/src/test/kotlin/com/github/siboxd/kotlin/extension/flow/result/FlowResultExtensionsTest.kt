package com.github.siboxd.kotlin.extension.flow.result

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@ExperimentalCoroutinesApi
internal class FlowResultExtensionsTest {

    private data class MyTestException(
        override val message: String? = null
    ) : RuntimeException(message)

    private val firstSuccessfulResult = Result.success("First")
    private val secondSuccessfulResult = Result.success("Second")
    private val firstFailedResult = Result.failure<String>(MyTestException("First"))
    private val secondFailedResult = Result.failure<String>(MyTestException("Second"))

    private val flowWithSuccessfulResults = flowOf(
        firstSuccessfulResult,
        secondSuccessfulResult,
    )
    private val flowWithFirstSuccessfulAndSecondFailed = flowOf(
        firstSuccessfulResult,
        secondFailedResult,
    )
    private val flowWithFirstFailedAndSecondSuccessful = flowOf(
        firstFailedResult,
        secondSuccessfulResult,
    )
    private val flowWithFailedResults = flowOf(
        firstFailedResult,
        secondFailedResult,
    )

    @Test
    fun `given flows of mixed results, when transformResult is called, then only successful results are transformed`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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
                    underTest.transformResult { emit(Result.success(1)) }.toList()
                )
            }
        }

    @Test
    fun `given exception throwing code block, when transformResult is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")
            flowWithSuccessfulResults
                .transformResult<_, Nothing> { throw exception }
                .catch {
                    assertSame(exception, it)
                }
                .collect()
        }

    @Test
    fun `given flows of mixed results, when transformResultWhile is called, then only successful results are transformed`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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
                    underTest.transformResultWhile { emit(Result.success(1)); true }.toList()
                )
            }
        }

    @Test
    fun `given exception throwing code block, when transformResultWhile is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")
            flowWithSuccessfulResults
                .transformResultWhile<_, Nothing> { throw exception }
                .catch {
                    assertSame(exception, it)
                }
                .collect()
        }

    @Test
    fun `given successful results flow, when transformResultWhile is called, then the returned flow is truncated when the condition becomes false`() =
        runTest {
            val actual = flowWithSuccessfulResults
                .transformResultWhile { emit(Result.success(1)); false }
                .toList()

            assertEquals(listOf(Result.success(1)), actual)
        }

    @Test
    fun `given flows of mixed results transformed with transformResult, when emitResult is called, then only successful results are transformed wrapping them into results`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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
                    underTest.transformResult { emitResult(1) }.toList()
                )
            }
        }

    @Test
    fun `given flows of mixed results, when filterResult is called, then only successful results are actually filtered retaining failures`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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

    @Test
    fun `given exception throwing code block, when filterResult is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")
            flowWithSuccessfulResults
                .filterResult { throw exception }
                .catch {
                    assertSame(exception, it)
                }
                .collect()
        }

    @Test
    fun `given flows of mixed results, when filterResultNot is called, then only successful results are actually filtered retaining failures`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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

    @Test
    fun `given exception throwing code block, when filterResultNot is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")
            flowWithSuccessfulResults
                .filterResultNot { throw exception }
                .catch {
                    assertSame(exception, it)
                }
                .collect()
        }

    @Test
    fun `given flows of mixed results, when filterResultIsInstance is called, then only successful results are actually filtered retaining failures`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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
    fun `given flows of mixed results, when filterResultNotNull is called, then only successful results are actually filtered retaining failures`() =
        runTest {
            val underTest = listOf(
                flow {
                    emit(Result.success(null))
                    emitAll(flowWithSuccessfulResults)
                    emit(Result.success(null))
                },
                flow {
                    emit(Result.success(null))
                    emitAll(flowWithFirstSuccessfulAndSecondFailed)
                    emit(Result.success(null))
                },
                flow {
                    emit(Result.success(null))
                    emitAll(flowWithFirstFailedAndSecondSuccessful)
                    emit(Result.success(null))
                },
                flow {
                    emit(Result.success(null))
                    emitAll(flowWithFailedResults)
                    emit(Result.success(null))
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
    fun `given flows of mixed results, when mapResult is called, then only successful results are mapped`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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

    @Test
    fun `given exception throwing code block, when mapResult is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")
            flowWithSuccessfulResults
                .mapResult { throw exception }
                .catch {
                    assertSame(exception, it)
                }
                .collect()
        }

    @Test
    fun `given flows of mixed results, when mapResultCatching is called, then only successful results are mapped`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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
    fun `given exception throwing code block, when mapResultCatching is called with it, then the exception is caught`() =
        runTest {
            val exception = MyTestException("Exception")
            val result = flowWithSuccessfulResults.mapResultCatching { throw exception }

            assertEquals(
                listOf(
                    Result.failure<String>(exception),
                    Result.failure(exception)
                ),
                result.toList()
            )
        }

    @Test
    fun `given flows of mixed results, when flatMapResult is called, then only successful results are mapped`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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

    @Test
    fun `given exception throwing code block, when flatMapResult is called with it, then the exception is caught`() =
        runTest {
            val exception = MyTestException("Exception")
            flowWithSuccessfulResults
                .flatMapResult<String, String> { throw exception }
                .catch {
                    assertSame(exception, it)
                }
                .collect()
        }

    @Test
    fun `given flows of mixed results, when flatMapResultCatching is called, then only successful results are mapped`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
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
    fun `given exception throwing code block, when flatMapResultCatching is called with it, then the exception is caught`() =
        runTest {
            val exception = MyTestException("Exception")
            val result =
                flowWithSuccessfulResults.flatMapResultCatching<String, String> { throw exception }

            assertEquals(
                listOf(
                    Result.failure<String>(exception),
                    Result.failure(exception)
                ),
                result.toList()
            )
        }

    @Test
    fun `given flows of mixed results, when mapInnerResults is called, then only successful results are mapped`() =
        runTest {
            val expected = listOf(
                listOf(listOf(Result.success(1), Result.success(1))),
                listOf(listOf(Result.success(1), secondFailedResult)),
                listOf(listOf(firstFailedResult, Result.success(1))),
                listOf(listOf(firstFailedResult, secondFailedResult)),
            )

            val underTest = listOf(
                flowOf(flowWithSuccessfulResults),
                flowOf(flowWithFirstSuccessfulAndSecondFailed),
                flowOf(flowWithFirstFailedAndSecondSuccessful),
                flowOf(flowWithFailedResults),
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
    fun `given exception throwing code block, when mapInnerResults is called with it, then the exception is caught`() =
        runTest {
            val exception = MyTestException("Exception")
            val result = flowOf(flowWithSuccessfulResults).mapInnerResults { throw exception }

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
    fun `given flows of mixed results, when flatMapInnerResults is called, then only successful results are mapped`() =
        runTest {
            val expected = listOf(
                listOf(listOf(Result.success(1), Result.success(1))),
                listOf(listOf(Result.success(1), secondFailedResult)),
                listOf(listOf(firstFailedResult, Result.success(1))),
                listOf(listOf(firstFailedResult, secondFailedResult)),
            )

            val underTest = listOf(
                flowOf(flowWithSuccessfulResults),
                flowOf(flowWithFirstSuccessfulAndSecondFailed),
                flowOf(flowWithFirstFailedAndSecondSuccessful),
                flowOf(flowWithFailedResults),
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
    fun `given exception throwing code block, when flatMapInnerResults is called with it, then the exception is caught`() =
        runTest {
            val exception = MyTestException("Exception")
            val result = flowOf(flowWithSuccessfulResults)
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
    fun `given flows of mixed results, when onEachResultSuccess is called, then only successful results are acted on`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
            )

            val expected = listOf(
                listOf("First", "Second"),
                listOf("First"),
                listOf("Second"),
                emptyList(),
            )

            underTest.zip(expected).forEach { (underTest, expected) ->
                val tempList = mutableListOf<String>()
                underTest.onEachResultSuccess { tempList.add(it) }.collect()
                assertEquals(expected, tempList)
            }
        }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when onEachResultSuccess is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")

            flowWithSuccessfulResults.onEachResultSuccess { throw exception }.collect()
        }

    @Test
    fun `given flows of mixed results, when onEachResultFailure is called, then only failure results are acted on`() =
        runTest {
            val underTest = listOf(
                flowWithSuccessfulResults,
                flowWithFirstSuccessfulAndSecondFailed,
                flowWithFirstFailedAndSecondSuccessful,
                flowWithFailedResults
            )

            val expected = listOf(
                emptyList(),
                listOf(MyTestException("Second")),
                listOf(MyTestException("First")),
                listOf(MyTestException("First"), MyTestException("Second")),
            )

            underTest.zip(expected).forEach { (underTest, expected) ->
                val tempList = mutableListOf<Throwable>()
                underTest.onEachResultFailure { tempList.add(it) }.collect()
                assertEquals(expected, tempList)
            }
        }

    @Test(expected = MyTestException::class)
    fun `given exception throwing code block, when onEachResultFailure is called with it, then the exception is re-thrown`() =
        runTest {
            val exception = MyTestException("Exception")

            flowWithFailedResults.onEachResultFailure { throw exception }.collect()
        }
}