package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.databinding.ItemRecycleBinding
import com.example.client_training_app.model.Client

class ClientAdapter(
    private var clients: List<Client>,
    private val onClientClicked: (Client) -> Unit // Lambda pro akci po kliknutí
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    // Vnitřní třída ViewHolder
    inner class ClientViewHolder(private val binding: ItemRecycleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(client: Client) {
            val fullName = "${client.firstName} ${client.lastName}"
            binding.tvName.text = fullName

            // Nastavení kliknutí na celou položku
            binding.root.setOnClickListener {
                onClientClicked(client)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemRecycleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(clients[position])
    }

    override fun getItemCount(): Int = clients.size

    // Metoda pro aktualizaci seznamu dat
    fun updateClients(newClients: List<Client>) {
        clients = newClients
        // Poznámka: Pro lepší optimalizaci použij DiffUtil, ale pro začátek stačí notifyDataSetChanged()
        notifyDataSetChanged()
    }
}