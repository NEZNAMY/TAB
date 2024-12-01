# Content
* [Creating an animation](#creating-an-animation)
* [Using an animation](#using-an-animation)
* [Tips & Tricks](#tips--tricks)
* [External tools](#external-tools)
* [Additional info](#additional-info)
  * [Additional note 1 - Frame synchronization](#additional-note-1---frame-synchronization)

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
To use an animation, use `%animation:<name>%` format.
In the example above it's `%animation:Welcome%`.
Animations are supported everywhere.
That includes header/footer, prefix/suffix, belowname, playerlist objective, bossbar and more.

# Tips & Tricks
* Animations don't need to be fast. You can use them as a changing text in general.
* Animations can also be used as a short alias to a long text. You can do this by creating an animation with one frame.

# External tools
If you want an animation too complicated to be done manually, you can check out these websites:
* https://www.birdflop.com/resources/animtab/
* https://starve-l.github.io/tab-animation.html
* https://srnyx.com/gradient

# Additional info
## Additional note 1 - Frame synchronization
All animations start at the same time, which is time of plugin being (re)loaded.
Thanks to this, all animations with the same number of frames and the same change interval will be synchronized.  
If you want to synchronize multiple animations with a different change interval and different number of frames,
you need to make sure `frame count`*`change interval` is same for both animations,
for example one animation with 10 frames and 1000ms and second animation with 2 frames and 5000ms.
