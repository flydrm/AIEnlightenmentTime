# App Signing Configuration

## Production Release Signing

### 1. Generate Release Keystore

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ai-enlightenment
```

### 2. Configure Signing in `local.properties`

Add these lines to your `local.properties` file (never commit this file):

```properties
storeFile=/path/to/your/release-keystore.jks
storePassword=your_keystore_password
keyAlias=ai-enlightenment
keyPassword=your_key_password
```

### 3. Update `app/build.gradle.kts`

Add signing configuration:

```kotlin
android {
    signingConfigs {
        create("release") {
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
                
                storeFile = file(localProperties["storeFile"] as String)
                storePassword = localProperties["storePassword"] as String
                keyAlias = localProperties["keyAlias"] as String
                keyPassword = localProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other release configurations
        }
    }
}
```

### 4. Build Release APK

```bash
./gradlew assembleRelease
```

The signed APK will be available at:
`app/build/outputs/apk/release/app-release.apk`

### 5. Security Best Practices

- **Never commit** the keystore file or passwords to version control
- Store the keystore in a secure location with backups
- Use strong passwords for both keystore and key
- Consider using Google Play App Signing for additional security
- Rotate keys periodically for enhanced security

### 6. Google Play App Signing

For production releases on Google Play:

1. Enable App Signing in Google Play Console
2. Upload your app signing key
3. Google Play will manage your app signing key
4. You'll use an upload key for subsequent releases

This provides additional security as your app signing key is managed by Google.