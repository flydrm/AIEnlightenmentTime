#!/usr/bin/env python3
"""
智能验证系统 V4.0 - 深度语义分析 + 行为验证
彻底解决"假完成"问题
"""

import os
import re
import ast
import json
import subprocess
from typing import List, Dict, Set, Tuple, Optional
from dataclasses import dataclass, field
from enum import Enum
import networkx as nx

class IssueLevel(Enum):
    BLOCKER = "BLOCKER"      # 阻塞发布
    CRITICAL = "CRITICAL"    # 必须修复
    MAJOR = "MAJOR"          # 重要问题
    MINOR = "MINOR"          # 次要问题

@dataclass
class CodeIssue:
    file_path: str
    line_number: int
    issue_type: str
    description: str
    level: IssueLevel
    evidence: str
    suggestion: str
    confidence: float = 1.0

@dataclass
class FunctionAnalysis:
    name: str
    file_path: str
    has_implementation: bool
    calls_api: bool
    handles_errors: bool
    updates_ui: bool
    saves_data: bool
    has_return: bool
    dependencies: List[str] = field(default_factory=list)

class IntelligentValidator:
    def __init__(self, project_root: str = "/workspace"):
        self.project_root = project_root
        self.issues: List[CodeIssue] = []
        self.functions: Dict[str, FunctionAnalysis] = {}
        self.dependency_graph = nx.DiGraph()
        
    def validate(self) -> Dict:
        """主验证入口 - 多维度深度分析"""
        print("\n" + "="*60)
        print("🧠 智能验证系统 V4.0 - 深度语义分析")
        print("="*60 + "\n")
        
        # Phase 1: 代码分析
        print("📊 Phase 1: 代码结构分析")
        self.analyze_code_structure()
        
        # Phase 2: 语义分析
        print("\n🔍 Phase 2: 语义完整性分析")
        self.analyze_semantic_completeness()
        
        # Phase 3: 依赖分析
        print("\n🔗 Phase 3: 依赖链分析")
        self.analyze_dependencies()
        
        # Phase 4: 行为验证
        print("\n🎯 Phase 4: 行为验证")
        self.verify_runtime_behavior()
        
        # Phase 5: 业务逻辑验证
        print("\n💼 Phase 5: 业务逻辑验证")
        self.verify_business_logic()
        
        # Phase 6: 集成测试
        print("\n🔧 Phase 6: 集成完整性测试")
        self.verify_integration()
        
        return self.generate_report()
    
    def analyze_code_structure(self):
        """分析代码结构，建立函数图谱"""
        kotlin_files = self.find_kotlin_files()
        
        for file_path in kotlin_files:
            self.parse_kotlin_file(file_path)
    
    def parse_kotlin_file(self, file_path: str):
        """解析Kotlin文件，提取函数信息"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 提取所有函数
            function_pattern = r'(suspend\s+)?fun\s+(\w+)\s*\([^)]*\)(\s*:\s*[^{]+)?\s*\{([^}]*)\}'
            functions = re.finditer(function_pattern, content, re.DOTALL)
            
            for match in functions:
                is_suspend = match.group(1) is not None
                func_name = match.group(2)
                return_type = match.group(3)
                body = match.group(4)
                
                # 分析函数体
                analysis = FunctionAnalysis(
                    name=func_name,
                    file_path=file_path,
                    has_implementation=len(body.strip()) > 0,
                    calls_api='api' in body.lower() or 'service' in body.lower(),
                    handles_errors='try' in body or 'catch' in body or '.onFailure' in body,
                    updates_ui='_uiState' in body or '_state' in body or 'mutableStateOf' in body,
                    saves_data='save' in body or 'insert' in body or 'update' in body or 'dataStore' in body,
                    has_return='return' in body or (return_type and return_type.strip() != ': Unit'),
                    dependencies=self.extract_dependencies(body)
                )
                
                self.functions[f"{file_path}::{func_name}"] = analysis
                
                # 检查空实现
                if not analysis.has_implementation:
                    self.add_issue(
                        file_path=file_path,
                        line_number=0,
                        issue_type="empty_implementation",
                        description=f"函数 {func_name} 没有实现",
                        level=IssueLevel.CRITICAL,
                        evidence=match.group(0)[:100],
                        suggestion="添加实际的实现逻辑"
                    )
                
        except Exception as e:
            print(f"⚠️ 解析文件失败 {file_path}: {e}")
    
    def analyze_semantic_completeness(self):
        """语义完整性分析 - 检查功能是否真正完整"""
        
        # 1. 检查ViewModel的完整性
        self.check_viewmodel_completeness()
        
        # 2. 检查Repository的完整性
        self.check_repository_completeness()
        
        # 3. 检查导航的完整性
        self.check_navigation_completeness()
        
        # 4. 检查API调用的完整性
        self.check_api_completeness()
    
    def check_viewmodel_completeness(self):
        """检查ViewModel是否完整实现"""
        viewmodels = [f for f in self.functions.items() if 'ViewModel' in f[0]]
        
        for path_func, analysis in viewmodels:
            file_path = analysis.file_path
            
            # 检查是否有对应的UI状态更新
            if analysis.calls_api and not analysis.updates_ui:
                self.add_issue(
                    file_path=file_path,
                    line_number=0,
                    issue_type="incomplete_ui_update",
                    description=f"{analysis.name} 调用了API但没有更新UI状态",
                    level=IssueLevel.CRITICAL,
                    evidence=f"Function: {analysis.name}",
                    suggestion="添加 _uiState.update 或 _state.value = ..."
                )
            
            # 检查错误处理
            if analysis.calls_api and not analysis.handles_errors:
                self.add_issue(
                    file_path=file_path,
                    line_number=0,
                    issue_type="missing_error_handling",
                    description=f"{analysis.name} 缺少错误处理",
                    level=IssueLevel.MAJOR,
                    evidence=f"Function: {analysis.name}",
                    suggestion="添加 try-catch 或 .onFailure 处理"
                )
    
    def check_repository_completeness(self):
        """检查Repository实现的完整性"""
        repos = [f for f in self.functions.items() if 'Repository' in f[0]]
        
        for path_func, analysis in repos:
            # 检查数据持久化
            if 'save' in analysis.name.lower() or 'update' in analysis.name.lower():
                if not analysis.saves_data:
                    self.add_issue(
                        file_path=analysis.file_path,
                        line_number=0,
                        issue_type="missing_persistence",
                        description=f"{analysis.name} 声称保存数据但没有实际保存",
                        level=IssueLevel.CRITICAL,
                        evidence=f"Function: {analysis.name}",
                        suggestion="添加数据库或DataStore操作"
                    )
    
    def check_navigation_completeness(self):
        """检查导航目标是否都存在"""
        nav_pattern = r'navigate\s*\(\s*["\']([^"\']+)["\']'
        screen_pattern = r'composable\s*\(\s*["\']([^"\']+)["\']'
        
        navigations = set()
        screens = set()
        
        for file_path in self.find_kotlin_files():
            with open(file_path, 'r') as f:
                content = f.read()
                
                # 收集导航调用
                nav_matches = re.findall(nav_pattern, content)
                navigations.update(nav_matches)
                
                # 收集定义的screen
                screen_matches = re.findall(screen_pattern, content)
                screens.update(screen_matches)
        
        # 检查缺失的screen
        missing_screens = navigations - screens
        for missing in missing_screens:
            self.add_issue(
                file_path="Navigation",
                line_number=0,
                issue_type="missing_screen",
                description=f"导航目标 '{missing}' 没有对应的Screen实现",
                level=IssueLevel.BLOCKER,
                evidence=f"navigate('{missing}')",
                suggestion=f"在NavHost中添加 composable('{missing}') {{ ... }}"
            )
    
    def check_api_completeness(self):
        """检查API调用的完整性"""
        # 检查所有声称调用API的地方是否真的有实现
        api_functions = [f for f in self.functions.items() if f[1].calls_api]
        
        for path_func, analysis in api_functions:
            # 读取函数体
            with open(analysis.file_path, 'r') as f:
                content = f.read()
            
            # 检查是否有实际的网络调用
            if 'suspend' in content and analysis.name in content:
                func_body = self.extract_function_body(content, analysis.name)
                
                # 检查常见的假实现模式
                fake_patterns = [
                    r'return\s+"[^"]+"\s*$',  # 直接返回字符串
                    r'return\s+\d+',          # 直接返回数字
                    r'return\s+true|false',   # 直接返回布尔值
                    r'delay\s*\(\s*\d+\s*\)', # 只有延迟
                    r'return\s+.*Mock',       # 返回Mock数据
                    r'return\s+.*fake',       # 返回fake数据
                    r'//\s*TODO',             # TODO注释
                    r'//.*implement',         # 未实现注释
                ]
                
                for pattern in fake_patterns:
                    if re.search(pattern, func_body, re.IGNORECASE):
                        self.add_issue(
                            file_path=analysis.file_path,
                            line_number=0,
                            issue_type="fake_implementation",
                            description=f"{analysis.name} 可能是假实现",
                            level=IssueLevel.CRITICAL,
                            evidence=pattern,
                            suggestion="实现真实的API调用逻辑",
                            confidence=0.8
                        )
                        break
    
    def analyze_dependencies(self):
        """分析依赖关系，确保链条完整"""
        # 构建依赖图
        for func_key, analysis in self.functions.items():
            self.dependency_graph.add_node(func_key)
            
            for dep in analysis.dependencies:
                dep_key = self.find_function_key(dep)
                if dep_key:
                    self.dependency_graph.add_edge(func_key, dep_key)
        
        # 检查断链
        for node in self.dependency_graph.nodes():
            if self.dependency_graph.out_degree(node) > 0:
                for neighbor in self.dependency_graph.neighbors(node):
                    if neighbor not in self.functions:
                        self.add_issue(
                            file_path=self.functions[node].file_path,
                            line_number=0,
                            issue_type="broken_dependency",
                            description=f"依赖的功能 {neighbor} 不存在",
                            level=IssueLevel.MAJOR,
                            evidence=f"{node} -> {neighbor}",
                            suggestion="实现缺失的依赖功能"
                        )
    
    def verify_runtime_behavior(self):
        """验证运行时行为"""
        print("  - 检查异步操作...")
        self.check_async_operations()
        
        print("  - 检查状态管理...")
        self.check_state_management()
        
        print("  - 检查生命周期...")
        self.check_lifecycle_handling()
    
    def check_async_operations(self):
        """检查异步操作的正确性"""
        suspend_functions = [f for f in self.functions.items() if 'suspend' in f[0]]
        
        for path_func, analysis in suspend_functions:
            # 检查是否在正确的scope中调用
            with open(analysis.file_path, 'r') as f:
                content = f.read()
            
            if 'viewModelScope' not in content and 'lifecycleScope' not in content:
                if 'ViewModel' in analysis.file_path:
                    self.add_issue(
                        file_path=analysis.file_path,
                        line_number=0,
                        issue_type="missing_coroutine_scope",
                        description=f"异步函数 {analysis.name} 可能没有在正确的scope中调用",
                        level=IssueLevel.MAJOR,
                        evidence=f"Function: {analysis.name}",
                        suggestion="使用 viewModelScope.launch { ... }"
                    )
    
    def verify_business_logic(self):
        """验证业务逻辑的完整性"""
        print("  - 验证用户故事...")
        self.verify_user_stories()
        
        print("  - 验证数据流...")
        self.verify_data_flow()
    
    def verify_user_stories(self):
        """验证关键用户故事是否可以完成"""
        user_stories = [
            {
                "name": "生成故事",
                "required_chain": ["GenerateStoryUseCase", "StoryRepository", "StoryViewModel", "StoryScreen"],
                "required_features": ["api_call", "error_handling", "ui_update", "data_save"]
            },
            {
                "name": "家长登录",
                "required_chain": ["ParentLoginViewModel", "ParentLoginScreen", "ParentDashboard"],
                "required_features": ["validation", "navigation", "state_management"]
            },
            {
                "name": "拍照识别",
                "required_chain": ["CameraScreen", "RecognizeImageUseCase", "ImageRecognitionRepository"],
                "required_features": ["camera_permission", "image_capture", "api_call", "result_display"]
            }
        ]
        
        for story in user_stories:
            print(f"    验证: {story['name']}")
            missing_components = []
            
            for component in story["required_chain"]:
                found = False
                for func_key in self.functions:
                    if component in func_key:
                        found = True
                        break
                
                if not found:
                    missing_components.append(component)
            
            if missing_components:
                self.add_issue(
                    file_path="User Story",
                    line_number=0,
                    issue_type="incomplete_user_story",
                    description=f"用户故事 '{story['name']}' 缺少组件: {missing_components}",
                    level=IssueLevel.BLOCKER,
                    evidence=str(missing_components),
                    suggestion="实现缺失的组件以完成用户故事"
                )
    
    def verify_integration(self):
        """验证集成的完整性"""
        print("  - 验证依赖注入...")
        self.verify_dependency_injection()
        
        print("  - 验证API配置...")
        self.verify_api_configuration()
        
        print("  - 验证权限配置...")
        self.verify_permissions()
    
    def verify_dependency_injection(self):
        """验证依赖注入的完整性"""
        # 查找所有@Inject
        inject_pattern = r'@Inject\s+(?:constructor|lateinit\s+var)\s+(\w+)'
        provides_pattern = r'@Provides.*fun\s+provide(\w+)'
        
        required_injections = set()
        provided_dependencies = set()
        
        for file_path in self.find_kotlin_files():
            with open(file_path, 'r') as f:
                content = f.read()
                
                # 收集需要注入的
                injects = re.findall(inject_pattern, content)
                required_injections.update(injects)
                
                # 收集提供的
                provides = re.findall(provides_pattern, content)
                provided_dependencies.update(provides)
        
        # 检查缺失的依赖
        # 这里简化处理，实际应该更智能
        missing = required_injections - provided_dependencies
        if missing:
            self.add_issue(
                file_path="Dependency Injection",
                line_number=0,
                issue_type="missing_dependency_provider",
                description=f"缺少依赖提供者: {missing}",
                level=IssueLevel.MAJOR,
                evidence=str(missing),
                suggestion="在DI模块中添加@Provides方法"
            )
    
    def extract_dependencies(self, body: str) -> List[str]:
        """从函数体中提取依赖"""
        # 简化版本，提取函数调用
        pattern = r'(\w+)\s*\('
        matches = re.findall(pattern, body)
        return [m for m in matches if m not in ['if', 'when', 'for', 'while', 'return']]
    
    def extract_function_body(self, content: str, func_name: str) -> str:
        """提取函数体"""
        pattern = rf'fun\s+{func_name}\s*\([^)]*\)[^{{]*\{{(.*?)\n\s*\}}'
        match = re.search(pattern, content, re.DOTALL)
        return match.group(1) if match else ""
    
    def find_function_key(self, func_name: str) -> Optional[str]:
        """查找函数的完整key"""
        for key in self.functions:
            if func_name in key:
                return key
        return None
    
    def find_kotlin_files(self) -> List[str]:
        """查找所有Kotlin文件"""
        kotlin_files = []
        for root, dirs, files in os.walk(os.path.join(self.project_root, "app/src/main")):
            dirs[:] = [d for d in dirs if d not in ['.git', 'build', '.gradle']]
            for file in files:
                if file.endswith('.kt'):
                    kotlin_files.append(os.path.join(root, file))
        return kotlin_files
    
    def add_issue(self, **kwargs):
        """添加问题"""
        issue = CodeIssue(**kwargs)
        self.issues.append(issue)
    
    def generate_report(self) -> Dict:
        """生成详细报告"""
        # 按级别分类
        blockers = [i for i in self.issues if i.level == IssueLevel.BLOCKER]
        criticals = [i for i in self.issues if i.level == IssueLevel.CRITICAL]
        majors = [i for i in self.issues if i.level == IssueLevel.MAJOR]
        minors = [i for i in self.issues if i.level == IssueLevel.MINOR]
        
        print("\n" + "="*60)
        print("📊 验证报告")
        print("="*60)
        
        if blockers:
            print(f"\n🚫 阻塞问题: {len(blockers)} 个")
            for issue in blockers[:3]:
                print(f"  - {issue.description}")
                print(f"    文件: {issue.file_path}")
                print(f"    建议: {issue.suggestion}")
        
        if criticals:
            print(f"\n❌ 严重问题: {len(criticals)} 个")
            for issue in criticals[:3]:
                print(f"  - {issue.description}")
                print(f"    证据: {issue.evidence[:60]}...")
                print(f"    建议: {issue.suggestion}")
        
        if majors:
            print(f"\n⚠️ 重要问题: {len(majors)} 个")
            for issue in majors[:2]:
                print(f"  - {issue.description}")
        
        # 统计
        total_issues = len(self.issues)
        must_fix = len(blockers) + len(criticals)
        
        print(f"\n📈 统计:")
        print(f"  - 扫描函数: {len(self.functions)} 个")
        print(f"  - 发现问题: {total_issues} 个")
        print(f"  - 必须修复: {must_fix} 个")
        
        # 生成JSON报告
        report = {
            "success": must_fix == 0,
            "total_functions": len(self.functions),
            "total_issues": total_issues,
            "blockers": len(blockers),
            "criticals": len(criticals),
            "majors": len(majors),
            "minors": len(minors),
            "must_fix": must_fix,
            "issues": [
                {
                    "file": i.file_path,
                    "type": i.issue_type,
                    "description": i.description,
                    "level": i.level.value,
                    "suggestion": i.suggestion,
                    "confidence": i.confidence
                } for i in self.issues[:20]  # 只保存前20个
            ]
        }
        
        with open("intelligent_validation_report.json", "w") as f:
            json.dump(report, f, indent=2)
        
        if must_fix == 0:
            print("\n✅ 验证通过！项目已达到发布标准。")
        else:
            print(f"\n❌ 发现 {must_fix} 个必须修复的问题。")
        
        return report

if __name__ == "__main__":
    validator = IntelligentValidator()
    report = validator.validate()
    
    exit(0 if report["success"] else 1)