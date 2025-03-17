package com.example.blablacat2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageSlide: ImageView = view.findViewById(R.id.imageSlide)
        private val textTitle: TextView = view.findViewById(R.id.textTitle)
        private val textDescription: TextView = view.findViewById(R.id.textDescription)

        fun bind(item: OnboardingItem) {
            imageSlide.setImageResource(item.imageResId)
            textTitle.text = item.title
            textDescription.text = item.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
