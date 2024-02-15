package org.hyperskill.phrases

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(
    private val textViewUpdater: TextViewUpdater,
    private val context: Context
) :
    RecyclerView.Adapter<RecyclerAdapter.PhrasesViewHolder>() {

    private val database: AppDatabase = (context.applicationContext as PhraseApplication).database
    var phrases: MutableList<Phrase> = database.getPhraseDao().getAll().toMutableList()
        set(value) {
            field = value
            notifyItemInserted(field.size - 1)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhrasesViewHolder {
        return PhrasesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_phrase, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhrasesViewHolder, position: Int) {
        val phrase = phrases[position]

        holder.apply {
            phraseTV.text = phrase.text
            deleteTV.setOnClickListener {
                remove(phrase)
            }
        }
    }

    override fun getItemCount(): Int {
        return phrases.size
    }

    private fun remove(phrase: Phrase) {
        val index = phrases.indexOf(phrase)
        phrases.removeAt(index)
        notifyItemRemoved(index)
        database.getPhraseDao().delete(phrase)
        Log.i(
            "Database",
            "${database.getPhraseDao().getAll()}"
        )
        if (database.getPhraseDao().getAll().isEmpty()) {
            cancelAlarms()
            textViewUpdater.updateTextView(context.getString(R.string.no_reminder_set))
        }
    }

    private fun cancelAlarms() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    class PhrasesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phraseTV: TextView = view.findViewById(R.id.phraseTextView)
        val deleteTV: TextView = view.findViewById(R.id.deleteTextView)
    }
}