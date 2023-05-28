package it.polito.mad.sportapp.entities.firestore.utilities

import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*

/**
 * Generic error Type for a failed generic Firestore operation. It just returns a message()
 * string explaining the reason of the error
 */
interface FireErrorType {
    fun message(): String
}

private const val defaultErrorMessage = "FireError: a generic error occurred during Firebase operation"
private const val defaultGetItemErrorMessage = "FireError: item has not been found"
private const val defaultInsertItemErrorMessage = "FireError: a conflict occurred during the insertion of the item"
private const val defaultDeserializationErrorMessage = "FireError: an error occurred deserializing item from Firestore result"
private const val defaultSerializationErrorMessage = "FireError: an error occurred Serializing your item to an hash map"


/**
 * Default Firebase Error Type for generic operations
 */
class DefaultFireError(private val message: String = defaultErrorMessage) : FireErrorType {
    override fun message() = message

    companion object {
        fun <T> withMessage(customMessage: String): Error<T, DefaultFireError> {
            return Error(DefaultFireError(customMessage))
        }
    }
}

/**
 * Default Firebase Error Type for Get operations: it might be
 * - (1) a NOT_FOUND_ERROR, if the item has not been found in the dd
 * - (2) a DESERIALIZATION_ERROR, if the item has been correctly retrieved from the cloud but an
 * error occurred during deserialization
 * - or (3) a DEFAULT_FIRE_ERROR, in case of generic error
 */
enum class DefaultGetFireError(private var message: String) : FireErrorType {
    DEFAULT_FIRE_ERROR(defaultErrorMessage),
    NOT_FOUND_ERROR(defaultGetItemErrorMessage),
    DESERIALIZATION_ERROR(defaultDeserializationErrorMessage);

    override fun message() = message

    companion object {
        /**
         * Returns a DEFAULT_FIRE_ERROR instance with a custom error message
         */
        fun <T> default(message: String): Error<T,DefaultGetFireError> {
            return Error(DEFAULT_FIRE_ERROR.apply { this.message = message })
        }

        /**
         * Returns a NOT_FOUND_ERROR instance with a custom error message
         */
        fun <T> notFound(message: String): Error<T,DefaultGetFireError> {
            return Error(NOT_FOUND_ERROR.apply { this.message = message })
        }

        /**
         * Returns a DESERIALIZATION_ERROR instance with a custom error message
         */
        fun <T> duringDeserialization(message: String): Error<T,DefaultGetFireError> {
            return Error(DESERIALIZATION_ERROR.apply { this.message = message })
        }
    }
}

/**
 * Default Firebase Error Type for Insert operations: it might be
 * - (1) a CONFLICT_ERROR, if the insertion has encountered a conflict with regard to the db constraints
 * - (2) a SERIALIZATION_ERROR if an error occurred during the serialization phase into an hashmap
 * (before sending it to the cloud)
 * - or (3) a DEFAULT_FIRE_ERROR, in case of a generic error
 */
enum class DefaultInsertFireError(private var message: String) : FireErrorType {
    DEFAULT_FIRE_ERROR(defaultErrorMessage),
    CONFLICT_ERROR(defaultInsertItemErrorMessage),
    SERIALIZATION_ERROR(defaultSerializationErrorMessage);

    override fun message() = message

    companion object {
        /**
         * Returns a DEFAULT_FIRE_ERROR instance with a custom error message
         */
        fun <T> default(message: String): Error<T, DefaultInsertFireError> {
            return Error(DEFAULT_FIRE_ERROR.apply { this.message = message })
        }

        /**
         * Returns a CONFLICT_ERROR instance with a custom error message
         */
        fun <T> conflict(message: String): Error<T, DefaultInsertFireError> {
            return Error(CONFLICT_ERROR.apply { this.message = message })
        }

        /**
         * Returns a SERIALIZATION_ERROR instance with a custom error message
         */
        fun <T> duringSerialization(message: String): Error<T, DefaultInsertFireError> {
            return Error(SERIALIZATION_ERROR.apply { this.message = message })
        }
    }
}

enum class NewReservationError(val message: String) : FireErrorType {
    SLOT_CONFLICT(
        "Ouch! the selected slots have just been booked by someone else \uD83D\uDE41. Please select new ones for your reservation!"
    ),
    EQUIPMENT_CONFLICT(
        "Ouch! the selected equipments have just been booked by someone else \uD83D\uDE41. Please select new ones for your reservation!"
    ),
    UNEXPECTED_ERROR(
        "An unexpected error occurred while saving your reservation. Please try again or check your connection status."
    );

    private var customMessage = message

    override fun message(): String {
        return customMessage
    }

    companion object {
        fun <T> slotConflict(customMessage: String? = null): Error<T, NewReservationError> {
            return Error(SLOT_CONFLICT.also {
                if(customMessage != null)
                    it.customMessage = customMessage
            })
        }

        fun <T> equipmentConflict(customMessage: String? = null): Error<T, NewReservationError> {
            return Error(EQUIPMENT_CONFLICT.also{
                if(customMessage != null)
                    it.customMessage = customMessage
            })
        }

        fun <T> unexpected(customMessage: String? = null): Error<T, NewReservationError> {
            return Error(UNEXPECTED_ERROR.also{
                if(customMessage != null)
                    it.customMessage = customMessage
            })
        }
    }
}

