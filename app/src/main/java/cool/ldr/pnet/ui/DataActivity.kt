package cool.ldr.pnet.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import autodispose2.AutoDispose
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter4.BaseQuickAdapter
import cool.ldr.pnet.api.data
import cool.ldr.pnet.api.data.getList
import cool.ldr.pnet.databinding.ActivityDataBinding
import cool.ldr.pnet.entity.Kinetics
import cool.ldr.pnet.entity.Krlh
import cool.ldr.pnet.entity.Reference
import cool.ldr.pnet.utils.Constant
import cool.ldr.pnet.utils.WebUtils
import cool.ldr.pnet.views.adapter.KineticsAdapter
import cool.ldr.pnet.views.dialog.BaseDialog
import cool.ldr.pnet.views.dialog.MessageDialog
import cool.ldr.pnet.views.tastytoast.SimToast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class DataActivity : AppCompatActivity() {
    private var binding: ActivityDataBinding? = null


    private var adapter: KineticsAdapter? = null
    private var typeId = Constant.loveTypeId;
    private var kineticsList: MutableList<Kinetics> = mutableListOf()
    private var referenceList: MutableList<Reference> = mutableListOf()

    private var loveIds: MutableList<String> = mutableListOf()
    private var historyIds: MutableList<String> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        typeId = intent.getIntExtra("typeId", Constant.loveTypeId)

        if (typeId == Constant.historyTypeId) {
            LogUtils.d("历史")
            supportActionBar?.title = "历史"
        }


        binding!!.rvData.setLayoutManager(LinearLayoutManager(this))
        adapter = KineticsAdapter()
        adapter!!.setOnItemClickListener { _: BaseQuickAdapter<Kinetics, *>, _: View, p: Int ->
            showDetail(p)
        }
        binding!!.rvData.setAdapter(adapter)

        initData()


    }

    private fun initListen() {

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

                    loadData()

                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                }

                override fun onComplete() {}
            })
    }

    private fun loadData() {

        Observable.create { emitter: ObservableEmitter<MutableList<Kinetics>> ->
            val ids: MutableList<String> = if (typeId == Constant.loveTypeId) {
                getList(
                    Constant.loveFileName,
                    String::class.java
                )
            } else {
                getList(
                    Constant.historyFileName,
                    String::class.java
                )
            }

            val ks = mutableListOf<Kinetics>()

            for (id in ids) {
                for (kinetics in kineticsList) {
                    if (id == kinetics.id) {
                        ks.add(kinetics)
                        break
                    }
                }
            }

            emitter.onNext(ks)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@DataActivity)))
            .subscribe(object : Observer<MutableList<Kinetics>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(ks: MutableList<Kinetics>) {
                    adapter!!.submitList(ks.reversed())
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


        MessageDialog.Builder(this@DataActivity)
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
                    WebUtils.openWeb(this@DataActivity, doi)
                }

                override fun onCancel(dialog: BaseDialog) {
                    saveLH(id, typeId, p)
                }
            }).show()

    }


    private fun saveLH(id: String, typeId: Int, p: Int) {
        Observable.create { emitter: ObservableEmitter<MutableList<String>> ->
            var file = Constant.loveFileName
            var ids = loveIds
            if (typeId == Constant.historyTypeId) {
                file = Constant.historyFileName
                ids = historyIds
            }

//                    删除
            if (ids.contains(id)) {
                ids.remove(id)
                data.saveList(file, ids)
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
                    adapter!!.removeAt(p)
                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                }

                override fun onComplete() {}
            })
    }

}