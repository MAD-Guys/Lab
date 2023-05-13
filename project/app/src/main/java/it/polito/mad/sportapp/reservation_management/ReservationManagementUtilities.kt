package it.polito.mad.sportapp.reservation_management

import android.os.Bundle
import androidx.core.os.bundleOf
import it.polito.mad.sportapp.R
import it.polito.mad.sportapp.entities.DetailedReservation
import java.lang.IllegalArgumentException
import java.time.Duration

enum class ReservationManagementMode(
    val appBarTitle: String,
) {
    ADD_MODE (
        "Reserve a slot",
    ),
    EDIT_MODE (
        "Edit reserved slot",
    );

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

class ReservationManagementModeWrapper(var mode: ReservationManagementMode?)

class ReservationManagementUtilities {
    companion object {
        fun createBundleFrom(reservation: DetailedReservation?, slotDuration: Duration): Bundle {
            return bundleOf(
                "playground_id" to reservation?.playgroundId,
                "start_slot" to reservation?.startLocalDateTime?.toString(),
                "end_slot" to reservation?.endLocalDateTime?.minus(slotDuration)?.toString(),
                "sport_id" to reservation?.sportId,
                "slot_duration_mins" to 30
            )
        }
    }
}