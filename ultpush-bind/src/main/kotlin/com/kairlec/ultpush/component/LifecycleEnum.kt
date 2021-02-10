package com.kairlec.ultpush.component

enum class LifecycleEnum(val order: Int, val stage: String, val autoRun: Boolean) {
    LOAD(0, "Load", true),
    INIT(1, "Init", true),
    RUN(2, "Run", true),
    DESTROY(3, "Destroy", false)
}