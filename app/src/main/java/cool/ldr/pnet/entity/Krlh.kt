package cool.ldr.pnet.entity


class Krlh(
    val ks: MutableList<Kinetics> = mutableListOf(),
    val rs: MutableList<Reference> = mutableListOf(),
    val loveIds: MutableList<String> = mutableListOf(),
    val historyIds: MutableList<String> = mutableListOf()
)
