@echo off
echo 启动后端SpringBoot应用...
cd /d "%~dp0\backend"
if exist ".env.local" (
  for /f "usebackq tokens=1,* delims==" %%A in (".env.local") do (
    if not "%%A"=="" set "%%A=%%B"
  )
)
mvn spring-boot:run
pause

