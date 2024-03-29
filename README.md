**UI-generator is a framework that allows you to intuitively and quickly create UI** using the principle of reusable components. This principle is the most modern and effective in the field of UI development, and it underlies such frameworks as React and Flutter.

---
**UI-generator is similar in functionality to [Jetpack Compose](https://developer.android.com/jetpack/compose)** and provides all its main features. But unlike the Jetpack Compose, UI-generator is fully available now and is compatible with the components of the Android support library - Fragments and Views, so you do not have to rewrite all your code to implement this framework. UI-generator works on annotation processing and generates code on top of Fragment and View classes.

## Installation

In your root build.gradle.kts:
```kts
allprojects {
   repositories {
      ...
      maven(url = "https://jitpack.io")
   }
}
```
In your app/build.gradle.kts:
```kts
plugins {
   id("com.google.devtools.ksp") version "<ksp-version>"
}

ksp {
    arg("packageName", "<your-package-name>")
}

android {
   ...
   dataBinding {
      isEnabled = true
   }
}

dependencies {
   implementation("com.github.ArtemiyDmtrvch.ui-generator:ui-generator-base:+")
   implementation("com.github.ArtemiyDmtrvch.ui-generator:ui-generator-annotations:+")
   ksp("com.github.ArtemiyDmtrvch.ui-generator:ui-generator-processor:+")
}
```
## Why do you need UI-generator
- You will write ***at least 2 times less code*** than if you wrote using Android SDK and any architecture.
- The entry threshold into your project will be minimal, because there are very few rules, and they are simple and universal for all situations
- Your code will be a priori reusable, and you will never have a situation when you have a Fragment, but you need to display it in the RecyclerView
- The principles laid down in UI-generator are the most promising for development for any platform, and soon they will become the standard for Android development

## Now let's see how this is all achieved

### 1. One rule for all components

Suppose you have a Fragment in which an argument is passed, which is then displayed in the TextView. Here's how you do it:
```kotlin
@MakeComponent
class MyFragment : ComponentScheme<Fragment, MyFragmentViewModel>({ R.layout.my_fragment })

class MyFragmentViewModel : ComponentViewModel() {

    @Prop
    var myText by state<String?>(null)
}
```
```xml
// my_fragment.xml
<layout>
    <data>
        <variable
            name="viewModel"
            type="my.package.MyFragmentViewModel" />
    </data>
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@{viewModel.myText}" />
</layout>
```
The annotation processor will generate a class **MyFragmentComponent** inherited from the Fragment with the ViewModel and the field `myText`. When this field is changed, an argument will be added to the Fragment, which will then be passed to the ViewModel and bound to the TextView using [Android data binding](https://developer.android.com/topic/libraries/data-binding). As a result, **this class can be used like this:**
```kotlin
showFragment(MyFragmentComponent().apply { myText = "Hello world!" })
```

Now let's imagine a similar example, but you will have a FrameLayout to which the text is bound, which is then displayed in the TextView. And here is how you do it:
```kotlin
@MakeComponent
class MyLayout : ComponentScheme<FrameLayout, MyLayoutViewModel>({ R.layout.my_layout })

class MyLayoutViewModel : ComponentViewModel() {

    @Prop
    var myText by state<String?>(null)
}
```
```xml
// my_layout.xml
<layout>
    <data>
        <variable
            name="viewModel"
            type="my.package.MyLayoutViewModel" />
    </data>
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@{viewModel.myText}" />
</layout>
```
The annotation processor will generate a class **MyLayoutComponent** inherited from the FrameLayout with the ViewModel and the [binding adapter](https://developer.android.com/topic/libraries/data-binding/binding-adapters) for `myText` attribute, which will pass the value to the ViewModel. As a result, **this class can be used like this:**
```xml
 <my.package.MyLayoutComponent
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    myText='@{"Hello world!"}' />
```

As you can see, the codes for the Fragment and for the View are completely identical. You do not need to write View and Fragment classes at all, they are generated automatically.

**The single rule is:** create a class inherited from `ComponentScheme`, specify the super component as the first type argument, ViewModel as the second and mark this class with `MakeComponent` annotation. Then mark with `Prop` annotation those properties that may come from the parent component (the properties must be vars). An argument will be generated for the Fragment, and a binding adapter for the View. Then build the project and use generated classes.

**Note:** you may need to build the project twice so that the binding adapters and component classes are generated correctly.

Also, in the case of a View:
- You can set `Prop.twoWay = true`, and then a two-way binding adapter will be generated for the View. It will send the value back when the annotated property changes.
```kotlin
@Prop(twoWay = true)
var twoWayText: String? = null //a two-way binding adapter will be generated
```
- You can bind xml attribute to your state property:
```kotlin
var picture by state<Drawable?>(null, attr = R.styleable.MyViewComponent_picture)
```
```xml
<MyViewComponent
	app:picture="@drawable/myPicture"/>
```

### 2. Observable state

First you create the `viewModel` variable in your layout.xml. Then you declare certain properties in the ViewModel by the `state` delegate. Each time one of these properties changes, data binding is performed. And this mechanism allows you to **forget about LiveData and ObservableFields.** Now the data for binding can be just vars.

This mechanism optimally distributes the load on the main thread (data binding is placed at the end of the message queue of the main Looper). And if there are many consecutive state changes data binding will only be done once:
```kotlin
property1 = "Hello world!"
property2 = 123
property3 = true
//data binding will only be done once
```

Data binding is performed at one time for all Views by replacing the old bound ViewModel with a new one. And this does not make the binding algorithm more complicated than using LiveData and ObservableFields, since all native data binding adapters and generated ones are not executed if the new value is the same as the old one.

You can manually initiate data binding by calling `onStateChanged` function in ViewModel.

**Note:** two-way data binding also works - changes in the view will change your state property

### 3. Functional rendering

Suppose you need to display one or another layout, depending on the condition. Here's how you do it:
```kotlin
@MakeComponent
class MyScrollView : ComponentScheme<ScrollView, MyScrollViewModel>({ viewModel ->
    if(viewModel.showFirstLayout)
        R.layout.first_layout
    else
        R.layout.second_layout
})
```
This lambda is called at the same time as data binding, that is, after a state change. In essence, this is also data binding, but to a super component. In addition to the ViewModel, a super component is passed to this lambda as `this`, and you can bind any data to it:
```kotlin
@MakeComponent
class MyButton : ComponentScheme<Button, MyButtonViewModel>({ viewModel ->
    //Button is passed to this lambda as `this`
    this.isEnabled = viewModel.isEnabled
    null
})
```

To bind data to a super component, you can use the functions located in `ru.impression.ui_generator_base.Binders.kt`. All of them are executed only if the new value is different from the set value. Example of the function `updateLayoutParams`:
```kotlin
@MakeComponent
class MyTextView : ComponentScheme<TextView, MyTextViewModel>({
    updateLayoutParams(width = MATCH_PARENT, height = WRAP_CONTENT, marginTop = 16)
    null
})
```

### 5. Coroutine support

#### suspend funs

Suppose that before you display some data, you need to load it first. Here's how you do it:
```kotlin
var greeting: String? by state({
    delay(2000)
    "Hello world!"
})
```
All you need to do is inherit your model from `CoroutineViewModel`. It implements `CoroutineScope` in which your suspend lambda is executed. You can also execute all your other coroutines in this scope. Scope is canceled when `onCleared` is called.

You can also observe the loading state of your data. For example, in order to show the progress bar during loading:
```xml
<ProgressBar
    isVisible="@{viewModel.greetingIsLoading}"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```
```kotlin
// After `isLoading` becomes `false`, the data binding will be called and the ProgressBar will be hidden.
val greetingIsLoading: Boolean get() = ::greeting.isLoading
```

And also you can reload your data:
```kotlin
fun reloadGreeting() {
    // The suspend lambda will be called again and `isLoading` will become `true`.
    // After that, the data binding will be called and the ProgressBar wil be shown again at loading time.
    ::greeting.reload()
}
```

#### Flows

Suppose you need to subscribe to the Flow and display all its elements. Here's how you do it:
```kotlin
var countDown: Int? by state(flow {
    delay(1000)
        emit(3)
        delay(1000)
        emit(2)
        delay(1000)
        emit(1)
        delay(1000)
        emit(0)
})
```

***For detailed examples see module `app`.***