# About
By default, the server sends information about ping of players to everyone, which you can see in the tablist as the green bar. Some modified clients allow to see the exact value instead of just bars. This feature spoofs ping value of all players to the configured value, hiding their true ping. I don't know why would anyone want to do that.

# Configuration
Find this section in your **config.yml**
```
ping-spoof:
  enabled: false
  value: 0
```
To enable the feature, set `enabled: true`.  
When the feature is enabled, everyone's ping value will be shown as value configured in `value` field.

The ping intervals for bars are client sided and are as following:  
| Ping value  | Displayed icon |
| ----------- | -------------- |
| Negative | ![image](https://user-images.githubusercontent.com/6338394/179717654-add90ec7-1321-41a3-99e6-76420e665833.png) |
| 0 - 149  | ![image](https://user-images.githubusercontent.com/6338394/179717531-3c6409b6-6bf8-41c1-a150-ce0ed615e5a5.png)  |
| 150 - 299  | ![image](https://user-images.githubusercontent.com/6338394/179717804-cfedfe33-7846-4b60-8559-be2b902b4a75.png)  |
| 300 - 599  | ![image](https://user-images.githubusercontent.com/6338394/179717915-0bccc83c-a2db-4459-9a76-58754f1307df.png)  |
| 600 - 999  | ![image](https://user-images.githubusercontent.com/6338394/179717996-eec0942a-17c9-44ff-9428-3137f76f50a4.png)  |
| 1000+  | ![image](https://user-images.githubusercontent.com/6338394/179718067-a3c86f83-cb57-436e-97ef-f3de394d9c41.png)  |