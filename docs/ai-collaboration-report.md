# AI 协同开发报告 — visit-subsystem

> 医药代表（MR）学术访视管理子系统：计划 → 签到 → 签退（合规校验）→ 拜访报告 → 月度看板。
> 技术栈：Java 21、Spring Boot 3.5.16、Spring Data JPA、Flyway、H2（内嵌文件库）、Thymeleaf + REST、Docker（多阶段构建 + Compose）。
> 协作方式：AI 分 6 个阶段推进，每个阶段结束后停下等人工评审；人负责纠偏与决策，AI 负责实现与自检。

---

## 1. 关键 Prompt 记录

| #   | 阶段/主题          | 关键 Prompt（原文节选）                                                                                                                                                                                                                                          | 意图与产出                                                                                      |
| --- | -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| 1   | 项目启动 / 协作契约    | “Build a runnable Spring Boot web app… **Go in small phases and STOP after each so I can review**… **Do NOT invent version numbers**… **Call out every assumption/tradeoff**… Begin with Phase 1 ONLY.”                                                  | 明确范围、技术约束与“分阶段+可评审+不臆造+显式权衡”的协作方式；确立 6 阶段路线                                                |
| 2   | 数据模型微调         | “`materials_distributed`… we can make it a **boolean**, to indicate whether material has been distributed over the visit”                                                                                                                                | 将 `materialsDistributed` 从文本改为布尔；产出 `BOOLEAN NOT NULL DEFAULT FALSE` + 实体 `boolean`        |
| 3   | 阶段推进           | “looks good locally, go ahead with **phase 3**”                                                                                                                                                                                                          | 人工评审通过后进入 Phase 3（Haversine + 合规 + 服务 + 测试）                                                |
| 4   | 纠偏·输出不完整       | “I can only see Haversine and its test for now, **continue**”                                                                                                                                                                                            | 指出 AI 只交付了部分产物，要求补齐 compliance/service/tests                                               |
| 5   | 配置外置           | “**let's not hardcode** this two compliance check values here, **move them to separate properties file**”                                                                                                                                                | 阈值从常量迁到 `@ConfigurationProperties` + `application.yml`                                     |
| 6   | 触发代码评审         | “**code review current changes**”                                                                                                                                                                                                                        | 触发系统化评审，暴露防御性拷贝、测试耦合阈值、check-in GPS 未参与合规、TOCTOU 等问题                                       |
| 7   | 静默失败·负时长       | “the only case durationSeconds can be capped to 0… is when `checkOutAt <= getCheckInAt`… the code will **silently mark this visit as SHORT_DURATION**, we need to **indicate this kind of error rather than mixing them** with the real SHORT_DURATION.” | 指出 `Math.max` 把“时钟回拨/脏数据”伪装成真实 `SHORT_DURATION`；改为抛 `InconsistentVisitTimestampsException` |
| 8   | 静默失败·全面审计      | “**are there any other ‘failing silently’ logic in code?**”                                                                                                                                                                                              | 系统性审计吞异常 / clamp / 兜底默认 / 宽容解析，产出 NaN 坐标等真实缺陷                                              |
| 9   | 阶段推进           | “let's move onto next phrase” → Phase 4                                                                                                                                                                                                                  | 进入 Web（Thymeleaf）+ REST（ProblemDetail 错误映射）                                                |
| 10  | Dashboard 决策   | 图表：**本地 vendor Chart.js**（离线/Docker 可用）；计数口径：**按** `plannedAt` **的当月全部访视**，`status` 可选过滤                                                                                                                                                                 | 确定离线图表方案与统计口径，避免 CDN 运行时依赖                                                                 |
| 11  | Docker 阶段 + 环境 | “**I've turned off the proxy, continue**”（Phase 6）                                                                                                                                                                                                       | 在受限网络下推进多阶段 Dockerfile + compose（命名卷持久化 H2）                                                |
| 12  | 核实 AI 声明       | “**I didn't see an application-docker.yml, only application.yml**”                                                                                                                                                                                       | 质疑 AI 声称的产物是否存在，要求“以代码为准”（核实后确认已提交于 `b9fde7c`）                                             |
| 13  | 时区修复           | “this is **UTC**, make it work with **utc+8** timezone to display correctly on dashboard”（指向 `DashboardService` 的 `systemDefault`）                                                                                                                       | 容器 JVM 默认 UTC 导致月份/显示偏移；引入 `AppTimeZone`（`visit.time-zone` 默认 `Asia/Shanghai`）             |
| 14  | 全面代码评审         | “**You are a senior backend engineer doing a thorough code review**… Focus on **logic correctness, persistence, test coverage**.”                                                                                                                        | 得到分层、带严重级别、带行号的评审（C1–C7）与优先级行动清单                                                           |
| 15  | 前端 UX          | “not very easy to use on safari and chrome, **the hour/minutes need to be input manually, make it a real time picker with selectable hour/minute**”                                                                                                      | 把 `datetime-local` 换成 `date` + 小时/分钟 `<select>`，跨浏览器可点选                                    |
| 16  | 看板月份导航 bug | `I got "2026-11-12T16:34:03.123+08:00  WARN 96993 --- [visit-subsystem] [nio-8080-exec-1] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Method parameter 'month': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; For input string: "DECEMBER"]" when clicking on previous/next month` | 点“上一月/下一月”触发 400（`month=DECEMBER`）；定位 `YearMonth.getMonth()` 返回枚举 → 改 `getMonthValue()`，加回归测试 |

