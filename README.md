# CameraX Example
This project is a simple example about how use `CameraX Android`.

## Functionalities
- Take photo
- Save photo to the gallery

## CameraX Dependencies and others things used (build.gradle)
<details> <summary>Dependencies</summary>
  
```groovy
dependencies {
    //(...)
    
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.2.0'

    // Navigation library
    def nav_version = "2.2.2"
    implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
    implementation "androidx.navigation:navigation-ui-ktx:$nav_version"

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

    //(...)
}
```

</details>

## CameraX's official project
[Click here!](https://github.com/android/camera-samples/tree/master/CameraXBasic)
