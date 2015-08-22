lua_run
=======

## android lua runer.

* 在安卓上编辑和运行lua

* 支持lua语法高亮

* 支持lua5.2.3版本

* 集成lua5.2离线文档

## 编译步骤


```
   $ cd lua_run
   $ android update project -p . -t android-19
   $ sh build_native.sh
```

## 使用luasocket库

* 拷贝jni/luasocket/src/*.lua 到/sdcard/.luaRun/目录下

* 测试luasocket

```lua
local socket = require"socket"
local mime   = require"mime"
print("Hello from " .. socket._VERSION .. " and " .. mime._VERSION .. "!")
```

## other

### 制作过程中借用了下面内容

* jota编辑器 <https://github.com/jiro-aqua/Jota-Text-Editor>

* 文件对话框 <http://blog.csdn.net/trbbadboy/article/details/7899424>

* 角标 <https://github.com/jgilfelt/android-viewbadger>

* lua文档 <http://www.lua.org/manual/5.2/>

### 截图

![编辑器](img/1.jpg)

![运行结果](img/2.jpg)

![离线文档](img/3.jpg)

