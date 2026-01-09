package com.tryanks.groovecontroller

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

object VibrationHelper {
    fun vibrate(context: Context) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
    }
}
