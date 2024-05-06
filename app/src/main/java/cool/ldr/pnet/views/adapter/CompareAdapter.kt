package cool.ldr.pnet.views.adapter

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import cool.ldr.pnet.R
import cool.ldr.pnet.entity.Compare

class CompareAdapter : BaseQuickAdapter<Compare, QuickViewHolder>() {

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_compare, parent)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Compare?) {
        if (item != null) {
            holder.setText(R.id.tv_c0, item.c0)
            holder.setText(R.id.tv_c1, item.c1)
            holder.setText(R.id.tv_c_title, item.title)
        }
    }
}
