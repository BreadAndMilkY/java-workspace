@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   递归清理IDE配置文件
echo   目标: target文件夹, .idea文件夹, *.iml文件
echo   作者: zhengqingya
echo   开发时间: 2021/7/20 16:42
echo ========================================
echo.

:: 管理员权限检查
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠ 请以管理员身份运行此脚本以获得最佳清理效果
    echo.
)

:: 安全确认
set /p confirm="⚠ 确认要清理当前目录及子目录下的所有IDE配置文件吗？(y/N): "
if /i not "!confirm!"=="y" (
    echo 操作已取消。
    timeout /t 3 >nul
    exit /b 0
)

echo.
echo 开始扫描并清理...

:: 初始化计数器
set target_count=0
set idea_count=0
set iml_count=0
set total_count=0

echo 正在扫描所有目标文件...
for /r . %%a in (target .idea *.iml) do (
    if exist "%%a" (
        set /a total_count+=1
        echo [!total_count!] 处理: %%~fa
        
        :: 判断文件类型并执行相应删除操作
        if /i "%%~nxa"=="target" (
            rd /s /q "%%a" 2>nul && (
                set /a target_count+=1
                echo   ✓ target文件夹删除成功
            ) || (
                echo   ✗ target文件夹删除失败
            )
        ) else if /i "%%~nxa"==".idea" (
            rd /s /q "%%a" 2>nul && (
                set /a idea_count+=1
                echo   ✓ .idea文件夹删除成功
            ) || (
                echo   ✗ .idea文件夹删除失败
            )
        ) else if /i "%%~xa"==".iml" (
            del /f /q "%%a" 2>nul && (
                set /a iml_count+=1
                echo   ✓ .iml文件删除成功
            ) || (
                echo   ✗ .iml文件删除失败
            )
        )
        echo.
    )
)

:: 统计结果
echo ========================================
echo 扫描完成！
echo 发现目标文件总数: !total_count! 个
echo ✓ 成功删除target文件夹: !target_count! 个
echo ✓ 成功删除.idea文件夹: !idea_count! 个  
echo ✓ 成功删除.iml文件: !iml_count! 个
echo ========================================
echo.

:: 残留检查
set remain_flag=0
echo 正在检查残留文件...
for /r . %%a in (target .idea *.iml) do (
    if exist "%%a" (
        echo ⚠ 残留: %%~fa
        set remain_flag=1
    )
)

if !remain_flag! equ 0 (
    echo ✓ 所有目标文件已清理干净！
) else (
    echo ⚠ 部分文件删除失败，请以管理员身份重新运行或手动检查
)

echo.
pause