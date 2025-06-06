package com.tomatomediacenter.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

/**
 * Abstract class for a Use Case that returns a [Flow] of results.
 * This is suitable for operations that emit multiple values over time, such as
 * observing data changes from a database or receiving real-time updates.
 *
 * @param P The type of parameters required by this use case.
 * @param R The type of data emitted by the [Flow]. Each emission is wrapped in a [Result].
 * @property dispatcher The CoroutineDispatcher on which the Flow collection and emissions should occur. Defaults to IO dispatcher.
 */
abstract class FlowUseCase<in P, out R>(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    /**
     * Executes the use case with the given parameters, returning a [Flow] of [Result]s.
     *
     * The Flow is configured to run on the specified [dispatcher] and catches any exceptions
     * during its execution, emitting them as `Result.failure`.
     *
     * @param parameters The parameters required to execute the use case.
     * @return A [Flow] emitting [Result] objects. Each emission contains either the successful data [R] or an [Exception].
     */
    operator fun invoke(parameters: P): Flow<Result<R>> {
        return execute(parameters)
            .catch { e ->
                // Log.e("FlowUseCase", "Exception in ${this::class.simpleName}", e) // Optional logging
                emit(Result.failure(Exception(e))) // Ensure it's an Exception type
            }
            .flowOn(dispatcher) // Ensure the upstream flow runs on the specified dispatcher
    }

    /**
     * The core logic of the use case that produces the [Flow]. This method must be implemented by subclasses.
     * It will be executed and its emissions collected on the [dispatcher] specified during construction.
     *
     * @param parameters The parameters required to execute the use case.
     * @return A [Flow] that emits data of type [R].
     */
    protected abstract fun execute(parameters: P): Flow<Result<R>>
}

/**
 * A specialized version of [FlowUseCase] for operations that do not require any parameters
 * but return a [Flow].
 *
 * @param R The type of data emitted by the [Flow].
 * @property dispatcher The CoroutineDispatcher on which the Flow collection and emissions should occur.
 */
abstract class FlowUseCaseNoParams<out R>(dispatcher: CoroutineDispatcher = Dispatchers.IO) : FlowUseCase<Unit, R>(dispatcher) {

    /**
     * Executes the use case without parameters, returning a [Flow] of [Result]s.
     *
     * @return A [Flow] emitting [Result] objects.
     */
    operator fun invoke(): Flow<Result<R>> = invoke(Unit)

    // The execute method is already defined in the parent FlowUseCase<Unit, R>
    // Subclasses will implement: protected abstract fun execute(parameters: Unit): Flow<Result<R>>
}
