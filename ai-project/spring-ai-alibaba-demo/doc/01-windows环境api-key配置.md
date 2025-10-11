# windows环境api-key配置

1、获取api-key: https://bailian.console.aliyun.com

2、配置环境变量 `AI_DASHSCOPE_API_KEY`
![](./images/windows环境api-key配置_1759071658333.png)

3、重启电脑，java应用程序才能正常获取环境变量值。

4、idea 命令端测试 `echo %AI_DASHSCOPE_API_KEY%`
![](./images/01-windows环境api-key配置_1759157900131.png)
