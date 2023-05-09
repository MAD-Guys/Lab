package it.polito.mad.sportapp.reservation_management

import it.polito.mad.sportapp.R
import java.lang.IllegalArgumentException

enum class ReservationManagementMode(
    val appBarTitle: String,
) {
    ADD_MODE (
        "Reserve a slot",
    ),
    EDIT_MODE (
        "Edit reserved slot",
    );

    val menuResourceId: Int = R.menu.reservation_management_menu
    val variantColorId: Int = R.color.current_month_background_color_variant

    companion object {
        fun from(s: String?): ReservationManagementMode? {
            if (s == null) return null

            return try {
                ReservationManagementMode.valueOf("${s.uppercase()}_MODE")
            }
            catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}