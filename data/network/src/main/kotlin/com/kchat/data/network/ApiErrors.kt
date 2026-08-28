package com.kchat.data.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Serializable
private data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
)

private val errorJson = Json { ignoreUnknownKeys = true }

inline fun <T> apiResult(fallback: String, block: () -> T): Result<T> =
    runCatching(block).fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception(it.apiMessage(fallback))) },
    )

fun Throwable.apiMessage(fallback: String = "Có lỗi xảy ra"): String {
    if (isNetworkUnavailable()) {
        return "Không có kết nối mạng. Kiểm tra mạng rồi thử lại."
    }
    if (this !is HttpException) {
        return message?.takeIf { it.isNotBlank() } ?: fallback
    }
    val body = response()?.errorBody()?.string().orEmpty()
    val parsed = runCatching { errorJson.decodeFromString<ApiErrorBody>(body) }.getOrNull()
    val serverMessage = parsed?.message?.takeIf { it.isNotBlank() }
    return localizeApiMessage(serverMessage)
        ?: httpStatusMessage(code())
        ?: fallback
}

private fun httpStatusMessage(code: Int): String? = when (code) {
    400 -> "Yêu cầu không hợp lệ"
    401 -> "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại"
    403 -> "Bạn không có quyền thực hiện thao tác này"
    404 -> "Không tìm thấy nội dung"
    409 -> "Dữ liệu đã tồn tại hoặc xung đột"
    413 -> "Dữ liệu quá lớn"
    422 -> "Dữ liệu không hợp lệ"
    429 -> "Quá nhiều yêu cầu, vui lòng thử lại sau"
    in 500..599 -> "Máy chủ gặp sự cố, vui lòng thử lại sau"
    else -> null
}

private fun Throwable.isNetworkUnavailable(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        when (current) {
            is java.net.ConnectException,
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.NoRouteToHostException,
            is java.io.InterruptedIOException,
            -> return true
        }
        val msg = current.message.orEmpty()
        if (msg.contains("Failed to connect", ignoreCase = true) ||
            msg.contains("ECONNREFUSED", ignoreCase = true) ||
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("Network is unreachable", ignoreCase = true) ||
            msg.contains("Software caused connection abort", ignoreCase = true) ||
            msg.contains("Cleartext HTTP traffic", ignoreCase = true)
        ) {
            return true
        }
        current = current.cause
    }
    return false
}

private fun localizeApiMessage(message: String?): String? {
    if (message.isNullOrBlank()) return null
    return when {
        message.contains("Current password is incorrect", ignoreCase = true) ->
            "Mật khẩu hiện tại không đúng"
        message.contains("upper, lower, digit", ignoreCase = true) ||
            message.contains("special character", ignoreCase = true) ->
            "Mật khẩu mới phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
        message.contains("size must be between", ignoreCase = true) ->
            "Mật khẩu mới phải có ít nhất 8 ký tự"
        message.contains("Invalid credentials", ignoreCase = true) ->
            "Email/tên đăng nhập hoặc mật khẩu không đúng"
        message.contains("Account is locked", ignoreCase = true) ->
            "Tài khoản đã bị khóa"
        message.contains("Authentication required", ignoreCase = true) ->
            "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại"
        message.contains("already in progress", ignoreCase = true) ->
            "Đang có cuộc gọi khác trong chat này"
        message.contains("already in another call", ignoreCase = true) ->
            "Bạn hoặc đối phương đang trong cuộc gọi khác"
        message.contains("only supported in 1-1", ignoreCase = true) ->
            "Chỉ gọi được trong chat 1-1"
        message.contains("Call is not ringing", ignoreCase = true) ->
            "Cuộc gọi không còn đổ chuông"
        message.contains("Call already ended", ignoreCase = true) ->
            "Cuộc gọi đã kết thúc"
        message.contains("must be a valid phone number", ignoreCase = true) ->
            "Số điện thoại không hợp lệ"
        message.contains("Username already taken", ignoreCase = true) ->
            "Tên đăng nhập đã được sử dụng"
        message.contains("registrationToken must not be blank", ignoreCase = true) ||
            (message.contains("registration_token", ignoreCase = true) &&
                message.contains("must not be blank", ignoreCase = true)) ->
            "Vui lòng xác minh OTP trước khi đăng ký"
        message.contains("Email already registered", ignoreCase = true) ||
            message.contains("Username or email already registered", ignoreCase = true) ->
            "Email hoặc tên đăng nhập đã được đăng ký"
        message.contains("Invalid or expired registration token", ignoreCase = true) ||
            message.contains("invalid_registration_token", ignoreCase = true) ->
            "Phiên đăng ký không hợp lệ hoặc đã hết hạn — vui lòng xác minh email lại"
        message.contains("Email does not match verified address", ignoreCase = true) ->
            "Email không khớp với địa chỉ đã xác minh"
        message.contains("must be alphanumeric or underscore", ignoreCase = true) ->
            "Tên đăng nhập chỉ gồm chữ, số và _"
        message.contains("must be a well-formed email", ignoreCase = true) ||
            message.contains("must be a valid email", ignoreCase = true) ->
            "Email không hợp lệ"
        message.contains("Invalid or expired reset token", ignoreCase = true) ||
            message.contains("invalid_reset_token", ignoreCase = true) ->
            "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"
        message.contains("Invalid or expired OTP", ignoreCase = true) ||
            message.contains("invalid_otp", ignoreCase = true) ->
            "Mã OTP không đúng hoặc đã hết hạn"
        message.contains("Not a member of this room", ignoreCase = true) ||
            message.contains("Not a room member", ignoreCase = true) ->
            "Bạn không phải thành viên phòng này"
        message.contains("Only owner or admin", ignoreCase = true) ->
            "Chỉ quản trị viên mới thực hiện được"
        message.contains("Not your device", ignoreCase = true) ->
            "Không phải thiết bị của bạn"
        message.contains("Cannot revoke the current device", ignoreCase = true) ->
            "Không thể đăng xuất thiết bị đang dùng"
        message.contains("Resource not found", ignoreCase = true) ||
            message.contains("not found", ignoreCase = true) ->
            "Không tìm thấy nội dung"
        message.contains("Validation failed", ignoreCase = true) ->
            "Dữ liệu không hợp lệ"
        message.contains("Modify window is 15 minutes", ignoreCase = true) ->
            "Chỉ sửa/xóa tin trong vòng 15 phút"
        message.contains("file exceeds 25MB", ignoreCase = true) ->
            "File vượt quá 25MB"
        message.contains("Unexpected error", ignoreCase = true) ->
            "Máy chủ gặp sự cố, vui lòng thử lại sau"
        else -> message
    }
}
