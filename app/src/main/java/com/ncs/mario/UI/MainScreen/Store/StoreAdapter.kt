import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ncs.mario.Domain.HelperClasses.PrefManager
import com.ncs.mario.Domain.Models.Merch
import com.ncs.mario.R
import com.ncs.mario.databinding.MerchItemBinding

class StoreAdapter(private val onRedeemClick: (Merch) -> Unit) :
    ListAdapter<Merch, StoreAdapter.StoreViewHolder>(MerchDiffUtil()) {

    class StoreViewHolder(val binding: MerchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Merch, onRedeemClick: (Merch) -> Unit) {
            with(binding) {
                Glide.with(root).load(item.image).error(R.drawable.placeholder_image).into(contentIcon)

                itemName.text = item.name
                points.text = item.cost.toString()

                if (PrefManager.getUserProfile()?.points ?: 0 >= item.cost) {
                    lockLayout.setBackgroundResource(R.drawable.filled_primary_box)
                    lockText.text = "Redeem"
                    lockImage.visibility = View.GONE

                    root.setOnClickListener {
                        onRedeemClick(item)
                    }
                } else {
                    root.setOnClickListener {
                        Toast.makeText(root.context, "You don't have enough points!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = MerchItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        if (position < itemCount) {
            holder.bind(getItem(position), onRedeemClick)
        }
    }

    class MerchDiffUtil : DiffUtil.ItemCallback<Merch>() {
        override fun areItemsTheSame(oldItem: Merch, newItem: Merch): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Merch, newItem: Merch): Boolean {
            return oldItem == newItem
        }
    }
}
