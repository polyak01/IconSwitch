# IconSwitch

The library is a custom Switch widget inspired by this [dribbble shot](https://dribbble.com/shots/2978168-Map-to-list-view). 

![GifSample](https://github.com/polyak01/IconSwitch/blob/master/data/3J8gYHy.gif)

## Gradle 
Add this into your dependencies block.
```
implementation 'com.github.parminder93:IconSwitch:v1.0.5'
```
## Sample
Please see the [sample app](sample/src/main) for a library usage example.

## Wiki
#### Usage:
Simply add an IconSwitch to your view hieararchy. Either programatically or using xml:
```xml
<com.polyak.iconswitch.IconSwitch
  android:id="@+id/icon_switch"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  app:isw_icon_left="@drawable/ic_format_list_bulleted_white_18dp"
  app:isw_icon_right="@drawable/ic_location_on_white_18dp" />
```

### API
#### General
Size of the widget is controlled by the attribute:
```xml
<com.polyak.iconswitch.IconSwitch
  android:isw_icon_size="@dimen/your_size" />
```
Default selection can be set using:
```xml
<com.polyak.iconswitch.IconSwitch
  android:isw_default_selection="left|right" />
```
To control the current state or get information about it, use:
```java
iconSwitch.setChecked();
iconSwitch.getChecked();
iconSwitch.toggle();
iconSwitch.setEnabled();
```

#### Color
To customize colors of the widget, you can use the following self-explanatory attributes:
```xml
<com.polyak.iconswitch.IconSwitch
  app:isw_background_color="#fff"
  app:isw_thumb_color_left="#fff"
  app:isw_thumb_color_right="#fff"
  app:isw_inactive_tint_icon_left="#fff"
  app:isw_inactive_tint_icon_right="#fff"
  app:isw_active_tint_icon_left="#fff"
  app:isw_active_tint_icon_right="#fff" />
```
or setter-methods:
```java
iconSwitch.setBackgroundColor(color);
iconSwitch.setThumbColorLeft(color);
iconSwitch.setThumbColorRight(color);
iconSwitch.setActiveTintIconLeft(color);
iconSwitch.setInactiveTintIconLeft(color);
iconSwitch.setActiveTintIconRight(color);
iconSwitch.setInactiveTintIconRight(color);
```

#### Callback
To listen for the check changed events use:
```java
iconSwitch.setCheckedChangeListener(listener);

public interface CheckedChangeListener {
  void onCheckChanged(Checked current);
}

enum Checked { LEFT, RIGHT }
```

## License
```
Copyright 2017 Yaroslav Polyakov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