## 2. AI 纠偏亮点案例

### 案例 A：负时长被 `Math.max` 掩盖成“真实”的 `SHORT_DURATION`

- Prompt:
  > the only case durationSeconds can be capped to 0 by this Math.max() is when checkOutAt <= getCheckInAt, right? which is not likely to happen in real world unless the timestamp persisted is broken. In this case the code will silently mark this visit as SHORT_DURATION, we need to indicate this kind of error rather than mixing them with the real SHORT_DURATION.
  >
  > 附带代码片段：`long durationSeconds = Math.max(0, Duration.between(visit.getCheckInAt(), checkOutAt).toSeconds());`
- **问题**：`checkOut` 里 `Math.max(0, Duration.between(...).toSeconds())` 会把“时钟回拨 / 脏数据导致的负时长”截断为 0，随后按普通短访视记为 `SHORT_DURATION`——把**系统/数据故障伪装成 MR 的合规违规**。
- **发现方式**：人工 review 时，用户用精确推理指出“唯一触发 `Math.max` 的情形就是 `checkOutAt <= checkInAt`”，并强调“应显式报错，而不是与真实短访视混在一起”。
- **纠偏动作**：改为显式判断 `elapsed.isNegative()` → 抛 `InconsistentVisitTimestampsException`，并新增“时钟回拨”测试（断言访视保持 `CHECKED_IN`、不落库）。
- **结果**：损坏的时间线以专用异常暴露（Phase 4 映射为 500），不再被“洗白”为合规结论。
- **收获**：`clamp`/`Math.max` 用作“兜底”时常在掩盖真实错误；合规系统应“响亮失败”。

### 案例 B：`NaN` 坐标绕过校验，被“洗白”为合规 `OK`

- Prompt:
  > are there any other "failing silently" logic in code?
- **问题**：`requireValidCoordinates` 用 `lat < -90 || lat > 90` 判断，`NaN` 与任何数比较都为 `false`，于是通过校验；Haversine 得到 `NaN` 距离，`NaN > 500.0` 也为 `false` → 不标记 `FAR_FROM_HOSPITAL` → 访视被持久化为 `OK`。`±Infinity` 反而会被范围比较拦住，唯独 `NaN` 漏网。
- **发现方式**：由 Prompt #8 的“静默失败专项审计”系统性挖出（同一类“坏输入被转换成看似合理结果”）。
- **纠偏动作**：`VisitService` 与 `Haversine` 两处都改为 `!Double.isFinite(...) || …`；补 `rejectsNaN/InfiniteCoordinates` 等测试。
- **结果**：非法/NaN 输入 → `IllegalArgumentException` → 400。
- **收获**：浮点比较的边界陷阱；“把坏输入 laundered 成合理结果”是最危险的一类 bug。

### 案例 C：合规阈值硬编码 → 外置为配置，并补“启动期校验”

- Prompt:
  > let's not hardcode this two compliance check values here, move them to separate properties file
  >
  > 附带代码片段：`public static final long MIN_DURATION_SECONDS = 5 * 60;` / `public static final double MAX_HOSPITAL_DISTANCE_METERS = 500.0;`
