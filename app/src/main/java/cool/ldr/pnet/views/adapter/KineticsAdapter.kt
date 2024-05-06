package cool.ldr.pnet.views.adapter

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import cool.ldr.pnet.R
import cool.ldr.pnet.entity.Kinetics

class KineticsAdapter : BaseDifferAdapter<Kinetics, QuickViewHolder>(KineticsDiffCallback()) {

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_kinetics, parent)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Kinetics?) {
        if (item != null) {
            holder.setText(R.id.tv_k_material, item.material)

            holder.setText(
                R.id.tv_k_enzyme_type,
                "${item.substrate} | ${item.enzyme_type} | ${item.material_type}"
            )
            holder.setText(R.id.tv_k_km, "${item.km} ${item.km_unit}")
            holder.setText(R.id.tv_k_kcat, "${item.kcat} ${item.kcat_unit}")
            //        holder.setText(R.id.tv_k_ph, kinetics.getPH());
            holder.setText(R.id.tv_k_vmax, "${item.vmax} ${item.vmax_unit}")
            holder.setVisible(R.id.iv_item_k_selected, item.selected);
        }
    }
}
