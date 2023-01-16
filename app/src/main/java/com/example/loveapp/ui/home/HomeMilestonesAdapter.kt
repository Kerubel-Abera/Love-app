package com.example.loveapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.loveapp.data.Milestone
import com.example.loveapp.databinding.HomeMilestonesItemBinding

class HomeMilestonesAdapter(private var milestones: List<Milestone>) :
    RecyclerView.Adapter<HomeMilestonesAdapter.ViewHolder>() {

    fun submitList(milestones: List<Milestone>) {
        this.milestones = milestones
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: HomeMilestonesItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            HomeMilestonesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val milestone = milestones[position]
        holder.binding.textviewMilestoneTitle.text = milestone.status
        holder.binding.textviewMilestoneDate.text = milestone.date
    }

    override fun getItemCount(): Int {
        return milestones.size
    }
}