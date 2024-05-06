package cool.ldr.pnet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import autodispose2.AutoDispose
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter4.BaseQuickAdapter
import cool.ldr.pnet.R
import cool.ldr.pnet.api.data
import cool.ldr.pnet.databinding.FragmentHomeBinding
import cool.ldr.pnet.entity.Kinetics
import cool.ldr.pnet.entity.Krlh
import cool.ldr.pnet.entity.Reference
import cool.ldr.pnet.utils.Constant
import cool.ldr.pnet.utils.Constant.historyTypeId
import cool.ldr.pnet.utils.WebUtils
import cool.ldr.pnet.views.adapter.KineticsAdapter
import cool.ldr.pnet.views.dialog.AddressDialog
import cool.ldr.pnet.views.dialog.BaseDialog
import cool.ldr.pnet.views.dialog.InputDialog
import cool.ldr.pnet.views.dialog.MessageDialog
import cool.ldr.pnet.views.tastytoast.SimToast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var mContext: Context? = null
    private var adapter: KineticsAdapter? = null
    private var kineticsList: MutableList<Kinetics> = mutableListOf()
    private var referenceList: MutableList<Reference> = mutableListOf()
    private var loveIds: MutableList<String> = mutableListOf()
    private var historyIds: MutableList<String> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding!!.getRoot()
        mContext = context
        binding!!.ivHomeQa.setOnClickListener { v: View? ->
            MessageDialog.Builder(mContext)
                .setTitle("说明")
                .setMessage(
                    "Km：反应底物结合能力，Km值越高表示酶与底物之间的亲和力越弱，Km值越低表示酶与底物之间的亲和力越强。" +
                            "\n\nVm：最大反应速率，指的是当酶的活性部位全部被底物饱和时，单位时间内酶催化反应的速率。" +
                            "\n\nKcat：在最有优条件下酶催化生成底物的速率，又叫转化数"
                )
                .setConfirm("确定")
                .setListener { }.show()
        }
        binding!!.rvMain.setLayoutManager(LinearLayoutManager(mContext))
        adapter = KineticsAdapter()
        adapter!!.setOnItemClickListener { _: BaseQuickAdapter<Kinetics, *>, _: View, p: Int ->
            showDetail(p)
        }
        adapter!!.setOnItemLongClickListener { _: BaseQuickAdapter<Kinetics, *>?, _: View?, p: Int ->
            compare(p)
            true
        }
        binding!!.rvMain.setAdapter(adapter)


        binding!!.ivHomeSearch.setOnClickListener { search() }
        binding!!.srlHome.setOnRefreshListener { select() }

        initData()
        return root
    }

    private fun initData() {

        Observable.create { emitter: ObservableEmitter<Krlh> ->
            emitter.onNext(data.getKrlh())
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe(object : Observer<Krlh> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(kr: Krlh) {
                    kineticsList.clear()
                    kineticsList.addAll(kr.ks)

                    referenceList.clear()
                    referenceList.addAll(kr.rs)

                    loveIds.clear()
                    loveIds.addAll(kr.loveIds)

                    historyIds.clear()
                    historyIds.addAll(kr.historyIds)

                    adapter!!.submitList(kineticsList)
                    binding!!.tvHomeSubtitle.text = "所有(${kineticsList.size})"
                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                }

                override fun onComplete() {}
            })

    }

    private fun saveLH(id: String, typeId: Int, rm: Boolean = false) {
        Observable.create { emitter: ObservableEmitter<MutableList<String>> ->
            var file = Constant.loveFileName
            var ids = loveIds
            if (typeId == historyTypeId) {
                file = Constant.historyFileName
                ids = historyIds
            }

            if (!rm) {
//                    添加
                if (!ids.contains(id)) {
                    LogUtils.d(ids)
                    ids.add(id)
                    LogUtils.d(ids)
                    if (ids.size > 100) {
                        ids.removeAt(0)
                        LogUtils.d(ids)
                    }
                    data.saveList(file, ids)
                }
            } else {
//                    删除
                if (ids.contains(id)) {
                    ids.remove(id)
                    data.saveList(file, ids)
                }
            }

            emitter.onNext(ids)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe(object : Observer<MutableList<String>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(ks: MutableList<String>) {
                    SimToast.toastSL("成功")
                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                }

                override fun onComplete() {}
            })
    }

    private fun showDetail(p: Int) {
        val (_, id, _, _, _, doi, title) = adapter!!.getItem(p) ?: return

        var cs = "收藏"
        if (loveIds.contains(id)) {
            cs = "取消收藏"
        }

        MessageDialog.Builder(mContext)
            .setTitle("数据来源")
            .setMessage(
                """
                $title

                $doi
                """.trimIndent()
            )
            .setConfirm("查看论文")
            .setCancel(cs)
            .setListener(object : MessageDialog.OnListener {
                override fun onConfirm(dialog: BaseDialog) {
                    WebUtils.openWeb(mContext, doi)
                    saveLH(id, historyTypeId)
                }

                override fun onCancel(dialog: BaseDialog) {
                    saveLH(id, Constant.loveTypeId, loveIds.contains(id))
                }
            }).show()

    }

    private fun compare(p: Int) {
        val (_, id) = adapter!!.getItem(p) ?: return

        val kineticsList1: MutableList<Kinetics> = ArrayList()
        for (kinetics in kineticsList) {
            if (id == kinetics.id) {
                kinetics.selected = !kinetics.selected

                adapter!![p] = kinetics
                adapter!!.notifyItemChanged(p)
            }

            if (kinetics.selected) {
                kineticsList1.add(
                    GsonUtils.fromJson(
                        GsonUtils.toJson(kinetics),
                        Kinetics::class.java
                    )
                )
            }
        }
        if (kineticsList1.size == 2) {
            SimToast.toastSL("选中两个自动对比")

            val intent = Intent(context, CompareActivity::class.java)
            intent.putExtra("id0", kineticsList1[0].id)
            intent.putExtra("id1", kineticsList1[1].id)
            startActivity(intent)

            for (i in kineticsList.indices) {
                val kinetics = kineticsList[i]
                if (kinetics.selected) {
                    kinetics.selected = false
                    adapter!!.notifyItemChanged(i)
                }
            }
        }
    }

    private fun search() {

        // 标题可以不用填写
        InputDialog.Builder(mContext).setTitle("搜索")
            .setHint("请输出入纳米酶名字")
            .setConfirm("确定")
            .setCancel("取消")
            .setListener { _: BaseDialog?, s_: String ->
                var s = s_
                val kineticsList1: MutableList<Kinetics> = ArrayList()
                s = s.lowercase(Locale.getDefault())
                for (kinetics in kineticsList) {
                    val s2 = kinetics.material.lowercase(Locale.getDefault())
                        .replace("[^a-zA-Z]".toRegex(), "")
                    if (s2.contains(s)) {
                        kineticsList1.add(kinetics)
                    }
                }
                adapter!!.submitList(kineticsList1)
                binding!!.tvHomeSubtitle.text = "搜索${s}(${kineticsList1.size})"

            }
            .show()
    }

    private fun select() {
        binding!!.srlHome.finishRefresh(1)
        AddressDialog.Builder(mContext)
            .setTitle(getString(R.string.address_title))
            .setListener(object : AddressDialog.OnListener {
                override fun onSelected(
                    dialog: BaseDialog,
                    province: String,
                    city: String,
                    area: String
                ) {
                    val ks: MutableList<Kinetics> = data.filterData(province, area, kineticsList)
                    adapter!!.submitList(ks)

                    var s = area;

                    if (area.contains("（")) {
                        s = area.split("（".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0]
                    }

                    binding!!.tvHomeSubtitle.text = "${s}(${ks.size})"

                }

                override fun onCancel(dialog: BaseDialog) {
                    SimToast.toastSL("取消了")
                }
            })
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}