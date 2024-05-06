package cool.ldr.pnet.entity


data class Kinetics(
    var selected: Boolean = false,
    val id: String,
    val ref: String,
    val material: String,
    var material_type: String,
    var doi: String,
    var title: String,
    val enzyme_type: String,
    val substrate: String,
    val pH: String,
    val T: String,
    val km: Float,
    val km_err: String,
    val km_10_n: String,
    val km_unit: String,
    val vmax: Float,
    val vmax_err: String,
    val vmax_10_n: String,
    val vmax_unit: String,
    val kcat: String,
    val kcat_err: String,
    val kcat_10_n: String,
    val kcat_unit: String,
    val kcat_km: String,
    val kcat_km_10_n: String,
    val kcat_km_unit: String,
    val comment: String,
    var y: Float
)
