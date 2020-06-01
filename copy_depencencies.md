
## build.gradle(app level)
```groovy
android {
  //...
  
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
  
  //...
}

dependencies {

  	//...
  
    // Navigation library
    def nav_version = "2.2.2"
    implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
    implementation "androidx.navigation:navigation-ui-ktx:$nav_version"
  
  	//Lifecycle
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.2.0'

    // CameraX core library
    def camerax_version = '1.0.0-beta03'
    implementation "androidx.camera:camera-core:$camerax_version"

    // CameraX Camera2 extensions
    implementation "androidx.camera:camera-camera2:$camerax_version"

    // CameraX Lifecycle library
    implementation "androidx.camera:camera-lifecycle:$camerax_version"

    // CameraX View class
    implementation 'androidx.camera:camera-view:1.0.0-alpha10'

    // CameraX Extensions library
    implementation "androidx.camera:camera-extensions:1.0.0-alpha10"

  	//...
}
```

## build.gradle(project level)

```groovy
buildscript {
  
  //...

  dependencies {
    //...
    classpath "androidx.navigation:navigation-safe-args-gradle-plugin:2.2.2"
  }
  //...
}
```
