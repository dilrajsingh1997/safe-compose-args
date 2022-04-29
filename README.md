`compose-annotation`    
[![compose-annotation](https://img.shields.io/badge/version-1.0.1-yellow.svg)](https://mvnrepository.com/artifact/io.github.dilrajsingh1997/compose-annotation)


`compose-annotation-processor`     
[![compose-annotation](https://img.shields.io/badge/version-1.0.3-green.svg)](https://mvnrepository.com/artifact/io.github.dilrajsingh1997/compose-annotation-processor)

# Safe-Compose-Args
This is a compile-time annotation processor that will generate type-safe methods to get/retrieve arguments for a jetpack compose destination so that we don't have to construct string routes or read from back stack entry. Please go through the release notes to find the latest features [[annotation](https://github.com/dilrajsingh1997/safe-compose-args/blob/main/AnnotationReleaseNotes.md)] [[annotation-processor](https://github.com/dilrajsingh1997/safe-compose-args/blob/main/AnnotationProcessorReleaseNotes.md)]. Integration guide is mentioned below. This repo is explained in the following articles: 
<br />
[Part-1](https://proandroiddev.com/safe-compose-arguments-an-improved-way-to-navigate-in-jetpack-compose-95c84722eec2)
<br />
[Part-2](https://proandroiddev.com/safe-compose-arguments-an-improved-way-to-navigate-in-jetpack-compose-part-2-218a6ae7a027)
<br />

Consider the following example for better understanding. Let's say we have a `UserPage` composable that expects 2 arguments, a `userId` which is of String type and a `uniqueUser` which is a custom data class object. This concept of expecting 2 arguments can be represented by the following interface-
```
@ComposeDestination
interface UserPage {
    val userId: String
    val uniqueUser: User

    companion object
}
```

This will cause the plugin to generate the following class-
```
data class UserPageArgs (
  val userId: String, 
  val uniqueUser: User, 
)
fun Companion.parseArguments(backStackEntry: NavBackStackEntry): UserPageArgs {
  return UserPageArgs(
    userId = backStackEntry.arguments?.getString("userId") ?: "", 
    uniqueUser = backStackEntry.arguments?.getSerializable("uniqueUser") as User ?: throw NullPointerException("parcel value not found"), 
  )
}
val Companion.argumentList: MutableList<NamedNavArgument> 
  get() = mutableListOf(
    navArgument("userId") {
      type = NavType.StringType
    },
    navArgument("uniqueUser") {
      type = UserPage_UniqueUserNavType
    },
  )
fun Companion.getDestination(userId: String, uniqueUser: User, ): String {
  return "UserPage?" + 
      "userId=$userId," + 
      "uniqueUser=${Uri.encode(gson.toJson(uniqueUser))}" + 
      ""
}
val Companion.route
  get() = "UserPage?userId={userId},uniqueUser={uniqueUser}"
```

As you might have noticed, we need a special `NavType` to work with passing custom data types. The plugin will generate these navigation types also for us.
```
val UserPage_UniqueUserNavType: NavType<User> = object : NavType<User>(false) {
  override val name: String
    get() = "uniqueUser"
  override fun get(bundle: Bundle, key: String): User {
    return bundle.getSerializable(key) as User
  }
  override fun parseValue(value: String): User {
    return gson.fromJson(value, object : TypeToken<User>() {}.type)
  }
  override fun put(bundle: Bundle, key: String, value: User) {
    bundle.putSerializable(key, value)
  }
}
```


Usage in your navigation graph will be as follows-
```
composable(
    route = UserPage.route,
    arguments = UserPage.argumentList
) {
    val userPageArgs = UserPage.parseArguments(it)
    // content
}
```

Similarly, to navigate to a composable, we can call the helper function as follows-
```
navHostController.navigate(UserPage.getDestination(
    userId = "userId", 
    uniqueUser = User(name = "name", age = -1)
))
```

## Concept of Interface
Interface defines the structure of a composable destination. This has many benefits as detailed out in the articles. A short version is as follows-
1. Compile-time safety for all the number of arguments for any composable and their types
2. Make sure the same key is not re-used for different arguments

## Salient features
1. Support for default values
2. Support for nullable types
3. Support for serializable and parcelable types
4. Support for list type objects (ArrayLisy<T>)
5. Support for native array types, `IntArray` in kotlin or `int[]` in java (IntArray, LongArray, BooleanArray, FloatArray)

# Integration guide to include this as a library in your project
The article at the end of the section explains the process in depth. But for a quick setup, please follow the following-
- Include ksp plugin in `app/build.gradle` -> `id("com.google.devtools.ksp") version "1.5.30-1.0.0"`
- Include the ksp library, annotation library and the annotation processor as follows
```
implementation "io.github.dilrajsingh1997:compose-annotation:1.0.1"
ksp("io.github.dilrajsingh1997:compose-annotation-processor:1.0.3")
implementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0")
```
- Construct the gradle file so that the build time generate files are accesible by the normal code
```
androidComponents.onVariants { variant ->
    kotlin.sourceSets.findByName(variant.name)?.kotlin?.srcDirs(
        file("$buildDir/generated/ksp/${variant.name}/kotlin")
    )
}
ksp {
    arg("ignoreGenericArgs", "false")
}
```
[Integration-Guide](https://proandroiddev.com/safe-compose-arguments-an-improved-way-to-navigate-in-jetpack-compose-part-3-2e5ab79b9a05)
