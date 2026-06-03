package com.example.recapexercise

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineTask {

    interface Callback {
        fun onResult(result: String)
    }

    companion object {
        @JvmStatic
        fun execute(callback: Callback) {
            GlobalScope.launch(Dispatchers.IO) {
                delay(3000)
                withContext(Dispatchers.Main) {
                    callback.onResult("Coroutine Finished")
                }
            }
        }
    }
}
