package cool.ldr.pnet.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import autodispose2.AutoDispose.autoDisposable
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import com.blankj.utilcode.util.LogUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import cool.ldr.pnet.R
import cool.ldr.pnet.api.data
import cool.ldr.pnet.databinding.FragmentDashboardBinding
import cool.ldr.pnet.entity.Kinetics
import cool.ldr.pnet.entity.Krlh
import cool.ldr.pnet.views.dialog.AddressDialog
import cool.ldr.pnet.views.dialog.BaseDialog
import cool.ldr.pnet.views.dialog.MessageDialog
import cool.ldr.pnet.views.tastytoast.SimToast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class DashboardFragment : Fragment() {
    private var binding: FragmentDashboardBinding? = null
    private val kineticsList = mutableListOf<Kinetics>()
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        mContext = context;

        initChart(binding!!.acvVm, "Vm")
        initChart(binding!!.acvKm, "Km")


        binding!!.ivDashboardQa.setOnClickListener { v ->
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

        binding!!.srlDashboard.setOnRefreshListener { select() }

        initData()


        return binding!!.getRoot()

    }


    private fun initData() {

        Observable.create { emitter: ObservableEmitter<Krlh> ->
            emitter.onNext(data.getKrlh())
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe(object : Observer<Krlh> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(kr: Krlh) {
                    kineticsList.clear()
                    kineticsList.addAll(kr.ks)
                    plt(kineticsList)

                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                }

                override fun onComplete() {}
            })

    }

    private fun initChart(lc: LineChart, title: String) {

        lc.description.text = "加载中……"
        lc.setNoDataText("加载中……")

        val description = Description()
        description.text = title
        lc.description = description
        val yl: YAxis = lc.axisLeft
        yl.enableGridDashedLine(10f, 10f, 0f)

        val xAxis: XAxis = lc.xAxis
        xAxis.setGranularity(1f)
        xAxis.position = XAxis.XAxisPosition.BOTTOM //x轴是在上边显示还是显示在下边
        xAxis.setDrawGridLines(false)

        val yr: YAxis = lc.axisRight
        yr.setDrawGridLines(false)
        yr.isEnabled = false
    }

    fun plt(ks: MutableList<Kinetics>) {
        pltKVm(ks, false)
        pltKVm(ks, true)
    }

    private fun pltKVm(ks: MutableList<Kinetics>, km: Boolean = false) {

        Observable.create { emitter: ObservableEmitter<LineData> ->
            emitter.onNext(setPlotData(ks, km))
            emitter.onComplete()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe(object : Observer<LineData> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(lineData: LineData) {

                    if (km) {
                        binding!!.acvKm.setData(lineData)
                        binding!!.acvKm.invalidate()
                    } else {
                        binding!!.acvVm.setData(setPlotData(ks, false))
                        binding!!.acvVm.invalidate()
                    }
                }

                override fun onError(e: Throwable) {
                    LogUtils.i(e.toString())
                    SimToast.toastEe("绘制错误！")
                }

                override fun onComplete() {}
            })
    }

    private fun setPlotData(ks: MutableList<Kinetics>, km: Boolean = false): LineData {

        val lineData = LineData()
        val entries: MutableList<Entry> = mutableListOf();
        val y2s: MutableList<String> = mutableListOf();
        val xs: MutableList<Float> = mutableListOf();
        val ys: MutableList<Float> = mutableListOf();

        val ks2 = ks.sortedBy {
            if (km) {
                it.km
            } else {
                it.vmax
            }
        }
        var n = 0
        for (k in ks2) {
            val y = if (km) {
                k.km
            } else {
                k.vmax
            }
            val x = n.toFloat()
            entries.add(Entry(x, y))
            xs.add(x)
            ys.add(y)
            y2s.add("${k.material}(${k.y})")
            n += 1
        }
        LogUtils.d(n, entries.size, y2s.size)

        val lineDataSet = LineDataSet(
            entries, if (km) {
                "米氏常数"
            } else {
                "最大速率"
            }
        )

        lineDataSet.setColor(ContextCompat.getColor(mContext!!, R.color.tertiary))
        lineDataSet.valueTextSize = 10f
        lineDataSet.setLineWidth(1.5f)
        lineDataSet.setValueTextColor(ContextCompat.getColor(mContext!!, R.color.tertiary))
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setCircleColor(ContextCompat.getColor(mContext!!, R.color.tertiary))
        lineDataSet.setValueTextColor(ContextCompat.getColor(mContext!!, R.color.secondary))
        lineDataSet.setColor(ContextCompat.getColor(mContext!!, R.color.tertiary))

        lineDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (ys.contains(value)) {
                    y2s[ys.indexOf(value)]
                } else {
                    super.getFormattedValue(value)
                }
            }
        }

        lineData.addDataSet(lineDataSet)

        return lineData;
    }

    private fun select() {
        binding!!.srlDashboard.finishRefresh(1)

        // 选择地区对话框
        AddressDialog.Builder(context)
            .setTitle(getString(R.string.address_title))
            .setListener(object : AddressDialog.OnListener {
                override fun onSelected(
                    dialog: BaseDialog,
                    province: String,
                    city: String,
                    area: String
                ) {

                    val ks: MutableList<Kinetics> = data.filterData(province, area, kineticsList)

                    var s = area;

                    if (area.contains("（")) {
                        s = area.split("（".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0]
                    }

                    binding!!.tvDashboardSubtitle.text = "${s}(${ks.size})"
                    plt(ks)

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