package cool.ldr.pnet.views.adapter

import androidx.recyclerview.widget.DiffUtil
import cool.ldr.pnet.entity.Kinetics

class KineticsDiffCallback : DiffUtil.ItemCallback<Kinetics>() {
    override fun areItemsTheSame(oldItem: Kinetics, newItem: Kinetics): Boolean {
        // 判断是否是同一个 item（通常使用id字段判断）
        // 示例假设DiffEntity有一个getId()方法
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Kinetics, newItem: Kinetics): Boolean {
        // 如果是同一个item，则判断item内的数据内容是否有变化
        // 此处需要实现具体的比较逻辑，例如比较具体的字段
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Kinetics, newItem: Kinetics): Any? {
        // 可选实现，可以在此处返回具体的变化详情
        // 如果不需要使用payload来优化绑定，可以不实现此方法或返回null
        return null
    }
}
