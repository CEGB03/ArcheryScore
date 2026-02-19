// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.hilt.android) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

// 🔐 Verificación de credenciales (tarea opcional)
tasks.register("checkCredentials") {
    doLast {
        val googleServicesFile = file("app/google-services.json")
        val supabaseUrl = project.findProperty("SUPABASE_URL") as String? ?: "NO_CONFIGURADO"
        val supabaseAnonKey = project.findProperty("SUPABASE_ANON_KEY") as String? ?: "NO_CONFIGURADO"
        
        val errors = mutableListOf<String>()
        
        if (!googleServicesFile.exists()) {
            errors.add("❌ app/google-services.json no encontrado")
            errors.add("   → Descárgalo de Firebase Console")
            errors.add("   → Mira CREDENTIALS_SETUP.md para instrucciones")
        }
        
        if (supabaseUrl == "NO_CONFIGURADO" || supabaseUrl == "https://CONFIGURAR") {
            errors.add("❌ SUPABASE_URL no configurado en local.properties")
        }
        
        if (supabaseAnonKey == "NO_CONFIGURADO" || supabaseAnonKey == "CONFIGURAR") {
            errors.add("❌ SUPABASE_ANON_KEY no configurado en local.properties")
        }
        
        if (errors.isNotEmpty()) {
            println("\n⚠️  CREDENCIALES FALTANTES:")
            errors.forEach { println(it) }
            println("\n📖 Lee CREDENTIALS_SETUP.md para configurar correctamente\n")
            throw GradleException("Credenciales no configuradas. Build abortado.")
        } else {
            println("✅ Todas las credenciales están configuradas correctamente")
        }
    }
}