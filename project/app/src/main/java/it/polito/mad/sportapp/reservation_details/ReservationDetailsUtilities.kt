package it.polito.mad.sportapp.reservation_details

import android.graphics.Bitmap
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import it.polito.mad.sportapp.entities.room.RoomDetailedReservation

/*  QR CODE */
fun reservationQRCode(r: RoomDetailedReservation): Bitmap {

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

internal fun setQRCodeView(reservation: RoomDetailedReservation, imageView: ImageView) {
    val qrCode: Bitmap = reservationQRCode(reservation)
    imageView.setImageBitmap(qrCode)
}