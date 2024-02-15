package org.hyperskill.phrases

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.hyperskill.phrases.databinding.ActivityMainBinding

class MainActivity :
    AppCompatActivity(),
    TimePickerDialog.OnTimeSetListener,
    TextViewUpdater {

    private lateinit var binding: ActivityMainBinding

    private lateinit var recyclerAdapter: RecyclerAdapter

    private lateinit var appDatabase: AppDatabase

    private var hourOfDay = -1
    private var minute = -1

    private val CHANNEL_ID = "org.hyperskill.phrases"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = (application as PhraseApplication).database

        recyclerAdapter = RecyclerAdapter(
            this,
            this
        )

        binding.recyclerView.apply {
            adapter = recyclerAdapter
        }

        // Creating notification channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Phrase",
            NotificationManager.IMPORTANCE_HIGH
        )

        val mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.createNotificationChannel(channel)

        binding.reminderTextView.setOnClickListener {
            if (appDatabase.getPhraseDao().getAll().isEmpty()) {
                Toast.makeText(
                    this,
                    "Add at least 1 phrase",
                    Toast.LENGTH_SHORT
                ).show()
                binding.reminderTextView.text = getString(R.string.no_reminder_set)
                this.hourOfDay = -1
                this.minute = -1
            } else {
                showTimePickerDialog()
            }
        }

        binding.addButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.add_phrase, null, false)
            AlertDialog.Builder(this)
                .setTitle("Add phrase")
                .setView(contentView)
                .setPositiveButton("Add") { _, _ ->
                    val editText: EditText = contentView.findViewById(R.id.editText)
                    val phrase = Phrase(editText.text.toString())

                    appDatabase.getPhraseDao().insert(phrase)
                    recyclerAdapter.phrases = appDatabase.getPhraseDao().getAll().toMutableList()

                    Log.i("Database", appDatabase.getPhraseDao().getAll().toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setReminderTime(hourOfDay: Int, minute: Int) {
        this.hourOfDay = hourOfDay
        this.minute = minute
        updateReminderText()
        updateAlarmManagerTime()
    }

    private fun updateReminderText() {
        updateTextView(
            if (hourOfDay >= 0 || minute >= 0) {
                "Reminder set for %02d:%02d".format(hourOfDay, minute)
            } else {
                getString(R.string.no_reminder_set)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAlarmManagerTime() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(applicationContext, Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val scheduledTime = Calendar.getInstance()
        scheduledTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
        scheduledTime.set(Calendar.MINUTE, minute)
        scheduledTime.set(Calendar.SECOND, 0)

        val currentTime = Calendar.getInstance()
        if (scheduledTime.before(currentTime)) {
            scheduledTime.add(Calendar.DATE, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            scheduledTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun showTimePickerDialog(){
        val c = java.util.Calendar.getInstance()
        val hour = c.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = c.get(java.util.Calendar.MINUTE)

        TimePickerDialog(
            this,
            this,
            hour,
            minute,
            true
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        setReminderTime(hourOfDay, minute)
    }


    override fun updateTextView(text: String) {
        binding.reminderTextView.text = text
    }
}

