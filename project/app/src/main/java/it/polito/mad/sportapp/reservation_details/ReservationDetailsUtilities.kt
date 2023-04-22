package it.polito.mad.sportapp.reservation_details

import android.graphics.Bitmap
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import it.polito.mad.sportapp.entities.EquipmentReservation
import it.polito.mad.sportapp.localDB.DetailedReservation
import java.sql.Time
import java.time.Instant

/*  QR CODE */
fun reservationQRCode(r: DetailedReservation): Bitmap {

    lateinit var bitmap: Bitmap
    lateinit var qrEncoder: QRGEncoder

    val text = "Reservation number " + String.format("%010d", r.id) + "\nUser: ${r.userId}"

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

fun setQRCodeView(reservation: DetailedReservation, imageView: ImageView){
    val qrCode :Bitmap = reservationQRCode(reservation)
    imageView.setImageBitmap(qrCode)
}

/* MOCK RESERVATION DETAILS */
fun mockReservationDetails() : DetailedReservation {

    val res = DetailedReservation(
        123456,
        27,
        "POLI Sport",
        "Corso Castelfidardo 54, Torino (TO)",
        "Basketball",
        "2023-04-18 15:00",
        "2023-04-18 17:00",
        "Campo 1",
        25.50f
    )

    res.equipments = mutableListOf(
        EquipmentReservation(42, 123456, 66, 2, "timestamp", 5f),
        EquipmentReservation(43, 123456, 68, 2, "timestamp", 5f)
    )

    return  res
}
