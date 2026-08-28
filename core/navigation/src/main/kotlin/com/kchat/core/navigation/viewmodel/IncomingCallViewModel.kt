package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import com.kchat.core.model.CallInfo
import com.kchat.data.repository.CallSignalBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val bus: CallSignalBus,
) : ViewModel() {
    val incoming: SharedFlow<CallInfo> = bus.incoming

    fun isBusy(): Boolean = bus.isBusy()

    fun activeCallId(): String? = bus.activeCallId()

    fun clearIncoming() {
        bus.clearIncoming()
    }
}
