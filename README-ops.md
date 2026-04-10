# qqAuto 运维说明

## 当前交付范围

- 用户注册、登录、找回密码说明页
- 用户中心：首页、订单、钱包、消息、个人资料
- 后台管理：首页、公告发布、商品配置、充值审核、提现审核
- 本地默认可用 H2 文件库；生产环境默认切换到香港机现有 `mysql8` 容器中的 `qqauto` 库

## 明确未实现

- 第三方 QQ 账号密码托管
- Cookie 导入、扫码代挂、自动秒赞、自动做任务
- 实时支付网关
- 短信/邮箱自动找回密码

## 本地运行

```powershell
mvn spring-boot:run
```

默认地址：

- 前台/登录：`http://localhost:8086/login`
- 后台：`http://localhost:8086/admin`
- H2 控制台：`http://localhost:8086/h2-console`

生产默认路径：

- `https://zoumh.com/qqauto`

默认管理员：

- 用户名：`admin`
- 密码：`zmh0012078070`

## Jenkins / Docker

- Jenkins 直接执行仓库内 `Jenkinsfile`
- 容器内端口固定 `8086`
- 宿主机数据目录建议挂载到：`/zoumh/docker/qqauto/data`
- 也可直接执行 [deploy-docker.sh](/D:/myproject/qqAuto/deploy/deploy-docker.sh)
- 生产默认使用 MySQL：`mysql8 / qqauto / root / zoumh`
- 首次部署脚本会自动执行 `CREATE DATABASE IF NOT EXISTS qqauto`
- 若要改库名或账号，可参考 [application-prod-mysql.properties.example](/D:/myproject/qqAuto/deploy/application-prod-mysql.properties.example)
- 生产默认 `APP_CONTEXT_PATH=/qqauto`

## 反代建议

- Nginx 将目标域名或路径反代到 `127.0.0.1:8086`
- 子路径部署时，使用 [nginx.qqauto.conf.example](/D:/myproject/qqAuto/deploy/nginx.qqauto.conf.example) 的 `/qqauto/` 反代形式
- 生产环境若要切到 MySQL，可补充 `application-prod.properties` 后再改容器环境变量
