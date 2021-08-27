[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Fragmentation-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5937)
[![Build Status](https://travis-ci.org/YoKeyword/Fragmentation.svg?branch=master)](https://travis-ci.org/YoKeyword/Fragmentation)
[![Download](https://jitpack.io/v/Z-P-J/Fragmentation.svg)](https://jitpack.io/#Z-P-J/Fragmentation)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)


# ZFragmentation

### [中文版 README.md](https://github.com/Z-P-J/ZFragmentation/blob/master/README_CN.md)

![](/gif/logo.png)

Fragmentation is a powerful library managing Fragment for Android.

It is designed for "Single Activity + Multi-Fragments" and "Multi-FragmentActivities + Multi-Fragments" architecture to simplify development process.

## Demo
The first demo shows the basic usage of the library. The second one shows the way to implement an app which is similar to Instagram. Complicated nested fragments' usage demo are also showed.

## [Download APK](https://www.pgyer.com/fragmentation)

<img src="/gif/demo1.gif" width="280px"/> <img src="/gif/demo2.gif" width="280px"/>
 <img src="/gif/demo3.gif" width="280px"/>

## Feature

**1. Develop complicated nested fragment app rapidly**

**2. Use fragment's stack view dialog to debug easily**

**3. Add launch mode, startForResult etc. to provide similar behavior of Activity**

**4. Add onBackPressedSupport() method to support back button press monitoring in Fragment**

**5. Add onSupportVisible(), onLazyInitView() to simplify dev**

**6. Easily manage Fragment transition animations**

**7. To simplify the communication between Fragment**

**8. Support SwipeBack to pop**

<img src="/gif/stack.png" width="150px"/> <img src="/gif/log.png" width="300px"/>       <img src="/gif/SwipeBack.png" width="150px"/>

## How do I use Fragmentation?

**1、build.gradle**
````gradle
// appcompat-v7 is required
implementation 'com.github.Z-P-J:ZFragmentation:1.0.0'
````

**2. Activity `extends` SupportActivity or `implements` ISupportActivity：(refer to [MySupportActivity](https://github.com/Z-P-J/Fragmentation/blob/master/demo/src/main/java/com/zpj/fragmentation/demo/demo_flow/base/MySupportActivity.java))**
````java
// since v1.0.0, forced extends of SupportActivity is not required, you can use interface + delegate to implement your own SupportActivity 
public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(...);
      	// Fragmentation is recommended to initialize in the Application
        Fragmentation.builder()
          	 // show stack view. Mode: BUBBLE, SHAKE, NONE
             .stackViewMode(Fragmentation.BUBBLE)
             .debug(BuildConfig.DEBUG)
             ...
             .install();

        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());  //load root Fragment
        }
    }
````

**3. Fragment `extends` SupportFragment or `implements` ISupportFragment：(refer to [MySupportFragment](https://github.com/Z-P-J/Fragmentation/blob/master/demo/src/main/java/com/zpj/fragmentation/demo/demo_flow/base/MySupportFragment.java))：**
````java
// since v1.0.0, forced extends of SupportActivity is not required, you can use interface + delegate to implement your own SupportActivity
public class HomeFragment extends SupportFragment {

    private void xxx() {
      	// launch a new Fragment, other methods: start(fragment,SINGTASK)、startForResult、startWithPop etc.
        start(DetailFragment.newInstance(HomeBean));
      	// check wiki for other pop, find and animation setting related API
    }
}
````

## [WIKI](https://github.com/Z-P-J/ZFragmentation/wiki) , [CHANGELOG](https://github.com/Z-P-J/ZFragmentation/blob/master/CHANGELOG.md)

## LICENSE
````
Copyright 2016 YoKey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````