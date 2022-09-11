package com.runescape.controller

import com.runescape.Client
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton
import com.studiohartman.jamepad.ControllerIndex
import com.studiohartman.jamepad.ControllerManager
import java.util.concurrent.Executors


open class ControllerManager {

    private val currentControllerIndex = 0
    private var currentState = ControllerStatus.NOT_CONNECTED
    private var controllers : ControllerManager =  ControllerManager()
    var currController : ControllerIndex? = null

    fun init() {
        val executor = Executors.newSingleThreadExecutor()


        controllers.let {

            controllers.initSDLGamepad()

            println("Controllers Connected: ${controllers.numControllers}")

            currentState = when(controllers.numControllers) {
                0 -> ControllerStatus.NO_CONTROLLERS
                else -> ControllerStatus.CONNECTED
            }

            //controllers.doVibration(currentControllerIndex, 0.2F,0.2F,200)


            onStart()

        }

    }

    fun onGameLoop() {
        controllers.update()
        currController = controllers.getControllerIndex(currentControllerIndex)

        if (!currController!!.isConnected) {
            onStop()
            controllers.quitSDLGamepad()
        }

        onButtonPressed( when {
            buttonPressed(ControllerButton.A) -> ControllerButton.A
            buttonPressed(ControllerButton.B) -> ControllerButton.B
            buttonPressed(ControllerButton.Y) -> ControllerButton.Y
            buttonPressed(ControllerButton.X) -> ControllerButton.X
            else -> null
        })

        onButtonHeld( when {
            buttonPressed(ControllerButton.A) -> ControllerButton.A
            buttonPressed(ControllerButton.B) -> ControllerButton.B
            buttonPressed(ControllerButton.Y) -> ControllerButton.Y
            buttonPressed(ControllerButton.X) -> ControllerButton.X
            else -> null
        })

        onBackButtonPressed( when {
            buttonPressed(ControllerButton.LEFTBUMPER) -> ControllerButton.LEFTBUMPER
            buttonPressed(ControllerButton.RIGHTBUMPER) -> ControllerButton.RIGHTBUMPER
            else -> null
        })

        if (currController!!.getAxisState(ControllerAxis.TRIGGERLEFT).toDouble() != 0.0) {
            val numb = currController!!.getAxisState(ControllerAxis.TRIGGERLEFT).toDouble()
            backTriggers(ControllerAxis.TRIGGERLEFT,numb)
        }

        if (currController!!.getAxisState(ControllerAxis.TRIGGERRIGHT).toDouble() != 0.0) {
            val numb = currController!!.getAxisState(ControllerAxis.TRIGGERRIGHT).toDouble()
            backTriggers(ControllerAxis.TRIGGERRIGHT,numb)
        }

        if (currController!!.getAxisState(ControllerAxis.LEFTX).toDouble() != 0.0) {
            val numb = currController!!.getAxisState(ControllerAxis.LEFTX).toDouble()
            when {
                numb > 0 -> leftStick(JoystickDirection.RIGHT,numb)
                else -> leftStick(JoystickDirection.LEFT,numb)
            }
        }

        if (currController!!.getAxisState(ControllerAxis.LEFTY).toString() != "-3.051851E-5") {
            val numb = currController!!.getAxisState(ControllerAxis.LEFTY).toDouble()
            when {
                numb > 0 -> leftStick(JoystickDirection.DOWN,numb)
                else -> leftStick(JoystickDirection.UP,numb)
            }
        }

        if (currController!!.getAxisState(ControllerAxis.RIGHTX).toDouble() != 0.0) {
            val numb = currController!!.getAxisState(ControllerAxis.RIGHTX).toDouble()
            when {
                numb > 0 -> rightStick(JoystickDirection.RIGHT,numb)
                else -> rightStick(JoystickDirection.LEFT,numb)
            }
        }

        System.out.println("Left:  " + currController!!.getAxisState(ControllerAxis.RIGHTX).toString())
        System.out.println("Right:  " + currController!!.getAxisState(ControllerAxis.RIGHTY).toString())

        if (currController!!.getAxisState(ControllerAxis.RIGHTY).toString() != "-3.051851E-5") {
            val numb = currController!!.getAxisState(ControllerAxis.RIGHTY).toDouble()
            when {
                numb > 0 -> rightStick(JoystickDirection.DOWN,numb)
                else -> rightStick(JoystickDirection.UP,numb)
            }
        }

    }

    private fun buttonPressed(button : ControllerButton) = currController!!.isButtonJustPressed(button)
    private fun buttonHeld(button : ControllerButton) = currController!!.isButtonPressed(button)

    open fun onStart() {}
    open fun onStop() {}
    open fun onButtonPressed(button : ControllerButton?) {}
    open fun onButtonHeld(button : ControllerButton?) {}
    open fun onBackButtonPressed(button : ControllerButton?) {}
    open fun onButtonJustPressed(button : ControllerButton) {}
    open fun leftStick(dir: JoystickDirection, value : Double) {}
    open fun rightStick(dir: JoystickDirection, value : Double) {}
    open fun backTriggers(axis: ControllerAxis, value : Double) {}


}