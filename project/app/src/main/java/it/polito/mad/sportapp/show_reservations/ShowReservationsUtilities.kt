package it.polito.mad.sportapp.show_reservations

/* Show Reservations Activity utilities */

// capitalize the first letter of the string
internal fun capitalizeFirstLetter(string: String): String {
    return string.substring(0, 1).uppercase() + string.substring(1).lowercase()
}