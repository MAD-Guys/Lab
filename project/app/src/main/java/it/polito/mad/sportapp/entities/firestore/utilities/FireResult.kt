package it.polito.mad.sportapp.entities.firestore.utilities

/**
 * Class to represent the result of a general operation in the Firestore cloud db (ex: get, insert,
 *  update, delete, ecc.)
 *  It may be either a *Success<T>* instance, containing the result of type T for the related operation
 *  if it was successful, or an *Error* instance, containing an error message
 *
 *  Tip: to deal with a FireResult<T> instance, you have two alternatives:
 *  - check first if it is a Success<T> instance or an Error instance with the respective methods
 *    isSuccess() and isError(); their results are complementary (an instance must either be
 *    of type Success<T> or Error, and not both or none of them). E.g.:
 *
 *         if(fireResult.isSuccess()) {
 *          val value = fireResult.unwrap()
 *          ...
 *       }
 *       else {   // fireResult.isError()
 *          val errorMessage = fireResult.errorMessage()
 *          ...
 *       }
 *
 *  - use the 'when' clause to distinguish the instance type and execute different branches
 *    of code. E.g.:
 *
 *      when(fireResult) {
 *          is Success -> {
 *              // fireResult is of type Success<T> here
 *              ...
 *          }
 *          is Error -> {
 *              // fireResult is of type Error here
 *              ...
 *          }
 *      }
 *
 *  Note: if the operation does not have any explicit return value, T will be Unit
 */
sealed class FireResult<T, ErrorType: FireErrorType> {
    /**
     * Class representing a *Failed* operation's result in Firestore operations. It contains
     * an error explaining the reason of the failure.
     * You can access that error in the 'type' property, or with the error() methods. Each error type
     * has a related message, accessible with message() methods
     */
    class Error<T, ErrorType: FireErrorType>(
        val type: ErrorType
    ) : FireResult<T, ErrorType>()
    {
        override fun isError() = true
        override fun isSuccess() = false

        // *unsafe* un-wrappers
        override fun unwrap(): T {
            throw Exception("Error: cannot unwrap a FireResult.Error instance. This" +
                    "is an error with message: ${type.message()}")
        }

        override fun errorType() = type
        override fun errorMessage() = type.message()

        // *safe* un-wrappers
        override fun safeUnwrap(): T? = null
        override fun safeErrorType() = errorType()
        override fun safeErrorMessage() = errorMessage()
    }


    /**
     * Class representing a *Successful* operation's result in Firestore operations. It contains
     * the result (of type T) of that operation.
     * You can access that result value in the 'value' property, or with the unwrap() methods
     */
    class Success<T, ErrorType: FireErrorType>(
        val value: T
    ) : FireResult<T, ErrorType>()
    {
        override fun isError() = false
        override fun isSuccess() = true

        // *unsafe* un-wrappers
        override fun unwrap() = value
        override fun errorType(): ErrorType {
            throw Exception("Error: cannot retrieve an error type from a FireResult.Success instance. " +
                    "This instance contains the value: ${value.toString()}}")
        }
        override fun errorMessage(): String {
            throw Exception("Error: cannot retrieve an error message from a FireResult.Success instance. " +
                    "This instance contains the value: ${value.toString()}}")
        }

        // *safe* un-wrappers
        override fun safeUnwrap(): T? = unwrap()
        override fun safeErrorType() = null
        override fun safeErrorMessage() = null
    }

    /**
     * Returns true if this FireResult instance is an Error<ErrorType>; 'false' otherwise
     */
    abstract fun isError(): Boolean

    /**
     * Returns true if this FireResult instance is a Success<T>, containing a valid result T;
     * 'false' otherwise
     */
    abstract fun isSuccess(): Boolean

    // *unsafe* un-wrappers

    /**
     * Retrieve the value stored by the underlying Success<T> instance, if any;
     * it throws an exception if this instance is an Error<ErrorType>
     */
    abstract fun unwrap(): T

    /**
     * Retrieve the error type stored by the underlying Error<ErrorType> instance, if any;
     * it throws an exception if this instance is a Success
     */
    abstract fun errorType(): ErrorType


    /**
     * Retrieve the error message string stored by the underlying Error<ErrorType> instance, if any;
     * it throws an exception if this instance is a valid Success<T>
     */
    abstract fun errorMessage(): String

    // *safe* un-wrappers

    /**
     * *Safely* retrieve the value stored by the underlying Success<T> instance, if any;
     * it returns 'null' if this instance is an Error<ErrorType>
     */
    abstract fun safeUnwrap(): T?

    /**
     * *Safely* retrieve the error type stored by the underlying Error<ErrorType> instance, if any;
     * it returns 'null' if this instance is a Success<T>
     */
    abstract fun safeErrorType(): ErrorType?


    /**
     * *Safely* retrieve the error message string stored by the underlying Error<ErrorType> instance,
     * if any; it returns 'null' if this instance is a valid Success<T>
     */
    abstract fun safeErrorMessage(): String?
}
