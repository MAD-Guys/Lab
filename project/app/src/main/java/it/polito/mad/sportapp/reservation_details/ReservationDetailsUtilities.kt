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

    val sportPictureUrl = when(reservation.sportId) {
        // minigolf
        "4nFO9rfxo6iIJVTluCcn" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Fminigolf.png?alt=media&token=fab7d2c6-09dc-418e-bb52-a43b509c06ef"
        // 5-a-side soccer
        "7AIqD0iwHOW6FIycvlwo" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Ffootball5.png?alt=media&token=87b65882-410d-4c9c-a32a-2751a2de9484"
        // table tennis
        "RQgUy37JaJcE8uRmLanb" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2FtableTennis.png?alt=media&token=00910834-198c-420c-a8cf-768ffc37dd47"
        // basketball
        "ZoasHiiaJ3CoNWMEr3RF" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Fbasketball.png?alt=media&token=c809096f-7c22-4102-9332-08125a4d6630"
        // volleyball
        "dU8Nvc3SfXfYaQKYzRbr" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Fvolleyball.png?alt=media&token=4351ff7b-b690-434f-9970-d398e5515cb6"
        // padel
        "fpkrSYDrMUDdqZ4kPfOc" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Fpadel.png?alt=media&token=1012cd8a-de0a-4171-ace5-249c246a6932"
        // beach volley
        "plGE1kMDKhqE17Azvdw8" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2FbeachVolley.png?alt=media&token=e0b3329a-cd58-4878-ad57-03c6e3e8c1fd"
        // 8-a-side soccer
        "qrwiJsMOa3eCiq6fwOW2" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Ffootball8.png?alt=media&token=a2a1b1a2-3814-4cdf-a96f-dbbc5e320e60"
        // 11-a-side soccer
        "te2BgJjzIJbC9qTgLrT4" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Ffootball11.png?alt=media&token=b9d244d4-00a3-4c51-a3cb-203376eb4e06"
        // tennis
        "x7f9jrM9BTiMoIFoyVFq" -> "https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/sport_pictures%2Ftennis.png?alt=media&token=40b9b6a7-532c-46d5-a29b-faee1d5b4a82"

        else -> throw Exception("Sport not found")
    }

    val heroImageField = """
        "heroImage": {
            "sourceUri": {
                "uri": "$sportPictureUrl"
            }
        },
    """.trimIndent()

    val pass = """
            {
                "iss":"mariomastrandrea.mate@gmail.com",
                "aud":"google",
                "typ":"savetowallet",
                "iat":${Date().time / 1000L},
                "origins":[],
                "payload":{
                    "genericClasses": [
                        {
                            "id": "3388000000022238618.GenericReservationWithInfo",
                            "classTemplateInfo": {
                                "cardTemplateOverride": {
                                    "cardRowTemplateInfos": [
                                        {
                                            "twoItems": {
                                                "startItem": {
                                                    "firstValue": {
                                                        "fields": [
                                                            {
                                                                "fieldPath": "object.textModulesData['playground']"
                                                            }
                                                        ]
                                                    }
                                                },
                                                "endItem": {
                                                    "firstValue": {
                                                        "fields": [
                                                            {
                                                                "fieldPath": "object.textModulesData['sport_center']"
                                                            }
                                                        ]
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            "twoItems": {
                                                "startItem": {
                                                    "firstValue": {
                                                        "fields": [
                                                            {
                                                                "fieldPath": "object.textModulesData['date']"
                                                            }
                                                        ]
                                                    }
                                                },
                                                "endItem": {
                                                    "firstValue": {
                                                        "fields": [
                                                            {
                                                                "fieldPath": "object.textModulesData['time']"
                                                            }
                                                        ]
                                                    }
                                                }
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    ],
                    "genericObjects":[
                        {
                            "id":"3388000000022238618.${reservation.id}.${passReservationInfo.hash}",
                            "classId":"3388000000022238618.GenericReservationWithInfo",
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
                            "subheader": {
                                "defaultValue":{
                                    "language":"en",
                                    "value":"${if (user.id == reservation.userId) "Organizer" else "Participant" }"
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
                            "logo":{
                                "sourceUri":{
                                    "uri":"https://firebasestorage.googleapis.com/v0/b/sportapp-project.appspot.com/o/ic_launcher-playstore.png?alt=media&token=89aa35a6-3bbb-41a0-b104-f86f1f730ff6"
                                }
                            },
                            "hexBackgroundColor": "#e25842",
                            "heroImage": {
                                "sourceUri": {
                                    "uri": "$sportPictureUrl"
                                }
                            },
                            "textModulesData": [
                                {
                                    "id": "sport_center",
                                    "header": "Sport Center",
                                    "body": "${reservation.sportCenterName}"
                                },
                                {
                                    "id": "playground",
                                    "header": "Playground",
                                    "body": "${reservation.playgroundName}"
                                },
                                {
                                    "header": "Sport",
                                    "body": "${reservation.sportName} ${reservation.sportEmoji}"
                                },
                                {
                                    "id": "date",
                                    "header": "Date",
                                    "body": "${reservation.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                                },
                                {
                                    "id": "time",
                                    "header": "Time",
                                    "body": "${reservation.startTime}-${reservation.endTime}"
                                },
                                {
                                    "header": "Organizer",
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