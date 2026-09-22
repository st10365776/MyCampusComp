package com.example.mycampuscomp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.R
import com.example.mycampuscomp.model.ApsModule
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ApsModuleAdapter(
    private val modules: MutableList<ApsModule>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<ApsModuleAdapter.ModuleViewHolder>() {

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tilName: TextInputLayout = itemView.findViewById(R.id.tilModuleName)
        val etName: TextInputEditText = itemView.findViewById(R.id.etModuleName)
        val tilMark: TextInputLayout = itemView.findViewById(R.id.tilModuleMark)
        val etMark: TextInputEditText = itemView.findViewById(R.id.etModuleMark)
        val tilCredits: TextInputLayout = itemView.findViewById(R.id.tilModuleCredits)
        val etCredits: TextInputEditText = itemView.findViewById(R.id.etModuleCredits)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveModule)
        
        var nameWatcher: TextWatcher? = null
        var markWatcher: TextWatcher? = null
        var creditsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aps_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]

        // Remove old watchers before setting text to avoid infinite listener loops
        holder.nameWatcher?.let { holder.etName.removeTextChangedListener(it) }
        holder.markWatcher?.let { holder.etMark.removeTextChangedListener(it) }
        holder.creditsWatcher?.let { holder.etCredits.removeTextChangedListener(it) }

        holder.etName.setText(module.name)
        holder.etMark.setText(module.markText)
        holder.etCredits.setText(module.creditsText)

        holder.tilName.error = null
        holder.tilMark.error = null
        holder.tilCredits.error = null

        holder.nameWatcher = object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                module.name = s?.toString() ?: ""
                holder.tilName.error = null
            }
        }
        holder.markWatcher = object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                module.markText = s?.toString() ?: ""
                holder.tilMark.error = null
            }
        }
        holder.creditsWatcher = object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                module.creditsText = s?.toString() ?: ""
                holder.tilCredits.error = null
            }
        }

        holder.etName.addTextChangedListener(holder.nameWatcher)
        holder.etMark.addTextChangedListener(holder.markWatcher)
        holder.etCredits.addTextChangedListener(holder.creditsWatcher)

        holder.btnRemove.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onRemove(currentPos)
            }
        }
    }

    override fun getItemCount(): Int = modules.size

    abstract class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }
}
