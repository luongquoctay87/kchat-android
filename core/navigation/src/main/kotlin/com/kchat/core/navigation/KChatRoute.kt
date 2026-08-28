package com.kchat.core.navigation

import kotlinx.serialization.Serializable

sealed interface KChatRoute {
    @Serializable data class Login(val infoMessage: String? = null) : KChatRoute
    @Serializable data object Register : KChatRoute
    @Serializable data object ForgotPassword : KChatRoute
    @Serializable data class VerifyResetOtp(val email: String) : KChatRoute
    @Serializable data class ResetPassword(val token: String) : KChatRoute
    @Serializable data object Main : KChatRoute
    @Serializable data class Chat(val roomId: String, val title: String = "") : KChatRoute
    @Serializable data object CreateGroup : KChatRoute
    @Serializable data class GroupInfo(
        val roomId: String,
        val title: String = "",
        val isChannel: Boolean = false,
    ) : KChatRoute
    @Serializable data object Profile : KChatRoute
    @Serializable data object ChangePassword : KChatRoute
    @Serializable data object Appearance : KChatRoute
    @Serializable data object Notifications : KChatRoute
    @Serializable data class ImageViewer(val mediaUrl: String, val title: String = "Ảnh") : KChatRoute
    @Serializable data class InChatSearch(val roomId: String, val title: String = "") : KChatRoute
    @Serializable data object Privacy : KChatRoute
    @Serializable data object PrivacyDm : KChatRoute
    @Serializable data object PrivacyQuietHours : KChatRoute
    @Serializable data object PinSettings : KChatRoute
    @Serializable data object Devices : KChatRoute
    @Serializable data object Storage : KChatRoute
    @Serializable data class Call(
        val roomId: String,
        val contactName: String,
        val callType: String,
        val callId: String = "",
    ) : KChatRoute
}
