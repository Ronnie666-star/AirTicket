#!/bin/sh
# 同容器内：nginx 后台起，java 前台跑（容器生命周期跟随后端）
nginx
exec java -jar /app/app.jar
