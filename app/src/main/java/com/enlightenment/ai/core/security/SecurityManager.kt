package com.enlightenment.ai.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 安全管理器
 * 负责敏感数据的加密存储和安全访问
 */
@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * 安全存储API密钥
     */
    fun storeApiKey(keyName: String, apiKey: String) {
        encryptedPrefs.edit().putString(keyName, apiKey).apply()
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(keyName: String): String? {
        return encryptedPrefs.getString(keyName, null)
    }
    
    /**
     * 加密敏感数据
     */
    fun encryptData(data: String): String {
        return try {
            val keyAlias = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(keyAlias))
            
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(data.toByteArray())
            
            // 组合IV和密文
            val combined = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
            
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("Failed to encrypt data", e)
        }
    }
    
    /**
     * 解密敏感数据
     */
    fun decryptData(encryptedData: String): String {
        return try {
            val keyAlias = getOrCreateSecretKey()
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            // 分离IV和密文
            val iv = ByteArray(GCM_IV_LENGTH)
            val ciphertext = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, ciphertext, 0, ciphertext.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), spec)
            
            String(cipher.doFinal(ciphertext))
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt data", e)
        }
    }
    
    /**
     * 验证应用完整性
     */
    fun verifyAppIntegrity(): Boolean {
        // 在生产环境中，这里应该实现：
        // 1. 签名验证
        // 2. 防篡改检查
        // 3. SafetyNet/Play Integrity API调用
        return true
    }
    
    /**
     * 清除所有敏感数据
     */
    fun clearSensitiveData() {
        encryptedPrefs.edit().clear().apply()
    }
    
    private fun getOrCreateSecretKey(): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            KEY_ALIAS
        } else {
            KEY_ALIAS
        }
    }
    
    private fun getSecretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(alias, null) as SecretKey
    }
    
    companion object {
        private const val ENCRYPTED_PREFS_NAME = "ai_enlightenment_secure_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "ai_enlightenment_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }
}

/**
 * 内容安全过滤器
 */
class ContentSafetyFilter {
    
    private val inappropriateKeywords = listOf(
        // 不适合儿童的关键词列表
        // 实际应用中应该使用更完善的过滤系统
    )
    
    /**
     * 检查内容是否适合儿童
     */
    fun isContentSafe(content: String): Boolean {
        val lowercaseContent = content.lowercase()
        return inappropriateKeywords.none { keyword ->
            lowercaseContent.contains(keyword)
        }
    }
    
    /**
     * 过滤不适当内容
     */
    fun filterContent(content: String): String {
        var filtered = content
        inappropriateKeywords.forEach { keyword ->
            filtered = filtered.replace(keyword, "*".repeat(keyword.length), ignoreCase = true)
        }
        return filtered
    }
}