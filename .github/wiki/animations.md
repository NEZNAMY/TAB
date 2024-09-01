# Content
* [Creating an animation](#creating-an-animation)
* [Using an animation](#using-an-animation)
* [Tips & Tricks](#tips--tricks)
* [External tools](#external-tools)
* [Additional info](#additional-info)
  * [Additional note 1 - Frame synchronization](#additional-note-1---frame-synchronization)
* [Examples](#examples)
  * [Example 1](#example-1)

# Creating an animation
Animations can be created in **animations.yml** file.  
To begin, you can copy an already existing animation or directly modify already existing one.  
Let's take a look at an example:
```animations:  
Welcome:  #this is name of the animation
  change-interval: 400  #this is refresh interval of the animation in milliseconds
  texts:  #the frames of animation
    - "&7&lW_"  
    - "&7&lW_"  
    - "&7&lWe_"  
    - "&7&lWel_"  
    - "&7&lWelc_"  
    - "&7&lWelco_"  
    - "&7&lWelcom_"  
    - "&7&lWelcome_"  
    - "&7&lWelcome_"  
    - "&7&lWelcome_"  
    - "&7&lWelcome_"  
    - "&7&lWelcom_"  
    - "&7&lWelco_"  
    - "&7&lWelc_"  
    - "&7&lWel_"  
    - "&7&lWe_"  
    - "&7&lW_"  
    - "&7&lW_"
```
This is an animation called `Welcome` with `change-interval` of `400` milliseconds, which is 2.5 refreshes per second. `Texts` are the actual frames of animation.

# Using an animation
To use an animation, use `%animation:<name>%` format. In the example above it's `%animation:Welcome%`. Animations are supported everywhere. That includes header/footer, prefix/suffix, belowname, yellow number, bossbar and more.

# Tips & Tricks
* Animations don't need to be fast. You can use them as a changing text in general.
* Animations can also be used as a short alias to a long text. Just create an animation with one frame.

# External tools
If you want an animation that's too complicated to be done manually, you can check out these websites:
* https://www.birdflop.com/resources/animtab/
* https://starve-l.github.io/tab-animation.html
* https://srnyx.com/gradient

# Additional info
## Additional note 1 - Frame synchronization
All animations start at the same time, which is time of plugin being (re)loaded. Thanks to this, all animations with the same amount of frames and same change interval will be synchronized.  
If you want to synchronize 2 animations with different change interval and different amount of frames, you need to make sure `frame count`*`change interval` is same for both animations, for example one animation with 10 frames and 1000ms and second animation with 2 frames and 5000ms.

# Examples
## Example 1
Swichtes between green and red in an extra smooth way (could replace the default animation bar for a christmas theme).
Works on 1.16+ only due to RGB/Hex values being used.

![](https://i.imgur.com/P9PN80H.png)
```
MyAnimation1:
  change-interval: 50
  texts:
  - "&#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m "
  - "&#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m "
  - "&#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m "
  - "&#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m "
  - "&#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m "
  - "&#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m "
  - "&#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m "
  - "&#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m "
  - "&#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m "
  - "&#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m "
  - "&#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m "
  - "&#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m "
  - "&#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m "
  - "&#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m "
  - "&#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m "
  - "&#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m "
  - "&#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m "
  - "&#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m &#55e600&l&m "
  - "&#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m &#4bf200&l&m "
  - "&#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m &#42ff00&l&m "
  - "&#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m &#4bf200&l&m "
  - "&#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m &#55e600&l&m "
  - "&#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m &#5ed900&l&m "
  - "&#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m &#68cc00&l&m "
  - "&#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m &#71bf00&l&m "
  - "&#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m &#7bb300&l&m "
  - "&#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m &#84a600&l&m "
  - "&#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m &#8e9900&l&m "
  - "&#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m &#978c00&l&m "
  - "&#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m &#a18000&l&m "
  - "&#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m &#aa7300&l&m "
  - "&#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m &#b36600&l&m "
  - "&#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m &#bd5900&l&m "
  - "&#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m &#c64d00&l&m "
  - "&#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m &#d04000&l&m "
  - "&#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m &#d93300&l&m "
  - "&#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m &#e32600&l&m "
  - "&#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m &#ec1a00&l&m "
  - "&#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m &#f60d00&l&m "
  - "&#42ff00&l&m &#4bf200&l&m &#55e600&l&m &#5ed900&l&m &#68cc00&l&m &#71bf00&l&m &#7bb300&l&m &#84a600&l&m &#8e9900&l&m &#978c00&l&m &#a18000&l&m &#aa7300&l&m &#b36600&l&m &#bd5900&l&m &#c64d00&l&m &#d04000&l&m &#d93300&l&m &#e32600&l&m &#ec1a00&l&m &#f60d00&l&m &#ff0000&l&m "
```