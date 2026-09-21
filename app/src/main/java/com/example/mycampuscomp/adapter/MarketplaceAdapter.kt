package com.example.mycampuscomp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mycampuscomp.R
import com.example.mycampuscomp.model.MarketplaceItem
import com.google.android.material.card.MaterialCardView

class MarketplaceAdapter(
    private var items: MutableList<MarketplaceItem>,
    private val onItemClick:
        (MarketplaceItem) -> Unit
) : RecyclerView.Adapter<MarketplaceAdapter.ViewHolder>() {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val card: MaterialCardView =
            view.findViewById(R.id.cardMarketplaceItem)

        val image: ImageView =
            view.findViewById(R.id.ivItemImage)

        val title: TextView =
            view.findViewById(R.id.tvItemTitle)

        val price: TextView =
            view.findViewById(R.id.tvItemPrice)

        val condition: TextView =
            view.findViewById(R.id.tvItemCondition)

        val location: TextView =
            view.findViewById(R.id.tvItemLocation)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_marketplace,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item =
            items[position]

        holder.title.text =
            item.title

        holder.price.text =
            "R%.2f".format(item.price)

        holder.condition.text =
            item.condition

        holder.location.text =
            item.location

        holder.card.setOnClickListener {
            onItemClick(item)
        }

        if (item.imageUrl.isNotEmpty()) {
            holder.image.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.logo2)
                error(R.drawable.logo2)
            }
        } else {
            holder.image.setImageResource(
                R.drawable.logo2
            )
        }
    }

    override fun getItemCount() =
        items.size

    fun updateItems(
        newItems: List<MarketplaceItem>
    ) {

        items =
            newItems.toMutableList()

        notifyDataSetChanged()
    }
}