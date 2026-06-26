@echo off
echo 启动后端SpringBoot应用...
cd /d "%~dp0\backend"
mvn spring-boot:run
pause


