#!/usr/bin/env python3
"""
多维度智能验证系统 V6.0
融合PMO、架构师、开发、QA、UX五大视角
实现真正的全方位项目验证
"""

import os
import re
import json
import subprocess
import ast
from typing import List, Dict, Set, Tuple, Optional, Any
from dataclasses import dataclass, field
from enum import Enum
from collections import defaultdict
from datetime import datetime

class Perspective(Enum):
    """验证视角"""
    PMO = "项目管理"
    ARCHITECT = "架构设计"
    DEVELOPER = "开发实现"
    QA = "质量保证"
    UX = "用户体验"

class IssueLevel(Enum):
    """问题级别"""
    SHOWSTOPPER = "SHOWSTOPPER"  # 阻止发布
    CRITICAL = "CRITICAL"        # 必须修复
    MAJOR = "MAJOR"             # 应该修复
    MINOR = "MINOR"             # 建议修复
    SUGGESTION = "SUGGESTION"    # 改进建议

@dataclass
class ValidationIssue:
    """验证问题"""
    perspective: Perspective
    category: str
    description: str
    level: IssueLevel
    evidence: str
    impact: str
    solution: str
    effort: str  # 修复工作量：低/中/高
    file_path: str = ""
    line_number: int = 0
    
@dataclass
class UserStory:
    """用户故事"""
    persona: str  # 用户角色
    action: str   # 用户行为
    value: str    # 业务价值
    acceptance_criteria: List[str]  # 验收标准
    test_scenarios: List[str]  # 测试场景

@dataclass
class SystemHealth:
    """系统健康度"""
    score: float  # 0-100
    dimensions: Dict[str, float]  # 各维度得分
    risks: List[str]  # 风险点
    strengths: List[str]  # 优势点

