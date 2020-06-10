**C-logic is a framework that allows you to intuitively and quickly create UI** using the principle of reusable components. This principle is the most modern and effective in the field of UI development, and it underlies such frameworks as React and Flutter.

---
**C-logic is similar in functionality to Jetpack Compose** and provides all its main features. But unlike the Jetpack Compose, C-logic is fully compatible with the components of the Android support library - Fragments and Views, so you do not have to rewrite all your code to implement this framework. C-logic works on annotation processing and generates code on top of Fragment and View classes.

## Installation

In your root build.gradle:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
In your app/build.gradle:
```gradle
android {
	...
	dataBinding {
		enabled = true
	}
	sourceSets {
		main {
			java {
				srcDir "${buildDir.absolutePath}/generated/source/kaptKotlin/"
			}
		}
	}
}
dependencies {
	implementation 'com.github.ArtemiyDmtrvch.c-logic:c-logic-base:0.9.+'
	implementation 'com.github.ArtemiyDmtrvch.c-logic:c-logic-annotations:0.9.+'
	kapt 'com.github.ArtemiyDmtrvch.c-logic:c-logic-processor:0.9.+'
}
```
## Why do you need C-logic:
- You will write ***at least 2 times less code*** than if you wrote using Android SDK and any architecture.
- The entry threshold into your project will be minimal, because there are very few rules, and they are simple and universal for all situations
- Your code will be a priori reusable, and you will never have a situation when you have a Fragment, but you need to display it in the RecyclerView
- The principles laid down in C-logic are the most promising for development for any platform, and soon they will become the standard for Android development

## Now let's see how this is all achieved.

### 1. One rule for all components

Suppose you have a Fragment in which an argument is passed, which is then displayed in the TextView. Here's how you do it:
```kotlin
@MakeComponent
class MyFragment : ComponentScheme<Fragment, MyFragmentViewModel>({ MyFragmentBinding::class })

class MyFragmentViewModel : ComponentViewModel() {

    @Prop
    var myText by state<String?>(null)
}
```
```xml
// MyFragmentBinding xml
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
The annotation processor will generate a class **MyFragmentComponent** inherited from the Fragment with the ViewModel and the field `myText`. When this field is changed, an argument will be added to the Fragment, which will then be passed to the ViewModel and bound to the TextView using Android data binding. As a result, **this class can be used like this:**
```kotlin
showFragment(MyFragmentComponent().apply { myText = "Hello world!" })
```

Now let's imagine a similar example, but you will have a FrameLayout to which the text is bound, which is then displayed in the TextView. And here is how you do it:
```kotlin
@MakeComponent
class MyLayout : ComponentScheme<FrameLayout, MyLayoutViewModel>({ MyLayoutBinding::class }) // MyLayoutBinding xml is the same as MyFragmentBinding xml

class MyLayoutViewModel : ComponentViewModel() {

    @Prop
    var myText by state<String?>(null)
}
```
The annotation processor will generate a class **MyLayoutComponent** inherited from the FrameLayout with the ViewModel and the BindingAdapter for `myText` attribute, which will pass the value to the ViewModel. As a result, **this class can be used like this:**
```xml
 <my.package.MyLayoutComponent
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    myText='@{"Hello world!"}' />
```

*As you can see, the codes for the Fragment and for the View are completely identical.*

**The single rule is:** create a class inherited from `ComponentScheme`, specify the super component as the first type argument, ViewModel as the second and mark this class with `MakeComponent` annotation. Then mark with `Prop` annotation those properties that may come from the parent component (the properties must be vars). An argument will be generated for the Fragment, and a binding adapter for the View. Then build the project and use generated classes.

**Note:** you may need to build the project twice so that the binding appapters and component classes are generated correctly.

Also, in the case of a View, you can set `Prop.twoWay = true`, and then a two-way binding adapter will be generated for the View. It will send the value back when the annotated property changes.
```kotlin
@Prop(twoWay = true)
var twoWayText: String? = null //a two-way binding adapter will be generated
```
### 2. Observable state

Certain properties in the model are declared by the `state` delegate. Each time one of these properties changes, data binding is performed. And this mechanism allows you to **forget about LiveData and ObservableFields.** Now the data for binding can be just vars.

This mechanism optimally distributes the load on the main thread (data binding is placed at the end of the message queue of the main Looper). And if there are many consecutive state changes data binding will only be done once:
```kotlin
property1 = "Hello world!"
property2 = 123
property3 = true
//data binding will only be done once
```

Data binding is performed at one time for all Views by replacing the old bound ViewModel with a new one. And this does not make the binding algorithm more complicated than using LiveData and ObservableFields, since all native data binding adapters and generated ones are not executed if the new value is equal to the old one.

**Note:** two-way data binding also works - changes in the view will change your state property

### 3. Functional rendering

Suppose you need to display one or another layout, depending on the condition. Here's how you do it:
```kotlin
@MakeComponent
class MyScrollView : ComponentScheme<ScrollView, MyScrollViewModel>({ viewModel ->
    if(viewModel.showFirstLayout)
        FirstLayoutBinding::class
    else
	SecondLayoutBinding::class
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

### 4. Shared state

Suppose you need to observe in one ViewModel changes of the property of another ViewModel. Here's how you do it:
```kotlin
@SharedViewModel
class MySharedViewModel : ComponentViewModel() {
    var sharedText by observable<String?>(null)
}

class MyPlainViewModel : ComponentViewModel() {
    var myText by observable<String?>(null) { print(it) }
    init {
        // when changing the value of `sharedText`, this value will be set to `myText`
        ::myText.isMutableBy(MySharedViewModel::sharedText)
    }
}
```
A ViewModel with a shared property is marked with `SharedViewModel` annotation, and the shared property is declared by the `observable` or `state` delegate. Then, in the observing ViewModel, in the initial block, using `isMutableBy` method, it is indicated which property values will be duplicated to your property (your property must be a var).

Consider using the `observable` delegate wherever you need to observe changes of a variable, because it, unlike `kotlin.properties.Delegates.observable`, does not call `onChanged` if the new value is equal to the old.
