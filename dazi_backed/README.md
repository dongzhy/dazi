## 一、项目介绍
本项目是伙伴匹配社交平台后端服务，基于 Spring Boot 开发，采用前后端分离架构。
核心围绕用户兴趣标签实现智能好友匹配、多类型队伍管理两大核心业务；整合 Redis 缓存、Redisson 分布式锁、定时任务、编辑距离算法、全局异常处理、数据脱敏 等主流技术方案，适合作为 Java Web 综合实战项目学习与二次开发。
### 核心能力
- 用户体系：注册、登录、注销、信息修改、权限控制、数据脱敏
- 标签检索：根据兴趣标签筛选同类用户
- 智能匹配：基于编辑距离 (Levenshtein) 算法实现标签相似度匹配推荐
- 队伍管理：支持公开 / 私有 / 加密三种队伍，实现队伍增删改、加入 / 退出 / 转让队长
- 性能优化：Redis 接口缓存 + 定时任务缓存预热，降低 DB 压力 :Redisson 分布式锁解决队伍加入等高并发场景问题
- 基础能力：统一返回结果、全局异常捕获、参数校验、MD5 密码加密
## 二、技术栈
### 核心框架
- 基础框架：Spring Boot 2.5.x、Spring MVC
- 持久层：MyBatis、MyBatis-Plus 3.4.x（分页、条件查询、CRUD 封装）
- 数据库：MySQL 8.0+
###  中间件 & 工具
- 缓存：Redis + Spring Data Redis
- 分布式锁：Redisson
- 序列化：Gson（JSON 解析、标签转换）
- 加密工具：MD5 密码加盐加密
- 定时任务：Spring Scheduled（缓存预热定时任务）
- API 文档：Swagger / Knife4j 接口在线文档
- 日志：SLF4J + Lombok

## 快速启动
运行 npm install 安装依赖
本地运行mysql、redis服务

创建数据库,复制create_sql.txt新建数据库

配置文件修改
打开application.yml修改本地数据库、redis连接信息



## 核心模块说明
### 用户模块
- POST /api/user/register：用户注册（账号校验、密码 MD5 加盐加密、星球编号唯一校验）
- POST /api/user/login：用户登录（Session 保存登录态、密码校验）
- POST /api/user/logout：用户注销（清空 Session 登录态）
- GET /api/user/current：获取当前登录用户（自动脱敏）
- GET /api/user/search：管理员查询所有用户
- GET /api/user/search/tags：根据标签列表筛选用户
- POST /api/user/update：修改个人信息（昵称、头像、标签、手机号、邮箱等）
- GET /api/user/match：标签相似度匹配用户（核心算法接口）
- GET /api/user/recommend：首页用户推荐（Redis 缓存）

### 智能匹配模块（标签+编辑算法）
- 实现逻辑
1. 获取当前登录用户标签（JSON 字符串），解析为标签列表
2. 查询数据库中非本人、标签不为空的所有用户
3. 使用 编辑距离算法 计算两组标签的差异值：
   数值越小 → 标签相似度越高
4. 按相似度升序排序，截取前端指定数量用户
5. 结果写入 Redis 缓存（有效期 10 分钟），下次请求优先走缓存
- 核心工具类
AlgorithmUtils.java：封装 Levenshtein 编辑距离动态规划实现。

### 队伍模块（Team）
队伍分为三种类型：
- 0 公开队伍：所有人可直接加入
- 1 私有队伍：仅创建者可见，禁止加入
- 2 加密队伍：需输入正确密码方可加入
#### 接口
- POST /api/team/add：创建队伍（校验队伍人数、名称、过期时间）
- POST /api/team/update：修改队伍信息（仅队长 / 管理员可操作）
- GET /api/team/get：根据 ID 查询队伍
- GET /api/team/list：分页 / 条件查询队伍列表
- POST /api/team/join：加入队伍（分布式锁防并发）
- POST /api/team/quit：退出队伍（人数为 1 则自动解散，多人自动转让队长）
- POST /api/team/delete：解散队伍（仅队长可操作）
- GET /api/team/list/my/create：查询我创建的队伍
- GET /api/team/list/my/join：查询我加入的队伍

### 项目亮点 
- 算法落地：将经典编辑距离算法应用到实际业务（标签相似度匹配）
- 缓存架构：接口缓存 + 定时预热缓存，完整缓存实战方案
- 分布式锁：Redisson 实现分布式锁，解决集群并发问题
- 分层架构：标准 Controller -> Service -> Mapper 三层架构，职责清晰；
- 安全设计：密码加密、数据脱敏、接口权限校验
- 健壮性：参数校验、异常捕获、缓存降级、空值判断全覆盖


## 存在问题
- 单一编辑距离算法 + 同步全量计算： 全量遍历用户计算相似度，用户量大后接口耗时严重； 单一算法匹配精度低，推荐结果不够智能；缓存策略简单，冷热数据无区分，缓存命中率一般。
- 当前标签搜索、用户检索全部依赖 MySQL：标签为 JSON 字符串，like 模糊查询效率极低，数据量上万后接口卡顿； 不支持分词、模糊匹配、权重排序；复杂多标签交集 / 并集查询，SQL 复杂且性能差。
- 现为单体的mvc架构，所有业务都在一个工程，不适用于团队多人开发，业务互相影响，当某个模块出问题，全部系统崩溃