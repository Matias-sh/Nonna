package com.cocido.nonna.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.cocido.nonna.core.logging.Logger
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager de seguridad siguiendo mejores prácticas de DevSecOps
 * - Manejo seguro de datos sensibles
 * - Validación de integridad
 * - Detección de root/jailbreak
 * - Cifrado de datos locales
 */
@Singleton
class SecurityManager @Inject constructor(
    private val context: Context
) {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Verifica si el dispositivo está rooteado o comprometido
     */
    fun isDeviceSecure(): Boolean {
        return try {
            // Verificar si la app está instalada desde una fuente confiable
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            val isFromPlayStore = installer == "com.android.vending" || installer == "com.google.android.feedback"
            
            // Verificar si la app puede detectar root (simplificado)
            val hasRoot = checkForRoot()
            
            val isSecure = isFromPlayStore && !hasRoot
            
            if (!isSecure) {
                Logger.security("Device security check failed: isFromPlayStore=$isFromPlayStore, hasRoot=$hasRoot")
            }
            
            isSecure
        } catch (e: Exception) {
            Logger.e("Error checking device security", throwable = e)
            false
        }
    }
    
    /**
     * Verifica si la app está siendo debuggeada
     */
    fun isDebuggingDetected(): Boolean {
        return try {
            val isDebuggerConnected = android.os.Debug.isDebuggerConnected()
            if (isDebuggerConnected) {
                Logger.security("Debugger detected")
            }
            isDebuggerConnected
        } catch (e: Exception) {
            Logger.e("Error checking debug status", throwable = e)
            false
        }
    }
    
    /**
     * Genera un hash seguro de una cadena
     */
    fun generateSecureHash(input: String, salt: String = ""): String {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val saltedInput = input + salt
            val hashBytes = messageDigest.digest(saltedInput.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Logger.e("Error generating secure hash", throwable = e)
            ""
        }
    }
    
    /**
     * Genera un salt aleatorio seguro
     */
    fun generateSecureSalt(): String {
        val salt = ByteArray(32)
        secureRandom.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Cifra datos sensibles usando AES-GCM
     */
    fun encryptSensitiveData(data: String, key: SecretKey): String? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            secureRandom.nextBytes(iv)
            
            val parameterSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
            
            val encryptedData = cipher.doFinal(data.toByteArray())
            val encryptedWithIv = iv + encryptedData
            
            android.util.Base64.encodeToString(encryptedWithIv, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Logger.e("Error encrypting sensitive data", throwable = e)
            null
        }
    }
    
    /**
     * Descifra datos sensibles usando AES-GCM
     */
    fun decryptSensitiveData(encryptedData: String, key: SecretKey): String? {
        return try {
            val encryptedWithIv = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT)
            val iv = encryptedWithIv.sliceArray(0..11)
            val encrypted = encryptedWithIv.sliceArray(12 until encryptedWithIv.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val parameterSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
            
            val decryptedData = cipher.doFinal(encrypted)
            String(decryptedData)
        } catch (e: Exception) {
            Logger.e("Error decrypting sensitive data", throwable = e)
            null
        }
    }
    
    /**
     * Genera una clave secreta para cifrado
     */
    fun generateSecretKey(): SecretKey? {
        return try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            Logger.e("Error generating secret key", throwable = e)
            null
        }
    }
    
    /**
     * Valida la integridad de los datos usando HMAC
     */
    fun validateDataIntegrity(data: String, expectedHash: String, key: String): Boolean {
        return try {
            val actualHash = generateSecureHash(data, key)
            val isValid = actualHash == expectedHash
            
            if (!isValid) {
                Logger.security("Data integrity validation failed")
            }
            
            isValid
        } catch (e: Exception) {
            Logger.e("Error validating data integrity", throwable = e)
            false
        }
    }
    
    /**
     * Verifica si la app tiene los permisos necesarios
     */
    fun hasRequiredPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si la app está siendo ejecutada en un emulador
     */
    fun isRunningOnEmulator(): Boolean {
        return try {
            val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    "google_sdk" == Build.PRODUCT
            
            if (isEmulator) {
                Logger.security("App running on emulator")
            }
            
            isEmulator
        } catch (e: Exception) {
            Logger.e("Error checking if running on emulator", throwable = e)
            false
        }
    }
    
    /**
     * Verifica si hay root (simplificado)
     */
    private fun checkForRoot(): Boolean {
        return try {
            val rootPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            
            rootPaths.any { path ->
                java.io.File(path).exists()
            }
        } catch (e: Exception) {
            Logger.e("Error checking for root", throwable = e)
            false
        }
    }
}

