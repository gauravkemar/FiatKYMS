package com.example.fiatkyms.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fiatkyms.R
import com.example.fiatkyms.model.searchvehicle.VehicleListResponseItem

import java.text.SimpleDateFormat
import java.util.*

class SearchVehicleAdapter(val onItemClick: (VehicleListResponseItem) -> Unit, val onNavgationClick:
    (VehicleListResponseItem) -> Unit,) : RecyclerView.Adapter<SearchVehicleAdapter.ViewHolder>()  {

    private var vehicleList = mutableListOf<VehicleListResponseItem>()
    private var context: Context? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setVehicleList(
        vehicleList: List<VehicleListResponseItem>,
        context: Context,
    ) {
        this.vehicleList = vehicleList as MutableList<VehicleListResponseItem>
        this.context = context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_vehicle_rc_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var vehicleListMod: VehicleListResponseItem = vehicleList.get(position)
        holder.bind(vehicleList[position])
    }

    override fun getItemCount(): Int {
        if (vehicleList.size == 0) {
            // Toast.makeText(context, "List is empty", Toast.LENGTH_LONG).show()
        } else {
            return vehicleList.size
        }
        return vehicleList.size
    }
    inner class ViewHolder(private val ItemView: View) : RecyclerView.ViewHolder(ItemView){

        fun bind(vehicleListResponseItem: VehicleListResponseItem) {
            tvVin.text = vehicleListResponseItem.vehicleResponse?.vin


            if(vehicleListResponseItem.createdDate?.isNotEmpty() == true || vehicleListResponseItem.createdDate!=null )
            {
                val outputFormatStr = "yyyy-M-dd HH:mm"
                val formattedDate = formatDate(vehicleListResponseItem.createdDate, outputFormatStr)
                tvDate.text=formattedDate
            }



            tvYardName.text = vehicleListResponseItem.vehicleId.toString()
            vehicleListResponseItem.status?.let { getImageResource(it) }
                ?.let { ivStatus.setImageResource(it) }
            if(vehicleListResponseItem.status.equals("Park Out"))
            {
                ivNavigation.visibility=View.GONE
                tvNavivgate.setText("Parked Out")
            }
            else
            {
                ivNavigation.visibility=View.VISIBLE
                tvNavivgate.setText("Navigate")
                tvVin.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClick.invoke(vehicleList[position])
                    }
                }
                tvDate.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClick.invoke(vehicleList[position])
                    }
                }
                tvYardName.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClick.invoke(vehicleList[position])
                    }
                }
                ivNavigation.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onNavgationClick.invoke(vehicleList[position])
                    }
                }
            }

        }

        private fun getImageResource(status: String): Int {
            when (status) {
                "Reparking" -> {
                    return R.drawable.ic_reparked
                }

                "Park In" -> {
                    return R.drawable.ic_parked
                }

                else -> {
                    return R.drawable.ic_dropped
                }

            }


        }

        val tvVin: TextView = itemView.findViewById(R.id.tvVin)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvYardName: TextView = itemView.findViewById(R.id.tvYardName)
        val tvNavivgate: TextView = itemView.findViewById(R.id.tvNavivgate)
        val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)
        val ivNavigation: ImageView = itemView.findViewById(R.id.ivNavigation)

    }

    fun formatDate(inputDateStr: String, outputFormatStr: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat(outputFormatStr, Locale.getDefault())

        try {
            val date = inputFormat.parse(inputDateStr)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /*  fun main() {
          val inputDateStr = "2023-10-07T12:38:00.3459365"
          val outputFormatStr = "yyyy-M-dd HH:mm"

          val formattedDate = formatDate(inputDateStr, outputFormatStr)
          println(formattedDate)
      }*/
}