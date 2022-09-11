package com.runescape.controller

import com.runescape.Client
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Robot
import java.awt.event.InputEvent
import kotlin.math.roundToInt


class ControllerTest : ControllerManager() {


    var robot = Robot()

    override fun onStart() {


    }

    override fun onStop() {
        println("STROPPED")
    }

    override fun onBackButtonPressed(button: ControllerButton?) {
        button?.let {
            when(it) {
                ControllerButton.RIGHTBUMPER -> Client.setTab(tabID(Client.tabId + 1))
                ControllerButton.LEFTBUMPER -> Client.setTab(tabID(Client.tabId - 1))
                ControllerButton.A -> {
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                }
            }
        }
    }

    override fun onButtonPressed(button: ControllerButton?) {
        button?.let {
            when(it) {
                ControllerButton.A -> {
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                }
            }
        }
    }

    override fun backTriggers(axis: ControllerAxis, value : Double) {
        when(axis) {
            ControllerAxis.TRIGGERRIGHT ->  setZoom(currController!!.getAxisState(ControllerAxis.TRIGGERRIGHT),true)
            ControllerAxis.TRIGGERLEFT -> setZoom(currController!!.getAxisState(ControllerAxis.TRIGGERLEFT),false)
        }
    }

    override fun rightStick(dir: JoystickDirection, value : Double) {
        val a = MouseInfo.getPointerInfo()
        val b: Point = a.location
        when(dir) {
            JoystickDirection.RIGHT -> robot.mouseMove(b.getX().roundToInt() + 5,b.getY().roundToInt())
            JoystickDirection.LEFT -> robot.mouseMove(b.getX().roundToInt() -5,b.getY().roundToInt() + 0)
            JoystickDirection.DOWN -> robot.mouseMove(b.getX().roundToInt() + 0,b.getY().roundToInt() + 5)
            JoystickDirection.UP -> robot.mouseMove(b.getX().roundToInt() + 0 ,b.getY().roundToInt() - 5)
        }
    }

    override fun leftStick(dir: JoystickDirection, value : Double) {
        when(dir) {
            JoystickDirection.RIGHT -> Client.instance.anInt1186 += (40 - Client.instance.anInt1186) / 2
            JoystickDirection.LEFT -> Client.instance.anInt1186 += (-40 - Client.instance.anInt1186) / 2
            JoystickDirection.DOWN -> Client.instance.anInt1187 += (-40 - Client.instance.anInt1187) / 2
            JoystickDirection.UP -> Client.instance. anInt1187 += (40 - Client.instance.anInt1187) / 2
        }
    }

    fun tabID(value : Int) : Int = value.coerceAtLeast(0).coerceAtMost(13)

    fun setZoom(value: Float, right: Boolean) {
        var percentage = (((value.toDouble() / 1) * 100) / 7).roundToInt()

        val zoom_in = if (!Client.instance.isResized) 195 else 240
        val zoom_out = if (!Client.instance.isResized) 1105 else 1220

        if (Client.openInterfaceId == -1) {
            if (right) {
                if (Client.cameraZoom > zoom_in) {
                    Client.cameraZoom -= percentage
                }
            } else {
                if (Client.cameraZoom < zoom_out) {
                    Client.cameraZoom += percentage
                }
            }
        }
    }

}

fun main() {
    ControllerTest().init()
}