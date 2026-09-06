package com.kchat.data.network.auth

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences

internal val ReplaceEmptyPreferences = ReplaceFileCorruptionHandler<Preferences> {
    emptyPreferences()
}
