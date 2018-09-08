/*
 * Copyright (c) 2018. Louis Cognault Ayeva Derman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package splitties.views

import android.os.Build.VERSION.SDK_INT
import android.view.View
import splitties.uithread.isUiThread
import java.util.concurrent.atomic.AtomicInteger

fun generateViewId(): Int = when {
    isUiThread -> uiThreadLastGeneratedId.also {
        uiThreadLastGeneratedId = (if (it == 1) aaptIdsStart else it) - 1
    }
    SDK_INT >= 17 -> View.generateViewId()
    else -> generatedViewIdCompat()
}

/** aapt-generated IDs have the high byte nonzero. Clamp to the range under that. */
private const val aaptIdsStart = 0x00FFFFFF
private var uiThreadLastGeneratedId = aaptIdsStart - 1

private val nextGeneratedId = AtomicInteger(1)
private fun generatedViewIdCompat(): Int {
    while (true) {
        val result = nextGeneratedId.get()
        // aapt-generated IDs have the high byte nonzero. Clamp to the range under that.
        var newValue = result + 1
        if (newValue > aaptIdsStart) newValue = 1 // Roll over to 1, not 0.
        if (nextGeneratedId.compareAndSet(result, newValue)) {
            return result
        }
    }
}
