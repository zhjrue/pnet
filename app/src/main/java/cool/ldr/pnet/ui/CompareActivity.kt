package cool.ldr.pnet.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import autodispose2.AutoDispose
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter4.BaseQuickAdapter
import cool.ldr.pnet.R
import cool.ldr.pnet.api.data
import cool.ldr.pnet.databinding.ActivityCompareBinding
import cool.ldr.pnet.entity.Compare
import cool.ldr.pnet.entity.Kinetics
import cool.ldr.pnet.views.adapter.CompareAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Objects

class CompareActivity : AppCompatActivity() {
    private var binding: ActivityCompareBinding? = null
    private var adapter: CompareAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompareBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        val id0 = intent.getStringExtra("id0")
        val id1 = intent.getStringExtra("id1")


        binding!!.rvCompare.setLayoutManager(LinearLayoutManager(this))
        adapter = CompareAdapter()
        adapter!!.setOnItemClickListener { _: BaseQuickAdapter<Compare, *>, _: View, p: Int ->
            LogUtils.d(p)
        }
        binding!!.rvCompare.setAdapter(adapter)
        initData(id0!!, id1!!)

    }


    private fun initData(id0: String, id1: String) {

        LogUtils.d(id0, id1)

        Observable.create { emitter: ObservableEmitter<List<Compare>> ->
            val cs: MutableList<Compare> = mutableListOf()

            val ks: MutableList<Kinetics> = data.getKrlh().ks
            var k0: Kinetics? = null
            var k1: Kinetics? = null
            for (k in ks) {
                if (Objects.equals(k.id, id0)) {
                    k0 = k
                } else if (Objects.equals(k.id, id1)) {
                    k1 = k
                }
            }
            if (k0 != null && k1 != null) {
                cs.add(Compare(getString(R.string.material), k0.material, k1.material))
                cs.add(Compare(getString(R.string.substrate), k0.substrate, k1.substrate))
                cs.add(
                    Compare(
                        getString(R.string.material_type),
                        k0.material_type,
                        k1.material_type
                    )
                )
                cs.add(Compare(getString(R.string.enzyme_type), k0.enzyme_type, k1.enzyme_type))
                cs.add(
                    Compare(
                        getString(R.string.km),
                        "${k0.km} ${k0.km_unit}",
                        "${k1.km} ${k1.km_unit}"
                    )
                )
                cs.add(
                    Compare(
                        getString(R.string.vmax),
                        "${k0.vmax} ${k0.vmax_unit}",
                        "${k1.vmax} ${k1.vmax_unit}"
                    )
                )
                cs.add(
                    Compare(
                        getString(R.string.kcat),
                        "${k0.kcat} ${k0.kcat_unit}",
                        "${k0.kcat} ${k0.kcat_unit}"
                    )
                )
                cs.add(Compare(getString(R.string.pH), k0.pH, k1.pH))
                cs.add(Compare(getString(R.string.title), k0.title, k1.title))
                cs.add(Compare(getString(R.string.doi), k0.doi, k1.doi))
            }

            emitter.onNext(cs)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe(object : Observer<List<Compare>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(cs: List<Compare>) {
                    adapter!!.submitList(cs);
                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                }

                override fun onComplete() {}
            })
    }
}