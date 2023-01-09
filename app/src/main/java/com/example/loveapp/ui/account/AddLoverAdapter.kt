package com.example.loveapp.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.loveapp.data.Request
import com.example.loveapp.databinding.AddLoverItemBinding

class AddLoverAdapter(private var requests: List<Request>) : RecyclerView.Adapter<AddLoverAdapter.ViewHolder>() {

    fun submitList(requests: List<Request>) {
        this.requests = requests
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: AddLoverItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AddLoverItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.binding.textviewName.text = request.name
    }

    override fun getItemCount(): Int {
        return requests.size
    }
}