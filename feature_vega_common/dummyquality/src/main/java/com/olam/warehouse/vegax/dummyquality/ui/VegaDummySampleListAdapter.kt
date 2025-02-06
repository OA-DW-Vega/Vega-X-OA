
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.data.domain.model.ResponseDummySampleList

class VegaDummySampleListAdapter(private val dummySampleList: List<ResponseDummySampleList>) : RecyclerView.Adapter<VegaDummySampleListAdapter.ViewHolder>() {
 var onItemClick :((ResponseDummySampleList,option:Int,position:Int,view :View) -> Unit)? =    null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dummy_sample_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = dummySampleList[position]
        holder.tvMaterialName.text = (model.materialDetail?.materialName)
        holder.tvVendorName.text = (model.vendorDetails?.vendorName)
        holder.tvTransactionNumber.text = (model.transactionNumber)
        holder.tvCountry.text = (model.plant?.countryDetail?.countryName)
        holder.tvDate.text = model.createdAt?.let { DateUtils.getDate(it.toLong(), "yyyy-MM-dd") }
        holder.cardView.setOnClickListener{
            onItemClick?.invoke(model,1,position,holder.cardView)
        }

        holder.imgClose.setOnClickListener{
            onItemClick?.invoke(model,2,position,holder.imgClose)
        }

    }

    override fun getItemCount(): Int {
        return dummySampleList.size
    }
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvMaterialName: TextView = itemView.findViewById(R.id.tvMaterialName)
        val tvVendorName: TextView = itemView.findViewById(R.id.tvVendorName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCountry: TextView = itemView.findViewById(R.id.tvWeight)
        val tvTransactionNumber: TextView = itemView.findViewById(R.id.tvSupplierName)
        val cardView: LinearLayout = itemView.findViewById(R.id.cardView)
        val imgClose: AppCompatImageView = itemView.findViewById(R.id.ivScaleClose)
    }



}
