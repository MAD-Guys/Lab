package it.polito.mad.sportapp.reservation_details

import android.graphics.Bitmap
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.User
import java.time.format.DateTimeFormatter
import java.util.Date

/*  QR CODE */
fun reservationQRCode(r: DetailedReservation): Bitmap {

    lateinit var bitmap: Bitmap
    lateinit var qrEncoder: QRGEncoder

    val text = """
        {
            "reservation_number": "${r.id}",
            "user_id": "${r.userId}"
        }
    """.trimIndent()

    qrEncoder = QRGEncoder(text, null, QRGContents.Type.TEXT, 512)

    // on below line we are running a try
    // and catch block for initializing our bitmap
    try {
        // on below line we are
        // initializing our bitmap
        bitmap = qrEncoder.getBitmap(1)

    } catch (e: Exception) {
        // on below line we
        // are handling exception
        e.printStackTrace()
    }

    return bitmap
}

internal fun setQRCodeView(reservation: DetailedReservation, imageView: ImageView) {
    val qrCode: Bitmap = reservationQRCode(reservation)
    imageView.setImageBitmap(qrCode)
}

private class PassReservationInfo(
    val reservationId: String,
    val userId: String,
    val playgroundId: String,
    val startTime: String,
    val endTime: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassReservationInfo

        if (reservationId != other.reservationId) return false
        if (userId != other.userId) return false
        if (playgroundId != other.playgroundId) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reservationId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + playgroundId.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }

    val hash: String
        get() {
            return hashCode().toString()
        }
}

internal fun createJsonPass(reservation: DetailedReservation, user: User): String {
    val reservationId = reservation.id
    val userId = user.id
    val playgroundId = reservation.playgroundId
    val startTime = reservation.startLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    val endTime = reservation.endLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME)

    // create a unique object corresponding to reservation info ->
    // its hash will be used as part of the google pass ID
    // (so if the reservation changes, its hash changes and the google pass too)
    val passReservationInfo = PassReservationInfo(reservationId, userId!!, playgroundId, startTime, endTime)

    val pass = """
            {
                "iss":"mariomastrandrea.mate@gmail.com",
                "aud":"google",
                "typ":"savetowallet",
                "iat":${Date().time / 1000L},
                "origins":[],
                "payload":{
                    "genericObjects":[
                        {
                            "id":"3388000000022238618.${reservation.id}.${passReservationInfo.hash}",
                            "classId":"3388000000022238618.GenericReservation",
                            "genericType":"GENERIC_TYPE_UNSPECIFIED",
                            "cardTitle":{
                                "defaultValue":{
                                    "language":"en",
                                    "value":"EzSport Reservation"
                                }
                            },
                            "header": {
                                "defaultValue":{
                                    "language":"en",
                                    "value":"${user.firstName} ${user.lastName}"
                                }
                            },
                            "subHeader": {
                                "defaultValue":{
                                    "language":"en",
                                    "value":"${user.username}"
                                }
                            },
                            "barcode": {
                                "alternateText": "${reservation.id}",
                                "type": "qrCode",
                                "value": "{
                                    \"reservation_number\": \"${reservation.id}\",
                                    \"user_id\": \"${user.id}\"
                                }"
                            },
                            "textModulesData": [
                                {
                                    "header": "Sport Center",
                                    "body": "${reservation.sportCenterName}"
                                },
                                {
                                    "header": "Playground",
                                    "body": "${reservation.playgroundName}"
                                },
                                {
                                    "header": "Sport",
                                    "body": "${reservation.sportName} ${reservation.sportEmoji}"
                                },
                                {
                                    "header": "Date",
                                    "body": "${reservation.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                                },
                                {
                                    "header": "Time",
                                    "body": "${reservation.startTime}-${reservation.endTime}"
                                },
                                {
                                    "header": "${if(reservation.userId == user.id) "Owner" else "Invited by"}",
                                    "body": "${reservation.username}"
                                }
                            ]
                        }  
                    ]
                }
            }
        """.trimIndent()

    return pass
}