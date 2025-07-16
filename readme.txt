# 关键文件

- 添加网络权限
app\src\main\AndroidManifest.xml

- 主程序
app\src\main\java\com\example\webtv\MainActivity.kt

# 优化后解码文件
app\src\main\assets\hls.js

# 按键提示
app\src\main\assets\index.html

- 全屏状态: 上下键换台; 双击确认/OK键刷新; 菜单键切换频道列表;
- 列表状态: 上下键切换选项; 确认/OK键进入频道; 菜单键返回全屏;
- 双击返回键退出

# 问题
偶尔卡死,需双击OK键刷新 
启动和换台较慢; 
按键操作有延迟; 
连续播放时设备温度80+
可能出现多次重启任无法播放的情况, 需要重新安装或清除应用数据