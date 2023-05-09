package it.polito.mad.sportapp.reservation_details

import android.graphics.Bitmap
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.entities.DetailedReservation
import it.polito.mad.sportapp.entities.Equipment

/*  QR CODE */
fun reservationQRCode(r: DetailedReservation): Bitmap {

    lateinit var bitmap: Bitmap
    lateinit var qrEncoder: QRGEncoder

    val text = "Reservation number: ${r.id}\nUser: ${r.userId}"

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

/* MOCK RESERVATION DETAILS */
/*fun mockReservationDetails(): DetailedReservation {

    val res = DetailedReservation(
        123456,
        27,
        1,
        1,
        "POLI Sport",
        "Corso Castelfidardo 54, Torino (TO)",
        "Basketball",
        "2023-04-18 15:00:00",
        "2023-04-18 17:00:00",
        "Campo 1",
        25.50f
    )

    res.equipments = mutableListOf(
        EquipmentReservation(42, 123456, 66, 2, "timestamp", 5f),
        EquipmentReservation(43, 123456, 68, 2, "timestamp", 5f)
    )

    return res
}*/


/*
fun mockAvailableEquipment(): MutableList<Equipment> {
    return mutableListOf(
        Equipment(27, "Ball", 1, 1, 3.99f, 7),
        Equipment(29, "Bottle", 1, 1, 1.99f, 17),
        Equipment(66, "Shoes", 1, 1, 2.5f, 5),
        Equipment(68, "Towel", 1, 1,  2.5f, 12)
    )
}

 */