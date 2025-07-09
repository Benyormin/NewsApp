package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.databinding.ItemNotificationToggleBinding
import com.example.newsapp.model.NotificationPreference

class NotificationSettingsAdapter(
    private val items: List<NotificationPreference>,
    private val onToggleChanged: (NotificationPreference) -> Unit
) : RecyclerView.Adapter<NotificationSettingsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationToggleBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNotificationToggleBinding.inflate(inflater, parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            categoryName.text = item.category
            notificationSwitch.isChecked = item.isEnabled

            // Toggle switch when clicked directly
            notificationSwitch.setOnCheckedChangeListener(null) // prevent triggering twice
            notificationSwitch.isChecked = item.isEnabled

            // Entire row click toggles the switch
            notificationRowContainer.setOnClickListener {
                notificationSwitch.isChecked = !notificationSwitch.isChecked
                notificationRowContainer.animate()
                    .alpha(0.5f)
                    .setDuration(150)
                    .withEndAction {
                        notificationRowContainer.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start()
                    }.start()

            }

            // Real handler: update model when switch changes
            notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
                item.isEnabled = isChecked
                onToggleChanged(item)
                notificationRowContainer.animate()
                    .alpha(0.5f)
                    .setDuration(150)
                    .withEndAction {
                        notificationRowContainer.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start()
                    }.start()

            }
        }
    }


    override fun getItemCount(): Int = items.size
}
