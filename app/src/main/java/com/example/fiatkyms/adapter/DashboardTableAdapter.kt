package com.example.fiatkyms.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fiatkyms.databinding.TableItemBinding
import com.example.fiatkyms.model.dashboard.DashboardDataResponseItem
import com.example.fiatkyms.model.dashboard.GetVehicleDashboardDataAdminResponse


class DashboardTableAdapter(val context: Context, val items: MutableList<DashboardDataResponseItem>?,
                            val itemsAdmin: MutableList<GetVehicleDashboardDataAdminResponse>?) :
    RecyclerView.Adapter<DashboardTableAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(private val itemViewBinding: TableItemBinding) : RecyclerView.ViewHolder(itemViewBinding.root) {

        fun bind(item: DashboardDataResponseItem?,itemsAdmin:GetVehicleDashboardDataAdminResponse?) {
            if(item!=null)
            {
                itemViewBinding.tvColumnOne.text = item.vin
                itemViewBinding.tvColumnTwo.text = item.status
                itemViewBinding.tvColumnThree.text = item.locationName
            }
            else if(itemsAdmin!=null)
            {
                itemViewBinding.tvColumnOne.text =itemsAdmin.vin
                itemViewBinding.tvColumnTwo.text = itemsAdmin.status
                itemViewBinding.tvColumnThree.text = itemsAdmin.attributes
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(TableItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        if(items!=null)
        {
            return items.size
        }
        else if(itemsAdmin!=null)
        {
            return itemsAdmin.size
        }
       return 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if(items!=null)
        {
            holder.bind(items[position],null)
        }
        else if(itemsAdmin!=null)
        {
            holder.bind(null,itemsAdmin[position])
        }

    }

    fun updateItems(list: MutableList<DashboardDataResponseItem>?,listAdmin: MutableList<GetVehicleDashboardDataAdminResponse>?) {
        if(items!=null)
        {
            items.clear()
            list?.let { items.addAll(it) }
        }
        else if(itemsAdmin!=null)
        {
            itemsAdmin?.clear()
            listAdmin?.let { itemsAdmin.addAll(it) }

        }
        notifyDataSetChanged()
    }

}
