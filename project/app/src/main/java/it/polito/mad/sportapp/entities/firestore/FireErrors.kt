package it.polito.mad.sportapp.entities.firestore

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
class DefaultFireError(val message: String=defaultErrorMessage) : FireErrorType {
    override fun message() = message
}

/**
 * Default Firebase Error Type for Get operations: it might be
 * - (1) a NOT_FOUND_ERROR, if the item has not been found in the dd
 * - (2) a DESERIALIZATION_ERROR, if the item has been correctly retrieved from the cloud but an
 * error occurred during deserialization
 * - or (3) a DEFAULT_FIRE_ERROR, in case of generic error
 */
enum class GetItemFireError(private var message: String) : FireErrorType {
    DEFAULT_FIRE_ERROR(defaultErrorMessage),
    NOT_FOUND_ERROR(defaultGetItemErrorMessage),
    DESERIALIZATION_ERROR(defaultDeserializationErrorMessage);

    override fun message() = message

    companion object {
        /**
         * Returns a DEFAULT_FIRE_ERROR instance with a custom error message
         */
        fun default(message: String): GetItemFireError {
            return DEFAULT_FIRE_ERROR.apply { this.message = message }
        }

        /**
         * Returns a NOT_FOUND_ERROR instance with a custom error message
         */
        fun notFound(message: String): GetItemFireError {
            return NOT_FOUND_ERROR.apply { this.message = message }
        }

        /**
         * Returns a DESERIALIZATION_ERROR instance with a custom error message
         */
        fun duringDeserialization(message: String): GetItemFireError {
            return DESERIALIZATION_ERROR.apply { this.message = message }
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
enum class InsertItemFireError(private var message: String) : FireErrorType {
    DEFAULT_FIRE_ERROR(defaultErrorMessage),
    CONFLICT_ERROR(defaultInsertItemErrorMessage),
    SERIALIZATION_ERROR(defaultSerializationErrorMessage);

    override fun message() = message

    companion object {
        /**
         * Returns a DEFAULT_FIRE_ERROR instance with a custom error message
         */
        fun default(message: String): InsertItemFireError {
            return DEFAULT_FIRE_ERROR.apply { this.message = message }
        }

        /**
         * Returns a CONFLICT_ERROR instance with a custom error message
         */
        fun conflict(message: String): InsertItemFireError {
            return CONFLICT_ERROR.apply { this.message = message }
        }

        /**
         * Returns a SERIALIZATION_ERROR instance with a custom error message
         */
        fun duringSerialization(message: String): InsertItemFireError {
            return SERIALIZATION_ERROR.apply { this.message = message }
        }
    }
}
