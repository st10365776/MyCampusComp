package com.example.mycampuscomp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.databinding.ItemOnboardingBinding
import com.example.mycampuscomp.model.OnboardingItem

class OnboardingAdapter(
    private val onboardingItems: List<OnboardingItem>,
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    class OnboardingViewHolder(private val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            binding.image.setImageResource(item.image)
            binding.title.text = item.title
            binding.description.text = item.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int {
        return onboardingItems.size
    }
}
