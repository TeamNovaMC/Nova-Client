package com.radiantbyte.novaclient.application

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import com.radiantbyte.novaclient.activity.CrashHandlerActivity

class AppContext : Application(), Thread.UncaughtExceptionHandler {

    companion object {

        lateinit var instance: AppContext
            private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

//        val ensureCompositionCreatedMethod = AbstractComposeView::class.java.getDeclaredMethod("ensureCompositionCreated")
//        ensureCompositionCreatedMethod.isAccessible = true

//        Pine.hook(
//            AbstractComposeView::class.java.getDeclaredMethod("onMeasure", Int::class.java, Int::class.java),
//            object : MethodHook() {
//                override fun beforeCall(callFrame: Pine.CallFrame) {
//                    if (callFrame.thisObject is PopupLayout) {
//                        val popupLayout = callFrame.thisObject as PopupLayout
//                        val args = callFrame.args
//                        val widthMeasureSpec = args[0] as Int
//                        val heightMeasureSpec = args[1] as Int
//
//                        ensureCompositionCreatedMethod.invoke(callFrame.thisObject)
//                        popupLayout.overrideInternalOnMeasure(widthMeasureSpec, heightMeasureSpec)
//                    }
//                }
//            }
//        )
//
//        Pine.hook(
//            AbstractComposeView::class.java.getDeclaredMethod("onLayout", Boolean::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java),
//            object : MethodHook() {
//                override fun beforeCall(callFrame: Pine.CallFrame) {
//                    if (callFrame.thisObject is PopupLayout) {
//                        val popupLayout = callFrame.thisObject as PopupLayout
//                        val args = callFrame.args
//                        val changed = args[0] as Boolean
//                        val left = args[1] as Int
//                        val top = args[2] as Int
//                        val right = args[3] as Int
//                        val bottom = args[4] as Int
//                        popupLayout.overrideInternalOnLayout(changed, left, top, right, bottom)
//                    }
//                }
//            }
//        )

        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val stackTrace = e.stackTraceToString()
        val deviceInfo = buildString {
            val declaredFields = Build::class.java.declaredFields
            for (field in declaredFields) {
                field.isAccessible = true
                try {
                    val name = field.name
                    var value = field.get(null)

                    if (value == null) {
                        value = "null"
                    } else if (value.javaClass.isArray) {
                        value = (value as Array<out Any?>).contentDeepToString()
                    }

                    append(name)
                    append(": ")
                    appendLine(value)
                } catch (_: Throwable) {
                }
            }
        }


        startActivity(Intent(this, CrashHandlerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("message", buildString {
                appendLine("An unexpected exception / error happened!")
                appendLine("Please tell the developer to fix it!")
                appendLine()
                appendLine(deviceInfo)
                appendLine("Thread: ${t.name}")
                appendLine("Thread Group: ${t.threadGroup?.name}")
                appendLine()
                appendLine("Stack Trace: $stackTrace")
            })
        })
        Process.killProcess(Process.myPid())
    }

}