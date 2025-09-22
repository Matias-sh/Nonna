package com.cocido.nonna.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cocido.nonna.databinding.ItemVaultCardBinding
import com.cocido.nonna.domain.model.Vault

class VaultAdapter(
    private val onVaultClick: (Vault) -> Unit,
    private val onVaultSettingsClick: (Vault) -> Unit
) : ListAdapter<Vault, VaultAdapter.VaultViewHolder>(VaultDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultViewHolder {
        val binding = ItemVaultCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VaultViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: VaultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class VaultViewHolder(
        private val binding: ItemVaultCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(vault: Vault) {
            binding.apply {
                tvVaultName.text = vault.name
                tvVaultMembers.text = "${vault.memberUids.size} miembros"
                tvVaultCreated.text = "Creado: ${formatDate(vault.createdAt)}"
                
                // TODO: Agregar imagen del baúl si está disponible
                ivVaultIcon.setImageResource(com.cocido.nonna.R.drawable.ic_launcher_foreground)
                
                root.setOnClickListener {
                    onVaultClick(vault)
                }
                
                btnVaultSettings.setOnClickListener {
                    onVaultSettingsClick(vault)
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val date = java.util.Date(timestamp)
            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return formatter.format(date)
        }
    }
    
    class VaultDiffCallback : DiffUtil.ItemCallback<Vault>() {
        override fun areItemsTheSame(oldItem: Vault, newItem: Vault): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Vault, newItem: Vault): Boolean {
            return oldItem == newItem
        }
    }
}