- **问题**：`MIN_DURATION_SECONDS` / `MAX_HOSPITAL_DISTANCE_METERS` 硬编码在 `ComplianceEvaluator`；且外置后**缺少范围校验**——`min-duration-seconds: -1` 会让 `SHORT_DURATION` 永不触发（静默失效），负距离会让所有访视 `ABNORMAL`。
- **发现方式**：用户先提出“不要硬编码，移到 properties”（Prompt #5）；随后在静默失败审计中进一步指出“无范围校验”的隐患（Prompt #8）。
- **纠偏动作**：新增 `ComplianceProperties` 记录类（`@DefaultValue`）+ `@Validated`/`@PositiveOrZero`/`@DecimalMin`；`ComplianceEvaluator` 改为注入；新增“自定义阈值改变行为”“负值启动失败”的测试。
- **结果**：错误配置 → 启动即失败；阈值可经环境变量/ConfigMap 覆盖。
- **收获**：区分“可配置”与“无约束可配置”；受监管场景要“配置错了就起不来”，而非静默失效。

### 案例 D：时区 `systemDefault()`（容器默认 UTC）→ 显式 UTC+8，并修复同源隐藏点

- Prompt:
  > this is UTC, make it work with utc+8 timezone to display correctly on dashboard
  >
  > 定位：`DashboardService.java:27` 的 `systemDefault`
- **问题**：`DashboardService` 用 `ZoneId.systemDefault()` 做月份分桶；宿主是 `+08:00` 但 `eclipse-temurin` 容器默认 **UTC**，导致容器里月份边界与时间显示整体偏移 8 小时（例：`2026-06-30T16:30Z` = `2026-07-01 00:30 +08`，会被分到 6 月）。
- **发现方式**：用户在 `DashboardService.java:27` 直接标出 `systemDefault`（Prompt #13）。
- **纠偏动作**：新增 `AppTimeZone` 暴露 `ZoneId` bean（`visit.time-zone` 默认 `Asia/Shanghai`），`DashboardService`/`VisitWebController`/`DisplayFormats` 统一注入；随后又发现**同类隐藏点** `DashboardController` 的 `YearMonth.now()`（默认月份仍用 JVM 默认区），改为 `YearMonth.now(zone)`。
- **结果**：以 `TZ=UTC` 运行验证——边界访视正确落入 7 月、列表显示 `2026-07-01 00:30`；测试全绿。
- **收获**：时区要“单一来源 + 显式”；一处改完要 grep 同类调用（`systemDefault`）避免遗漏。

### 案例 E：`datetime-local` 在 Safari/Chrome 需手动输入时分 → 日期 + 时分选择器

- Prompt:
  > this is not very easy to use on safari and chrome, the hour/minutes need to be input manually, make it a real time picker with selectable hour/minute
  >
  > 附带代码片段（`new.html:45-47`）：`<input type="datetime-local" th:field="*{plannedAtLocal}">`
- **问题**：单个 `<input type="datetime-local">` 在 Safari 上小时/分钟要手敲，Chrome 的分段控件也难用。
- **发现方式**：用户直接贴出 `new.html` 第 45–47 行并反馈“hour/minutes need to be input manually”（Prompt #15）。
- **纠偏动作**：拆成 `date` + 小时 `00–23` / 分钟 `00–59` 两个 `<select>`；`PlanVisitForm` 改为 `plannedDate/plannedHour/plannedMinute` 并加 `@NotBlank/@NotNull/@Min/@Max`；控制器在配置时区里 `LocalDate.atTime(h,m).atZone(zone)` 组合成 `Instant`；表单默认填充“下一个半点”。
- **结果**：跨浏览器均可点选；实测渲染默认值/预选值正确、提交后按 UTC+8 正确回显；测试全绿。
- **收获**：原生控件的跨浏览器行为要按目标用户浏览器验证；把“输入难”转为“可选控件 + 服务端组合”。

### 补充案例 F：“信任但验证”——AI 声称的产物要以代码为准

- Prompt:
  > I can only see Haversine and its test for now, continue
  >
  > I didn't see an application-docker.yml, only application.yml
