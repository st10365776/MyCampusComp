package com.example.mycampuscomp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts

class AttendanceActivity : AppCompatActivity() {

    private lateinit var attendanceStatus: TextView

    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedValue = result.data?.getStringExtra("QR_RESULT")
            if (scannedValue == VALID_ATTENDANCE_QR) {
                attendanceStatus.text = "Present ✓"
                attendanceStatus.setTextColor(getColor(R.color.accent_text))
                Toast.makeText(this, "Attendance marked successfully", Toast.LENGTH_LONG).show()
            } else {
                attendanceStatus.text = "Invalid QR Code"
                attendanceStatus.setTextColor(getColor(R.color.upcoming_icon_tint))
                Toast.makeText(this, "Invalid attendance QR code", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val VALID_ATTENDANCE_QR = "ATTENDANCE:SD2026"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_attendance)

        attendanceStatus =
            findViewById(R.id.tvAttendanceStatus)

        val scanButton =
            findViewById<Button>(R.id.btnScanQr)

        scanButton.setOnClickListener {

            val intent =
                Intent(
                    this,
                    QrScannerActivity::class.java
                )

            qrScannerLauncher.launch(intent)
        }
        val manualCodeButton =
            findViewById<Button>(R.id.btnManualCode)

        manualCodeButton.setOnClickListener {

            val input = EditText(this)

            input.hint = "Enter attendance code"
            input.setSingleLine(true)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Enter Attendance Code")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit") { _, _ ->

                    val enteredCode =
                        input.text.toString().trim()

                    if (enteredCode == VALID_ATTENDANCE_QR) {

                        attendanceStatus.text =
                            "Present ✓"

                        attendanceStatus.setTextColor(
                            getColor(R.color.accent_text)
                        )

                        Toast.makeText(
                            this,
                            "Attendance marked successfully",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        attendanceStatus.text =
                            "Invalid Code"

                        attendanceStatus.setTextColor(
                            getColor(R.color.upcoming_icon_tint)
                        )

                        Toast.makeText(
                            this,
                            "Invalid attendance code",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .create()

            dialog.show()
        }
    }
}