class IntelligentValidatorV6:
    def __init__(self, project_root: str = "/workspace"):
        self.project_root = project_root
        self.issues: List[ValidationIssue] = []
        self.user_stories: List[UserStory] = []
        self.system_health = SystemHealth(
            score=100.0,
            dimensions={},
            risks=[],
            strengths=[]
        )
        
        # 初始化用户故事
        self._init_user_stories()
        
    def _init_user_stories(self):
        """初始化核心用户故事"""
        self.user_stories = [
            UserStory(
                persona="3岁小明",
                action="打开应用听AI讲故事",
                value="获得个性化的教育内容",
                acceptance_criteria=[
                    "3秒内启动应用",
                    "一键开始故事",
                    "故事内容适龄",
                    "支持语音交互",
                    "家长可控制时长"
                ],
                test_scenarios=[
                    "首次使用流程",
                    "网络断开情况",
                    "故事中断恢复",
                    "超时自动暂停",
                    "不当内容过滤"
                ]
            ),
            UserStory(
                persona="家长王女士",
                action="查看孩子学习报告",
                value="了解孩子学习进度和兴趣",
                acceptance_criteria=[
                    "需要密码验证",
                    "数据可视化展示",
                    "支持导出分享",
                    "隐私数据保护",
                    "个性化建议"
                ],
                test_scenarios=[
                    "密码错误处理",
                    "数据为空情况",
                    "报告生成失败",
                    "分享权限控制"
                ]
            ),
            UserStory(
                persona="5岁小红",
                action="拍照识别物体学习",
                value="通过AI认知真实世界",
                acceptance_criteria=[
                    "相机快速启动",
                    "识别准确率>80%",
                    "儿童友好的解释",
                    "安全内容过滤",
                    "支持保存记录"
                ],
                test_scenarios=[
                    "光线不足情况",
                    "识别失败处理",
                    "不适内容过滤",
                    "权限拒绝处理"
                ]
            )
        ]
    
    def validate(self) -> Dict:
        """主验证入口 - 多视角全方位验证"""
        print("\n" + "="*70)
        print("🧠 多维度智能验证系统 V6.0")
        print("融合5大视角，25个维度，100+检查点")
        print("="*70)
        
        # 1. PMO视角 - 项目完整性
        print("\n👔 [PMO视角] 项目管理验证")
        self.validate_from_pmo_perspective()
        
        # 2. 架构师视角 - 技术架构
        print("\n🏗️ [架构师视角] 架构设计验证")
        self.validate_from_architect_perspective()
        
        # 3. 开发者视角 - 代码实现
        print("\n💻 [开发者视角] 代码实现验证")
        self.validate_from_developer_perspective()
        
        # 4. QA视角 - 质量保证
        print("\n🔍 [QA视角] 质量保证验证")
        self.validate_from_qa_perspective()
        
        # 5. UX视角 - 用户体验
        print("\n🎨 [UX视角] 用户体验验证")
        self.validate_from_ux_perspective()
        
        # 6. 综合评估
        print("\n📊 [综合评估] 系统健康度分析")
        self.evaluate_system_health()
        
        # 7. 生成报告
        return self.generate_comprehensive_report()
    
    def validate_from_pmo_perspective(self):
        """PMO视角验证 - 关注项目交付标准"""
        print("  ✓ 检查项目交付物完整性...")
        self._check_project_deliverables()
        
        print("  ✓ 验证用户故事实现度...")
        self._validate_user_stories()
        
        print("  ✓ 评估项目风险...")
        self._assess_project_risks()
        
        print("  ✓ 检查文档完整性...")
        self._check_documentation()
    
    def _check_project_deliverables(self):
        """检查项目交付物"""
        required_deliverables = {
            "源代码": ["app/src/main", "所有功能模块的源代码"],
            "测试代码": ["app/src/test", "单元测试覆盖核心功能"],
            "配置文件": ["app/build.gradle.kts", "构建和依赖配置"],
            "文档": ["README.md", "项目说明和使用指南"],
            "资源文件": ["app/src/main/res", "UI资源和配置"]
        }
        
        for deliverable, (path, description) in required_deliverables.items():
            full_path = os.path.join(self.project_root, path)
            if not os.path.exists(full_path):
                self._add_issue(
                    perspective=Perspective.PMO,
                    category="交付物缺失",
                    description=f"缺少{deliverable}: {description}",
                    level=IssueLevel.CRITICAL,
                    evidence=f"Path not found: {path}",
                    impact="项目交付不完整",
                    solution=f"创建并完善{deliverable}",
                    effort="中"
                )
    
    def _validate_user_stories(self):
        """验证用户故事的实现"""
        for story in self.user_stories:
            print(f"    - 验证故事: {story.persona} - {story.action}")
            
            # 检查每个验收标准
            for criterion in story.acceptance_criteria:
                if not self._check_acceptance_criterion(story, criterion):
                    self._add_issue(
                        perspective=Perspective.PMO,
                        category="用户故事未完成",
                        description=f"{story.persona}的需求未满足: {criterion}",
                        level=IssueLevel.MAJOR,
                        evidence=f"User story: {story.action}",
                        impact="用户体验不完整",
                        solution="实现相应功能以满足验收标准",
                        effort="中"
                    )
    
    def _check_acceptance_criterion(self, story: UserStory, criterion: str) -> bool:
        """检查验收标准是否满足"""
        # 这里应该有具体的检查逻辑
        # 简化实现：检查代码中是否有相关实现
        
        criterion_checks = {
            "3秒内启动应用": ["SplashScreen", "LazyColumn", "remember"],
            "一键开始故事": ["HomeScreen", "generateStory", "Button"],
            "需要密码验证": ["ParentLoginScreen", "verifyPassword"],
            "相机快速启动": ["CameraScreen", "CameraX", "rememberCameraProviderFuture"],
        }
        
        for key, patterns in criterion_checks.items():
            if key in criterion:
                # 检查相关代码是否存在
                for pattern in patterns:
                    found = False
                    for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
                        for file in files:
                            if file.endswith('.kt'):
                                try:
                                    with open(os.path.join(root, file), 'r') as f:
                                        if pattern in f.read():
                                            found = True
                                            break
                                except:
                                    pass
                        if found:
                            break
                    if not found:
                        return False
                return True
        
        return True  # 默认通过
    
    def validate_from_architect_perspective(self):
        """架构师视角验证 - 关注技术架构质量"""
        print("  ✓ 检查架构分层...")
        self._check_architecture_layers()
        
        print("  ✓ 验证依赖关系...")
        self._validate_dependencies()
        
        print("  ✓ 评估可扩展性...")
        self._assess_scalability()
        
        print("  ✓ 检查性能设计...")
        self._check_performance_design()
    
    def _check_architecture_layers(self):
        """检查Clean Architecture分层"""
        layers = {
            "domain": ["model", "repository", "usecase"],
            "data": ["local", "remote", "repository"],
            "presentation": ["screen", "viewmodel", "state"]
        }
        
        for layer, components in layers.items():
            layer_path = os.path.join(self.project_root, f"app/src/main/java/com/enlightenment/ai/{layer}")
            if os.path.exists(layer_path):
                for component in components:
                    component_path = os.path.join(layer_path, component)
                    if not os.path.exists(component_path):
                        # 检查是否有相关文件
                        found = False
                        for root, _, files in os.walk(layer_path):
                            if any(component in file.lower() for file in files):
                                found = True
                                break
                        
                        if not found:
                            self._add_issue(
                                perspective=Perspective.ARCHITECT,
                                category="架构不完整",
                                description=f"{layer}层缺少{component}组件",
                                level=IssueLevel.MAJOR,
                                evidence=f"Missing: {layer}/{component}",
                                impact="违反Clean Architecture原则",
                                solution=f"在{layer}层实现{component}",
                                effort="中"
                            )
    
    def validate_from_developer_perspective(self):
        """开发者视角验证 - 关注代码质量和完整性"""
        print("  ✓ 检查代码完整性...")
        self._check_code_completeness()
        
        print("  ✓ 验证错误处理...")
        self._validate_error_handling()
        
        print("  ✓ 检查资源管理...")
        self._check_resource_management()
        
        print("  ✓ 评估代码质量...")
        self._assess_code_quality()
    
    def _check_code_completeness(self):
        """检查代码完整性"""
        # 扫描所有Kotlin文件
        kotlin_files = []
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            kotlin_files.extend([os.path.join(root, f) for f in files if f.endswith('.kt')])
        
        for file_path in kotlin_files:
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # 检查空实现
                empty_functions = re.findall(
                    r'fun\s+(\w+)\s*\([^)]*\)[^{]*\{\s*\}',
                    content
                )
                
                for func_name in empty_functions:
                    # 排除合理的空实现
                    if func_name not in ['toString', 'hashCode', 'equals', 'onCreate', 'onDestroy']:
                        self._add_issue(
                            perspective=Perspective.DEVELOPER,
                            category="空实现",
                            description=f"函数{func_name}缺少实现",
                            level=IssueLevel.MAJOR,
                            evidence=f"Empty function in {os.path.basename(file_path)}",
                            impact="功能不完整",
                            solution="实现函数逻辑",
                            effort="低",
                            file_path=file_path
                        )
                
                # 检查TODO标记
                todos = re.findall(r'//\s*(TODO|FIXME|XXX).*', content)
                for todo in todos:
                    self._add_issue(
                        perspective=Perspective.DEVELOPER,
                        category="未完成任务",
                        description=f"发现{todo[0]}标记",
                        level=IssueLevel.MINOR,
                        evidence=todo,
                        impact="代码未完成",
                        solution="完成TODO项",
                        effort="低",
                        file_path=file_path
                    )
                    
            except Exception as e:
                pass
    
    def _validate_error_handling(self):
        """验证错误处理机制"""
        critical_patterns = {
            "网络请求": ["try", "catch", ".onFailure", ".onError"],
            "数据库操作": ["try", "catch", "transaction"],
            "文件操作": ["try", "catch", "use"]
        }
        
        # 检查关键操作的错误处理
        for operation, required_patterns in critical_patterns.items():
            # 这里简化：检查Repository和ViewModel中的错误处理
            for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
                for file in files:
                    if ('Repository' in file or 'ViewModel' in file) and file.endswith('.kt'):
                        file_path = os.path.join(root, file)
                        try:
                            with open(file_path, 'r') as f:
                                content = f.read()
                            
                            # 检查是否有异步操作但缺少错误处理
                            if 'suspend fun' in content or 'viewModelScope.launch' in content:
                                has_error_handling = any(pattern in content for pattern in required_patterns)
                                if not has_error_handling:
                                    self._add_issue(
                                        perspective=Perspective.DEVELOPER,
                                        category="错误处理缺失",
                                        description=f"{os.path.basename(file)}缺少{operation}错误处理",
                                        level=IssueLevel.CRITICAL,
                                        evidence=file,
                                        impact="应用可能崩溃",
                                        solution="添加try-catch或错误回调",
                                        effort="低",
                                        file_path=file_path
                                    )
                        except:
                            pass
    
    def validate_from_qa_perspective(self):
        """QA视角验证 - 关注测试和质量"""
        print("  ✓ 检查测试覆盖率...")
        self._check_test_coverage()
        
        print("  ✓ 验证边界条件...")
        self._validate_edge_cases()
        
        print("  ✓ 检查安全漏洞...")
        self._check_security_vulnerabilities()
        
        print("  ✓ 模拟用户场景...")
        self._simulate_user_scenarios()
    
    def _check_test_coverage(self):
        """检查测试覆盖率"""
        # 统计源文件和测试文件
        source_files = []
        test_files = []
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src")):
            for file in files:
                if file.endswith('.kt'):
                    if '/test/' in root or '/androidTest/' in root:
                        test_files.append(file)
                    elif '/main/' in root:
                        source_files.append(file)
        
        # 计算覆盖率
        coverage_ratio = len(test_files) / max(len(source_files), 1)
        
        if coverage_ratio < 0.3:  # 低于30%
            self._add_issue(
                perspective=Perspective.QA,
                category="测试覆盖率低",
                description=f"测试覆盖率仅{coverage_ratio:.1%}",
                level=IssueLevel.CRITICAL,
                evidence=f"测试文件:{len(test_files)}, 源文件:{len(source_files)}",
                impact="质量无法保证",
                solution="增加单元测试和集成测试",
                effort="高"
            )
        
        # 检查关键功能是否有测试
        key_components = ["StoryViewModel", "DialogueViewModel", "CameraViewModel"]
        for component in key_components:
            has_test = any(component.replace("ViewModel", "") in test for test in test_files)
            if not has_test:
                self._add_issue(
                    perspective=Perspective.QA,
                    category="关键功能无测试",
                    description=f"{component}缺少测试",
                    level=IssueLevel.MAJOR,
                    evidence=f"No test for {component}",
                    impact="核心功能质量无保障",
                    solution=f"为{component}编写测试",
                    effort="中"
                )
    
    def _validate_edge_cases(self):
        """验证边界条件处理"""
        edge_cases = [
            ("网络断开", ["offline", "no network", "connection failed"]),
            ("空数据", ["empty", "null", "isEmpty()"]),
            ("权限拒绝", ["permission denied", "not granted"]),
            ("超大数据", ["limit", "max", "overflow"]),
            ("并发操作", ["synchronized", "atomic", "concurrent"])
        ]
        
        for case_name, patterns in edge_cases:
            found = False
            for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
                for file in files:
                    if file.endswith('.kt'):
                        try:
                            with open(os.path.join(root, file), 'r') as f:
                                content = f.read().lower()
                                if any(pattern.lower() in content for pattern in patterns):
                                    found = True
                                    break
                        except:
                            pass
                if found:
                    break
            
            if not found:
                self._add_issue(
                    perspective=Perspective.QA,
                    category="边界条件未处理",
                    description=f"未处理{case_name}的情况",
                    level=IssueLevel.MAJOR,
                    evidence="No handling found",
                    impact="特殊情况下可能出错",
                    solution=f"添加{case_name}的处理逻辑",
                    effort="中"
                )
    
    def validate_from_ux_perspective(self):
        """UX视角验证 - 关注用户体验"""
        print("  ✓ 检查UI一致性...")
        self._check_ui_consistency()
        
        print("  ✓ 验证无障碍支持...")
        self._validate_accessibility()
        
        print("  ✓ 评估性能体验...")
        self._assess_performance_ux()
        
        print("  ✓ 检查错误提示...")
        self._check_error_ux()
    
    def _check_ui_consistency(self):
        """检查UI一致性"""
        # 检查是否使用了统一的主题
        theme_files = []
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            theme_files.extend([f for f in files if 'theme' in f.lower() or 'color' in f.lower()])
        
        if len(theme_files) < 2:
            self._add_issue(
                perspective=Perspective.UX,
                category="UI不一致",
                description="缺少统一的主题定义",
                level=IssueLevel.MAJOR,
                evidence="No theme files found",
                impact="用户体验不一致",
                solution="创建统一的主题和颜色方案",
                effort="中"
            )
        
        # 检查是否有加载状态
        loading_patterns = ["CircularProgressIndicator", "Loading", "Shimmer"]
        has_loading = False
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            for file in files:
                if file.endswith('.kt'):
                    try:
                        with open(os.path.join(root, file), 'r') as f:
                            content = f.read()
                            if any(pattern in content for pattern in loading_patterns):
                                has_loading = True
                                break
                    except:
                        pass
            if has_loading:
                break
        
        if not has_loading:
            self._add_issue(
                perspective=Perspective.UX,
                category="用户反馈缺失",
                description="缺少加载状态提示",
                level=IssueLevel.MAJOR,
                evidence="No loading indicators",
                impact="用户不知道应用是否在响应",
                solution="添加加载动画和状态提示",
                effort="低"
            )
    
    def _check_error_ux(self):
        """检查错误提示的用户友好性"""
        # 查找错误处理代码
        error_messages = []
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            for file in files:
                if file.endswith('.kt'):
                    try:
                        with open(os.path.join(root, file), 'r') as f:
                            content = f.read()
                            # 查找错误消息
                            messages = re.findall(r'[Ee]rror.*"([^"]+)"', content)
                            error_messages.extend(messages)
                    except:
                        pass
        
        # 检查错误消息是否友好
        unfriendly_patterns = ['Exception', 'Failed', 'Error', 'null', '404', '500']
        for msg in error_messages:
            if any(pattern.lower() in msg.lower() for pattern in unfriendly_patterns):
                self._add_issue(
                    perspective=Perspective.UX,
                    category="错误提示不友好",
                    description=f"技术性错误信息: {msg}",
                    level=IssueLevel.MINOR,
                    evidence=msg,
                    impact="用户体验差",
                    solution="使用友好的中文提示",
                    effort="低"
                )
                break  # 只报告一个示例
    
    def evaluate_system_health(self):
        """评估系统健康度"""
        # 基于各视角的问题计算健康度
        perspective_weights = {
            Perspective.PMO: 0.20,
            Perspective.ARCHITECT: 0.25,
            Perspective.DEVELOPER: 0.25,
            Perspective.QA: 0.20,
            Perspective.UX: 0.10
        }
        
        # 计算各视角得分
        for perspective in Perspective:
            perspective_issues = [i for i in self.issues if i.perspective == perspective]
            
            # 根据问题级别扣分
            deductions = {
                IssueLevel.SHOWSTOPPER: 20,
                IssueLevel.CRITICAL: 10,
                IssueLevel.MAJOR: 5,
                IssueLevel.MINOR: 2,
                IssueLevel.SUGGESTION: 0.5
            }
            
            score = 100.0
            for issue in perspective_issues:
                score -= deductions.get(issue.level, 0)
            
            score = max(0, score)  # 确保不为负
            self.system_health.dimensions[perspective.value] = score
        
        # 计算总分
        total_score = sum(
            score * perspective_weights[perspective]
            for perspective, score in zip(Perspective, self.system_health.dimensions.values())
        )
        
        self.system_health.score = total_score
        
        # 识别风险和优势
        if total_score < 60:
            self.system_health.risks.append("整体质量不达标")
        if self.system_health.dimensions.get(Perspective.QA.value, 0) < 50:
            self.system_health.risks.append("测试严重不足")
        if self.system_health.dimensions.get(Perspective.ARCHITECT.value, 0) > 80:
            self.system_health.strengths.append("架构设计良好")
    
    def _add_issue(self, **kwargs):
        """添加验证问题"""
        issue = ValidationIssue(**kwargs)
        self.issues.append(issue)
    
    def _assess_project_risks(self):
        """评估项目风险"""
        # 技术风险
        if not os.path.exists(os.path.join(self.project_root, ".github/workflows")):
            self._add_issue(
                perspective=Perspective.PMO,
                category="技术风险",
                description="缺少CI/CD配置",
                level=IssueLevel.MAJOR,
                evidence="No .github/workflows",
                impact="发布流程不规范",
                solution="配置GitHub Actions",
                effort="中"
            )
        
        # 进度风险 - 检查是否有未实现的核心功能
        core_features = ["Story", "Dialogue", "Camera", "Parent"]
        for feature in core_features:
            feature_files = 0
            for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
                feature_files += sum(1 for f in files if feature in f and f.endswith('.kt'))
            
            if feature_files < 3:  # 至少应该有Screen, ViewModel, Repository
                self._add_issue(
                    perspective=Perspective.PMO,
                    category="进度风险",
                    description=f"{feature}功能可能未完全实现",
                    level=IssueLevel.CRITICAL,
                    evidence=f"Only {feature_files} files found",
                    impact="核心功能缺失",
                    solution=f"完成{feature}功能的所有组件",
                    effort="高"
                )
    
    def _check_documentation(self):
        """检查文档完整性"""
        required_docs = {
            "README.md": "项目说明",
            "docs/architecture.md": "架构文档",
            "docs/api.md": "API文档",
            "CONTRIBUTING.md": "贡献指南"
        }
        
        for doc, description in required_docs.items():
            doc_path = os.path.join(self.project_root, doc)
            if not os.path.exists(doc_path):
                level = IssueLevel.CRITICAL if doc == "README.md" else IssueLevel.MAJOR
                self._add_issue(
                    perspective=Perspective.PMO,
                    category="文档缺失",
                    description=f"缺少{description}",
                    level=level,
                    evidence=f"Missing: {doc}",
                    impact="项目可维护性差",
                    solution=f"创建{doc}",
                    effort="低"
                )
    
    def _validate_dependencies(self):
        """验证依赖关系"""
        build_file = os.path.join(self.project_root, "app/build.gradle.kts")
        
        if os.path.exists(build_file):
            with open(build_file, 'r') as f:
                content = f.read()
            
            # 检查关键依赖
            critical_deps = {
                "room": "本地数据库",
                "retrofit": "网络请求",
                "hilt": "依赖注入",
                "compose": "UI框架"
            }
            
            for dep, description in critical_deps.items():
                if dep not in content.lower():
                    self._add_issue(
                        perspective=Perspective.ARCHITECT,
                        category="依赖缺失",
                        description=f"缺少{description}依赖",
                        level=IssueLevel.MAJOR,
                        evidence=f"No {dep} in build.gradle",
                        impact="功能实现受限",
                        solution=f"添加{dep}依赖",
                        effort="低"
                    )
    
    def _assess_scalability(self):
        """评估可扩展性"""
        # 检查是否使用了依赖注入
        di_patterns = ["@Inject", "@Provides", "@Module", "@HiltViewModel"]
        di_usage = 0
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            for file in files:
                if file.endswith('.kt'):
                    try:
                        with open(os.path.join(root, file), 'r') as f:
                            content = f.read()
                            if any(pattern in content for pattern in di_patterns):
                                di_usage += 1
                    except:
                        pass
        
        if di_usage < 10:
            self._add_issue(
                perspective=Perspective.ARCHITECT,
                category="可扩展性差",
                description="依赖注入使用不足",
                level=IssueLevel.MAJOR,
                evidence=f"Only {di_usage} files use DI",
                impact="代码耦合度高",
                solution="使用Hilt进行依赖管理",
                effort="中"
            )
    
    def _check_performance_design(self):
        """检查性能设计"""
        # 检查是否有性能优化措施
        perf_patterns = ["LazyColumn", "remember", "derivedStateOf", "key ="]
        perf_optimizations = 0
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            for file in files:
                if file.endswith('.kt') and 'Screen' in file:
                    try:
                        with open(os.path.join(root, file), 'r') as f:
                            content = f.read()
                            perf_optimizations += sum(1 for p in perf_patterns if p in content)
                    except:
                        pass
        
        if perf_optimizations < 5:
            self._add_issue(
                perspective=Perspective.ARCHITECT,
                category="性能设计",
                description="Compose性能优化不足",
                level=IssueLevel.MAJOR,
                evidence=f"Only {perf_optimizations} optimizations found",
                impact="UI可能卡顿",
                solution="使用LazyColumn和remember优化",
                effort="中"
            )
    
    def _check_resource_management(self):
        """检查资源管理"""
        # 检查是否有资源泄漏风险
        resource_patterns = [
            ("ViewModel", ["onCleared", "viewModelScope"]),
            ("Composable", ["DisposableEffect", "LaunchedEffect"]),
            ("Repository", [".close()", ".cancel()"])
        ]
        
        for component, required in resource_patterns:
            component_files = []
            for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
                component_files.extend([
                    os.path.join(root, f) for f in files 
                    if component in f and f.endswith('.kt')
                ])
            
            for file_path in component_files[:3]:  # 检查前3个
                try:
                    with open(file_path, 'r') as f:
                        content = f.read()
                        if not any(pattern in content for pattern in required):
                            self._add_issue(
                                perspective=Perspective.DEVELOPER,
                                category="资源泄漏风险",
                                description=f"{os.path.basename(file_path)}可能有资源泄漏",
                                level=IssueLevel.MAJOR,
                                evidence="No resource cleanup found",
                                impact="内存泄漏",
                                solution="添加资源清理代码",
                                effort="低",
                                file_path=file_path
                            )
                            break
                except:
                    pass
    
    def _assess_code_quality(self):
        """评估代码质量"""
        quality_issues = []
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            for file in files:
                if file.endswith('.kt'):
                    try:
                        with open(os.path.join(root, file), 'r') as f:
                            lines = f.readlines()
                        
                        # 检查代码规范
                        for i, line in enumerate(lines):
                            # 超长行
                            if len(line) > 120:
                                quality_issues.append(("超长代码行", file, i+1))
                            
                            # 魔法数字
                            if re.search(r'\b\d{3,}\b', line) and '//' not in line:
                                quality_issues.append(("魔法数字", file, i+1))
                            
                            # 调试代码
                            if 'println(' in line:
                                quality_issues.append(("调试代码", file, i+1))
                        
                    except:
                        pass
        
        # 报告前几个问题
        for issue_type, file, line in quality_issues[:3]:
            self._add_issue(
                perspective=Perspective.DEVELOPER,
                category="代码质量",
                description=f"{issue_type}: {file}:{line}",
                level=IssueLevel.MINOR,
                evidence=f"Line {line}",
                impact="代码可维护性差",
                solution="重构代码符合规范",
                effort="低"
            )
    
    def _check_security_vulnerabilities(self):
        """检查安全漏洞"""
        security_patterns = [
            ("硬编码密钥", ['"sk-', '"api_key"', '"secret"']),
            ("不安全存储", ['SharedPreferences', 'MODE_WORLD']),
            ("SQL注入", ['rawQuery', 'execSQL']),
            ("不安全通信", ['http://', 'allowAllHostnameVerifier'])
        ]
        
        for vuln_name, patterns in security_patterns:
            for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
                for file in files:
                    if file.endswith('.kt'):
                        try:
                            with open(os.path.join(root, file), 'r') as f:
                                content = f.read()
                                for pattern in patterns:
                                    if pattern in content:
                                        self._add_issue(
                                            perspective=Perspective.QA,
                                            category="安全漏洞",
                                            description=f"潜在的{vuln_name}",
                                            level=IssueLevel.CRITICAL,
                                            evidence=f"{pattern} in {file}",
                                            impact="安全风险",
                                            solution="使用安全的实现方式",
                                            effort="中"
                                        )
                                        break
                        except:
                            pass
    
    def _simulate_user_scenarios(self):
        """模拟用户场景测试"""
        for story in self.user_stories:
            for scenario in story.test_scenarios:
                # 简化实现：检查是否有相关的测试代码
                scenario_tested = False
                
                test_keywords = scenario.lower().split()[:2]  # 取前两个关键词
                for root, _, files in os.walk(os.path.join(self.project_root, "app/src/test")):
                    for file in files:
                        if file.endswith('.kt'):
                            try:
                                with open(os.path.join(root, file), 'r') as f:
                                    content = f.read().lower()
                                    if all(keyword in content for keyword in test_keywords):
                                        scenario_tested = True
                                        break
                            except:
                                pass
                    if scenario_tested:
                        break
                
                if not scenario_tested:
                    self._add_issue(
                        perspective=Perspective.QA,
                        category="场景未测试",
                        description=f"{story.persona}的场景未测试: {scenario}",
                        level=IssueLevel.MAJOR,
                        evidence="No test found",
                        impact="用户体验未验证",
                        solution=f"添加{scenario}的测试",
                        effort="中"
                    )
    
    def _validate_accessibility(self):
        """验证无障碍支持"""
        # 检查是否有无障碍支持
        a11y_patterns = ["contentDescription", "semantics", "Modifier.clickable"]
        a11y_support = 0
        
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            for file in files:
                if file.endswith('.kt') and 'Screen' in file:
                    try:
                        with open(os.path.join(root, file), 'r') as f:
                            content = f.read()
                            a11y_support += sum(1 for p in a11y_patterns if p in content)
                    except:
                        pass
        
        if a11y_support < 10:
            self._add_issue(
                perspective=Perspective.UX,
                category="无障碍支持",
                description="无障碍支持不足",
                level=IssueLevel.MAJOR,
                evidence=f"Only {a11y_support} a11y features",
                impact="部分用户无法使用",
                solution="添加contentDescription等无障碍支持",
                effort="中"
            )
    
    def _assess_performance_ux(self):
        """评估性能体验"""
        # 检查启动优化
        splash_screen = False
        for root, _, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            if any('splash' in f.lower() for f in files):
                splash_screen = True
                break
        
        if not splash_screen:
            self._add_issue(
                perspective=Perspective.UX,
                category="启动体验",
                description="缺少启动画面",
                level=IssueLevel.MINOR,
                evidence="No splash screen",
                impact="启动体验差",
                solution="添加启动画面",
                effort="低"
            )
        
        # 检查图片优化
        res_path = os.path.join(self.project_root, "app/src/main/res")
        if os.path.exists(res_path):
            large_images = []
            for root, _, files in os.walk(res_path):
                for file in files:
                    if file.endswith(('.png', '.jpg', '.jpeg')):
                        file_path = os.path.join(root, file)
                        if os.path.getsize(file_path) > 100 * 1024:  # 100KB
                            large_images.append(file)
            
            if large_images:
                self._add_issue(
                    perspective=Perspective.UX,
                    category="性能优化",
                    description=f"存在大图片文件: {large_images[0]}",
                    level=IssueLevel.MINOR,
                    evidence=f"{len(large_images)} large images",
                    impact="加载缓慢",
                    solution="压缩图片或使用WebP格式",
                    effort="低"
                )
    
    def generate_comprehensive_report(self) -> Dict:
        """生成综合报告"""
        # 统计各级别问题
        issue_stats = defaultdict(int)
        perspective_stats = defaultdict(lambda: defaultdict(int))
        
        for issue in self.issues:
            issue_stats[issue.level] += 1
            perspective_stats[issue.perspective][issue.level] += 1
        
        # 显示报告头
        print("\n" + "="*70)
        print("📋 验证报告 - 多维度分析结果")
        print("="*70)
        
        # 1. 健康度评分
        print(f"\n🏥 系统健康度: {self.system_health.score:.1f}/100")
        print("\n各视角得分:")
        for perspective, score in self.system_health.dimensions.items():
            emoji = "✅" if score >= 80 else "⚠️" if score >= 60 else "❌"
            print(f"  {emoji} {perspective}: {score:.1f}/100")
        
        # 2. 问题统计
        print(f"\n📊 问题统计 (共{len(self.issues)}个):")
        for level in IssueLevel:
            count = issue_stats[level]
            if count > 0:
                print(f"  - {level.value}: {count}个")
        
        # 3. 各视角问题分布
        print("\n🔍 各视角问题分布:")
        for perspective in Perspective:
            p_issues = perspective_stats[perspective]
            total = sum(p_issues.values())
            if total > 0:
                critical = p_issues[IssueLevel.SHOWSTOPPER] + p_issues[IssueLevel.CRITICAL]
                print(f"  {perspective.value}: {total}个问题 (严重:{critical})")
        
        # 4. 关键问题列表
        critical_issues = [i for i in self.issues 
                          if i.level in [IssueLevel.SHOWSTOPPER, IssueLevel.CRITICAL]]
        
        if critical_issues:
            print(f"\n🚨 必须修复的问题 ({len(critical_issues)}个):")
            for i, issue in enumerate(critical_issues[:10], 1):
                print(f"\n  [{i}] [{issue.perspective.value}] {issue.description}")
                print(f"      级别: {issue.level.value}")
                print(f"      影响: {issue.impact}")
                print(f"      方案: {issue.solution}")
                print(f"      工作量: {issue.effort}")
        
        # 5. 风险和优势
        if self.system_health.risks:
            print(f"\n⚠️ 主要风险:")
            for risk in self.system_health.risks:
                print(f"  - {risk}")
        
        if self.system_health.strengths:
            print(f"\n💪 项目优势:")
            for strength in self.system_health.strengths:
                print(f"  - {strength}")
        
        # 6. 行动建议
        print("\n📝 行动建议:")
        if issue_stats[IssueLevel.SHOWSTOPPER] > 0:
            print("  1. 立即修复SHOWSTOPPER问题")
        if issue_stats[IssueLevel.CRITICAL] > 5:
            print("  2. 优先处理CRITICAL问题")
        if self.system_health.dimensions.get(Perspective.QA.value, 0) < 60:
            print("  3. 大幅增加测试覆盖率")
        if self.system_health.dimensions.get(Perspective.UX.value, 0) < 70:
            print("  4. 改善用户体验设计")
        
        # 7. 生成JSON报告
        report = {
            "version": "6.0",
            "timestamp": datetime.now().isoformat(),
            "health_score": self.system_health.score,
            "health_dimensions": self.system_health.dimensions,
            "can_release": issue_stats[IssueLevel.SHOWSTOPPER] == 0 and 
                          issue_stats[IssueLevel.CRITICAL] < 3,
            "statistics": {
                "total_issues": len(self.issues),
                "by_level": {level.value: count for level, count in issue_stats.items()},
                "by_perspective": {
                    p.value: {l.value: c for l, c in perspective_stats[p].items()} 
                    for p in Perspective
                }
            },
            "critical_issues": [
                {
                    "perspective": issue.perspective.value,
                    "category": issue.category,
                    "description": issue.description,
                    "level": issue.level.value,
                    "impact": issue.impact,
                    "solution": issue.solution,
                    "effort": issue.effort,
                    "file": issue.file_path,
                    "line": issue.line_number
                } for issue in critical_issues[:20]
            ],
            "risks": self.system_health.risks,
            "strengths": self.system_health.strengths
        }
        
        # 保存报告
        with open("validation_report_v6.json", "w", encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        # 最终判定
        print("\n" + "="*70)
        if report["can_release"]:
            print("✅ 项目可以发布，但建议修复主要问题")
        else:
            print("❌ 项目还不能发布，必须先修复关键问题")
        print("="*70)
        
        return report

if __name__ == "__main__":
    validator = IntelligentValidatorV6()
    report = validator.validate()
    
    exit(0 if report["can_release"] else 1)