- **问题**：AI 曾宣称已创建 `application-docker.yml`，但用户“只看到 `application.yml`”（Prompt #12）；Phase 3 中 AI 也一度只交付了 Haversine + 测试（Prompt #4）。
- **发现方式**：用户不轻信描述，直接核对文件/进度。
- **纠偏动作**：AI 用 `git ls-files`/`git status` 证明文件确已提交于 `b9fde7c`（此处为真实核对）；对“输出不完整”则继续补齐。
- **收获**：对 AI 的“完成/已创建”声明保持怀疑，用 `git`/实际文件状态核验——这正是把 AI 纳入工程流程的关键纪律。

### 案例 G：看板“上一月/下一月”链接输出 `DECEMBER`（`Month` 枚举）→ 点击 400

- Prompt:
  > I got "2026-11-12T16:34:03.123+08:00  WARN 96993 --- [visit-subsystem] [nio-8080-exec-1] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Method parameter 'month': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; For input string: "DECEMBER"]" when clicking on previous/next month
- **问题**：看板“上一月/下一月”链接用 `${prev.month}`/`${next.month}`，实际调用 `YearMonth.getMonth()` → 返回 `Month` 枚举（如 `DECEMBER`），而非数字 `1–12`；控制器 `@RequestParam Integer month` 无法把 `DECEMBER` 转成 `Integer`，点击即抛 `MethodArgumentTypeMismatchException`（400）。因页面仍按默认月份渲染，**看似正常、实际点击报错**。
- **发现方式**：用户实际点击上一月/下一月并贴出 WARN 日志（既有测试未覆盖导航链接）。
- **纠偏动作**：模板改为 `${prev.monthValue}`/`${next.monthValue}`（`getMonthValue()` → `int`）；新增回归测试 `dashboardMonthNavigationLinksUseNumericMonth`（断言 `month=12`/`month=2`，且不含 `DECEMBER`/`FEBRUARY`）。
- **结果**：61 测试通过；链接渲染为 `?year=2025&month=12`、`?year=2026&month=2`，点击返回 200（原为 400）。
- **收获**：`YearMonth` 有两个“月”访问器——`getMonth()`（枚举）/ `getMonthValue()`（数字）——模板里极易踩坑；“能渲染”≠“功能可用”，导航/跳转类交互需专门的 UI 断言。

### 正面亮点（非纠偏，可作为“良好实践”注记）

- **Phase 3 先计算真实参考距离**再写断言（如 Paris–London ≈ 343,556.5 m），让测试断言**可独立验证的真实值**，而非“自洽但可能错”的数字。
- **“静默失败”专项审计**（Prompt #8）作为一种可复用的 review 手法，一次系统性清出 NaN、吞异常、无校验配置、`Math.max` 等多处隐患。

## 3. 全程 AI 会话导出

完整的人机协作会话已保存为 `docs/conversation.txt`，覆盖从需求规划、Phase 1–6 实现，到代码评审与多轮纠偏的全过程（含关键往返原文），可与第 1 节的 Prompt、第 2 节的案例逐一对照。

> 脱敏提醒：提交前请确认该导出文件中不含 SSH 私钥、访问 token、内网 IP、代理账号等敏感信息。

## 4. Git History

已导出到 `docs/`（可直接随报告提交）：

- `docs/git-history-graph.txt` — 带阶段 tag 的可读历史
- `docs/git-history-stat.txt` — 逐提交 + 改动统计（时间正序）
- `docs/git-history-files.txt` — 每个提交改动的文件清单

## 5. 反思与改进

- **AI 最需要人工把关的地方**是“边界语义”和“失败模式”：`Math.max` 掩盖、`NaN` 绕过、`systemDefault` 时区、`datetime-local` 跨浏览器——都属于“看似能跑、实则静默错误”。后续应把“静默失败审计”固化为每个 Phase 的收尾清单。
- **协作纪律**：“分阶段 + 停下评审 + 显式权衡 + 不臆造版本”显著降低了返工；对 AI 的“已完成/已创建”声明坚持用 `git`/文件状态核验。
- **可继续加强**：`checkOut` 对 `checkInAt == null` 的 NPE（评审 C1）尚未处理；`submitReport` 的 check-then-act 建议在 MVC 侧也映射 409；如需合规审计可加 `@Version` 乐观锁。
- **UI/导航交互是测试盲区**：月份切换链接的 bug 未被测试覆盖，靠人工点击才暴露；今后对“生成链接/跳转”的页面补断言（本次已加 `dashboardMonthNavigationLinksUseNumericMonth`）。
