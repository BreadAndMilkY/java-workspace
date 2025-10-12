@REM 关闭命令回显，执行命令时不显示命令本身
@echo off
@REM 将代码页设置为UTF-8编码(65001)，>nul表示不显示执行结果
chcp 65001 >nul
@REM 允许变量延迟扩展，在for等语句中用%a%语法读取变量一直是初始值 ，即使你在里面改变了变量的值 ，变量延迟扩展用!a! 感叹号代替百分号读取变量，取出的是变量的实时值
setlocal enabledelayedexpansion

echo ========================================
echo   递归清理IDE配置文件
echo   目标: target文件夹, .idea文件夹, *.iml文件
echo   作者: zhengqingya
echo   开发时间: 2021/7/20 16:42
echo ========================================
echo.


:: 安全确认
set /p confirm="⚠ 确认要清理当前目录及子目录下的所有IDE配置文件吗？(y/N): "
if /i not "!confirm!"=="y" (
    echo 操作已取消。
    timeout /t 3 >nul
    exit /b 0
)


echo ================================================================================


echo 正在扫描所有目标文件...
for /r . %%d in (target .idea *.iml) do (
    if exist "%%d" (
        set "opt_target_name=%%~nxd"
        set "opt_dir=%%~fd"

        echo ****** [debug] 操作目标：【!opt_target_name!】 【!opt_dir!】

        echo !opt_dir! | findstr /i "\\node_modules\\" >nul
        if !errorlevel! equ 0 (
            echo ****** [debug] 跳过node_modules：!opt_dir!
        ) else (
            if "!opt_target_name:~-4!" == ".iml" (
                del /f /q "!opt_dir!" 2>nul && (
                    echo   ✓ 删除成功 【!opt_dir!】
                ) || (
                    echo   ✗ 删除失败 【!opt_dir!】
                )
            ) else (
                rd /s /q "!opt_dir!" 2>nul && (
                    echo   ✓ 删除成功 【!opt_dir!】
                ) || (
                    echo   ✗ 删除失败 【!opt_dir!】
                )
            )
        )
    )
)


echo.
pause