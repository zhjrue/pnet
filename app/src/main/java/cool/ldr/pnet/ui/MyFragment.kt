package cool.ldr.pnet.ui

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ClickUtils
import cool.ldr.pnet.databinding.FragmentMyBinding
import cool.ldr.pnet.utils.Constant
import cool.ldr.pnet.utils.WebUtils
import cool.ldr.pnet.views.dialog.MessageDialog
import cool.ldr.pnet.views.tastytoast.SimToast
import java.util.Calendar

class MyFragment : Fragment() {
    private var binding: FragmentMyBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyBinding.inflate(inflater, container, false)
        val root: View = binding!!.getRoot()
        binding!!.sbMyLove.setOnClickListener {
            val intent = Intent(context, DataActivity::class.java)
            intent.putExtra("typeId", Constant.loveTypeId)
            startActivity(intent)
        }
        binding!!.sbMyHistory.setOnClickListener {
            val intent = Intent(context, DataActivity::class.java)
            intent.putExtra("typeId", Constant.historyTypeId)
            startActivity(intent)
        }

        initListen()
        return root
    }


    private fun initListen() {
        val name = "zhjrue"

        binding!!.tvAboutYsxy.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding!!.tvAboutFwtk.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding!!.tvAboutUs.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding!!.tvAboutBa.paint.flags = Paint.UNDERLINE_TEXT_FLAG


        val url = "https://github.com/${name}/pnet"

        binding!!.sbMyCode.setOnClickListener {
            WebUtils.openWeb(context, url)

        }
        binding!!.sbMyReportBug.setOnClickListener {
            WebUtils.openWeb(context, url)

        }

        binding!!.sbMyShare.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            // 比如发送文本形式的数据内容 // 指定发送的内容
            sendIntent.putExtra(Intent.EXTRA_TEXT, url) // 指定发送内容的类型
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "把我分享给谁呢..."))
        }


        binding!!.tvAboutYsxy.setOnClickListener {

            MessageDialog.Builder(context)
                .setTitle("隐私协议")
                .setMessage("待定")
                .setConfirm("确定")
                .setListener {}.show()
        }

        binding!!.tvAboutFwtk.setOnClickListener {

            MessageDialog.Builder(context)
                .setTitle("服务条款")
                .setMessage("待定")
                .setConfirm("确定")
                .setListener { }.show()
        }

        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]

        binding!!.tvAboutUs.text = "Copyright © 2024-$year ${name}"

        binding!!.aboutV.text = "${AppUtils.getAppVersionName()}"

        binding!!.tvAboutUs.setOnClickListener(object : ClickUtils.OnMultiClickListener(6) {
            override fun onTriggerClick(v: View) {
                SimToast.toastSs("张瑾")
            }

            override fun onBeforeTriggerClick(v: View, count: Int) {}
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}