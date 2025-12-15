# @JsNamedArgs: a KMP compiler plugin to generate JS style functions and classes

### Status: Beta
At Philo, as we look to leverage Kotlin Multiplatform, this plugin has helped provide nicer API contracts for our JS based clients, with friendlier methods and class constructors.
### [Annotation](https://central.sonatype.com/artifact/com.philo/jsnamedargs-annotations) artifacts
### [Compiler](https://central.sonatype.com/artifact/com.philo/jsnamedargs-compiler) artifacts

## Background

### Problem Statement
Consider the following function definition below. Generally, arguments are resolved based on their position in the function. However, in Kotlin, functions support `named arguments`, which allow the consumer of a function to specify the names of the input arguments.
```kotlin
fun myCoolFunction(number: Int, isValid: Boolean, username: String) { }

// Arguments resolved by order
myCoolFunction(8, true, "philo-user")

// Using named arguments
myCoolFunction(number = 9, username = "philo-user", isValid = true)
```
These named arguments ensure the consumer has control over what is passed to the function, makes the code more readable, and prevents bugs if arguments are reordered, have the same types, etc.

Javascript does not support named arguments; however, the conventional way to pass arguments to functions/constructors is through a destructured `arguments object`, often anonymous, that contains all of the desired inputs. For example,
```js
function myCoolFunctionJs({number, isValid, username}) { }

// Arguments destructured in input object
myCoolFunctionJs({ number: 7, isValid: true, username: "js-user" })
```

By default, when exporting a KMP library to JS, there is no support for passing arguments as a destructured object, and as a result, JS consumers of functions are required to call the functions based on positional ordering. While this works, it is a bit cumbersome, especially as function inputs grow. Thus, there is a use case to generate JS functions/class constructors that support invocation via plain objects.

### Technical Approach
In order to support this JS/TS style invocation, a Kotlin `external interface` that contains the arguments as members is needed. Additionally, a new function is needed that accepts this interface, essentially as an overload of the original. This code could then be exported to JS using the `JsExport` annotation. 

In the previous `myCoolFunction` example, the required Kotlin code to generate the JS functions we want would look like:
```kotlin
@JsExport
external interface MyCoolFunctionArgs {
    val number: Int
    val isValid: Boolean
    val username: String
}

/**
 * This function accepts the interface as the arguments and then maps
 * them to the correct input for the original function
 */
@JsExport
fun myCoolFunctionWrapper(args: MyCoolFunctionArgs) {
    myCoolFunction(
        number = args.number,
        isValid = args.isValid,
        username = args.username
    )
}
```
With this exported Kotlin code, the Javascript functions and Typescript types would be created such that the Javascript consumers can call this function in the idiomatic JS way:
```js
// Call functioning using the specific function for argument passing
myCoolFunctionWrapper({
    number: 8,
    isValid: true,
    username: "philo-user"
})
```

### Solution
In order to turn all of our KMP functions and classes into JS friendly versions as described above, the `@JsNamedArgs` annotation has been created to automatically generate and export the required code. Simply add the annotation to any functions or classes that you'd like to create the new functions for.
```kotlin
@JsNamedArgs
@JsExport
fun myCoolFunction(number: Int, isValid: Boolean, username: String) { }
```
Note: The `@JsExport` annotation is also required in order for the generated method to be exported as well.

## Current Status
### Use Cases
#### Top Level Functions
When used on top level functions, new `Wrapper` functions are created for use in JS
```kotlin
@JsNamedArgs
@JsExport
fun myCoolFunction(number: Int, isValid: Boolean, username: String) { }
```
```js 
// Example JS usage
myCoolFunctionWrapper({
    number: 8,
    isValid: true,
    username: "philo-user"
})
```
#### Class Constructors
When used on top level classes, new `create...Wrapper` functions are created for use in JS
```kotlin
@JsNamedArgs
@JsExport
data class MyCoolData(val id: Int, val name: String)
```
```js 
// Example JS usage
const data = createMyCoolDataWrapper({
    id: 10,
    name: "Pam Beasley"
})
```
#### Public Class Methods
When used on top level class that have public methods, new `<Class-Name>Wrapper` functions are created for use in JS as well. They vary slightly from top level functions with their use in JS.
```kotlin
@JsNamedArgs
@JsExport
class MyCoolData(val id: Int, val name: String) {
    fun updateInfo(id: Int, name: String) { }
}
```
```js 
// Example JS usage - first create object
const data = createMyCoolDataWrapper({
    id: 10,
    name: "Pam Beasley"
})

// Now use member wrapper with object, and then the function arguments
updateInfoMyCoolDataWrapper(data, {
    id: 10,
    name: "Erin Hannon"
})
```

### Status Notes
- Currently, this annotation only works with Top Level function declarations and classes (and their public methods and inner classes). For the most part, this annotation should be used in conjunction with the `JsExport` annotation, and any classes referenced in the arguments of the functions should also be exported.
- This annotation will only generate interfaces and functions for functions and classes that are `public`, as it is assumed the JS consumers should not need access to internal or private methods/classes.
- When annotating a class, interfaces and functions will be generated for all public constructors, member methods, and inner classes
- For member methods of annotated classes, the functions generated will be extension functions on that class. When using these methods in JS, the function requires the instance of the class as the first argument, and the interface object as the second.
- If a public function or class method does not have any arguments, no interface or function will be generated.
- The annotation supports generic types used in functions and classes
- Functions are generated when doing a JS distribution

## Setup
In order to build an application using the `@JsNamedArgs` annotation, the application must also install the [KSP](https://github.com/google/ksp/tree/main) plugin in the `build.gradle.kts`
```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version <kspVersion> // See gradle.properties for latest version used
}
```
With the KSP plugin installed, you will then have to define the dependencies to these libraries in the `app/build.gradle.kts` files and enable the compiler plugin when doing JS builds
```kotlin
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            dependencies {
                implementation("com.philo:jsnamedargs-annotations:<version>")
            }
        }
    }
}

// This enables the compiler plugin to run when building a JS distribution. Without it, the
// annotation would do nothing
dependencies {
    add("kspJs", "com.philo:jsnamedargs-compiler:<version>")
}
```

## License
This application is licensed under the MIT License. See [License](/LICENSE.txt) for more license and copyright information.

## Dependencies
The most up to date dependencies can always be found in the module `build.gradle.kts` files. 

The main dependencies for this project include:
- [KSP](https://github.com/google/ksp/tree/main) - [Apache 2.0](https://github.com/google/ksp/blob/main/LICENSE)
- [KotlinPoet](https://github.com/square/kotlinpoet) - [Apache 2.0](https://github.com/square/kotlinpoet/blob/main/LICENSE.txt)

## Building Locally
To build and publish the library to your local Maven repository, run:
```bash
./gradlew -PskipSigning publishToMavenLocal
```

The `-PskipSigning` flag skips GPG signing, which is only required for publishing to Maven Central.

The version published will be the one defined in the root `build.gradle.kts` file (currently set in `allprojects { version = "..." }`).
