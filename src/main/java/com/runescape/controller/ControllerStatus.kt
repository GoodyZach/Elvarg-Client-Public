package com.runescape.controller

enum class ControllerStatus {
    CONNECTED,
    DISCONNECTED,
    NO_CONTROLLERS,
    NOT_CONNECTED,
    DISABLED;

    companion object {
        fun ControllerStatus.isConnected() = this == CONNECTED
    }

}
