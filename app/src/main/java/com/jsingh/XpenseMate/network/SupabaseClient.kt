package com.jsingh.XpenseMate.network

import com.jsingh.XpenseMate.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

object SupabaseNetwork {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            scheme = "xpensemate"
            host = "login"

            alwaysAutoRefresh = true
        }

        install(Postgrest)

        install(ComposeAuth) {
            googleNativeLogin(serverClientId = "240423962092-gam7qbaqvt2epp75f697g6q4k0jq8sl5.apps.googleusercontent.com")
        }
    }
}