package it.polito.mad.sportapp.reservation_details

import android.graphics.Bitmap
import android.widget.ImageView
import it.polito.mad.sportapp.localDB.PlaygroundReservation
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import it.polito.mad.sportapp.localDB.Equipment
import it.polito.mad.sportapp.localDB.SportCenter
import java.sql.Date
import java.sql.Time
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/*  QR CODE */
fun reservationQRCode(r: ReservationDetails): Bitmap {

    lateinit var bitmap: Bitmap
    lateinit var qrEncoder: QRGEncoder

    val text = "Reservation number " + String.format("%010d", r.getId()) + "\nUser: ${r.getUserName()}"

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

fun setQRCodeView(reservation: ReservationDetails, imageView: ImageView){
    val qrCode :Bitmap = reservationQRCode(reservation)
    imageView.setImageBitmap(qrCode)
}

/* MOCK RESERVATION DETAILS */
class MockReservationDetails : ReservationDetails{
    override fun getId(): Int {
        return 123456
    }

    override fun getUserName(): String {
        return "@johndoe"
    }

    override fun getDate(): LocalDate {
        return LocalDate.parse("2023-04-18")
    }

    override fun getStartTime(): LocalTime {
        return  LocalTime.parse("15:00")
    }

    override fun getEndTime(): LocalTime {
        return LocalTime.parse("17:00")
    }

    override fun getSport(): String {
        return "Basketball"
    }

    override fun getPlaygroundName(): String {
        return "Campo 1"
    }

    override fun getSportCenter(): SportCenter {
        return SportCenter(
            12,
            "POLI Sport",
            "Corso Castelfidardo 54, Torino (TO)",
            "description",
            "3456789012",
            "8:00",
            "22:00")
    }

    override fun getEquipment(): List<Equipment> {
        return listOf<Equipment>(
            Equipment(888, "Ball", 1, 12, 5.50 as Float, 20),
            Equipment(999, "Shoes", 1, 12, 10 as Float, 4)
        )
    }

    override fun getTotalPrice(): Float {
        return 25.50.toFloat()
    }
}
