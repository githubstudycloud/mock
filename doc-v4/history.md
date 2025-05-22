# 开发历程与版本演进

## V1-V2：基础能力与API雏形
- 实现基础接口/类mock，支持when/thenReturn/thenThrow。
- 采用JDK Proxy/CGLIB，行为注册与查找分离。
- 主要问题：静态/构造器/私有方法mock能力有限，JDK21兼容性不足。

## V3：高级mock与架构重构
- 引入双引擎（Instrumentation+Objenesis），支持构造器、静态、私有方法mock。
- Spring/DI集成，自动注入mock。
- 行为注册、调用验证、mock工厂等核心API重构。
- 主要遗留：Javassist方案下构造器mock无法100%替换new对象，需Instrumentation完善。
- 详细设计与开发历程见doc-history/（v3-*.md、framework-design.md等）。

## V4：正式发布与文档规范
- 文档结构升级，开发历程、架构、API、用法分离，便于外部集成和二次开发。
- 兼容JDK8-21+，推荐JDK17/21。
- 重点完善文档、用例和集成指引。

---

> 历史详细文档请见 doc-history/ 目录。 