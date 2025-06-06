package com.tomatomediacenter.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Abstract class for a Use Case (Interactor in Clean Architecture).
 * This class represents a single business operation that is executed with specific parameters.
 * It is designed to be executed off the main thread.
 *
 * @param P The type of parameters required by this use case.
 * @param R The type of result returned by this use case.
 * @property dispatcher The CoroutineDispatcher on which the use case will be executed. Defaults to IO dispatcher.
 */
abstract class UseCase<in P, out R>(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    /**
     * Executes the use case with the given parameters.
     *
     * This method handles switching to the specified [dispatcher] (typically IO or Default)
     * for execution and wraps the result or any thrown exception in a [Result] object.
     *
     * @param parameters The parameters required to execute the use case.
     * @return A [Result] object containing either the successful result [R] or an [Exception].
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            // Switch to the specified dispatcher (e.g., Dispatchers.IO) for execution
            withContext(dispatcher) {
                execute(parameters).let {
                    Result.success(it)
                }
            }
        } catch (e: Exception) {
            // Catch any exceptions thrown during the execution of the use case
            // Log.e("UseCase", "Exception in ${this::class.simpleName}", e) // Optional logging
            Result.failure(e)
        }
    }

    /**
     * The core logic of the use case. This method must be implemented by subclasses.
     * It will be executed on the [dispatcher] specified during construction.
     *
     * @param parameters The parameters required to execute the use case.
     * @return The result [R] of the use case execution.
     * @throws Exception if the use case encounters an error.
     */
    @Throws(Exception::class)
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * A specialized version of [UseCase] for operations that do not require any parameters.
 *
 * @param R The type of result returned by this use case.
 * @property dispatcher The CoroutineDispatcher on which the use case will be executed.
 */
abstract class UseCaseNoParams<out R>(dispatcher: CoroutineDispatcher = Dispatchers.IO) : UseCase<Unit, R>(dispatcher) {

    /**
     * Executes the use case without parameters.
     *
     * @return A [Result] object containing either the successful result [R] or an [Exception].
     */
    suspend operator fun invoke(): Result<R> = invoke(Unit)

    // The execute method is already defined in the parent UseCase<Unit, R>
    // Subclasses will implement: protected abstract suspend fun execute(parameters: Unit): R
}
