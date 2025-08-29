#!/usr/bin/env python3
"""
多维度智能验证系统 V5.0
- 改进的Kotlin语法解析
- 更准确的语义分析
- 减少误报，提高准确性
"""

import os
import re
import json
import subprocess
from typing import List, Dict, Set, Tuple, Optional, Any
from dataclasses import dataclass, field
from enum import Enum
from collections import defaultdict

class IssueLevel(Enum):
    BLOCKER = "BLOCKER"      # 必须修复才能上线
    CRITICAL = "CRITICAL"    # 严重问题
    MAJOR = "MAJOR"          # 重要问题
    MINOR = "MINOR"          # 次要问题
    INFO = "INFO"            # 信息提示

@dataclass
class CodeContext:
    """代码上下文信息"""
    file_path: str
    is_interface: bool = False
    is_abstract: bool = False
    is_data_class: bool = False
    is_test_file: bool = False
    class_name: str = ""
    package_name: str = ""

@dataclass
class FunctionInfo:
    """函数详细信息"""
    name: str
    file_path: str
    line_number: int
    signature: str
    body: str
    is_abstract: bool = False
    is_override: bool = False
    is_suspend: bool = False
    is_single_expression: bool = False
    is_extension: bool = False
    has_implementation: bool = True
    context: Optional[CodeContext] = None

@dataclass
class ValidationIssue:
    """验证问题"""
    file_path: str
    line_number: int
    issue_type: str
    description: str
    level: IssueLevel
    evidence: str
    suggestion: str
    confidence: float = 1.0
    false_positive: bool = False

class IntelligentValidatorV5:
    def __init__(self, project_root: str = "/workspace"):
        self.project_root = project_root
        self.issues: List[ValidationIssue] = []
        self.functions: Dict[str, FunctionInfo] = {}
        self.contexts: Dict[str, CodeContext] = {}
        self.kotlin_keywords = {
            'fun', 'val', 'var', 'class', 'interface', 'object', 'companion',
            'abstract', 'override', 'suspend', 'inline', 'data', 'sealed'
        }
        
    def validate(self) -> Dict:
        """主验证入口"""
        print("\n" + "="*70)
        print("🧠 多维度智能验证系统 V5.0")
        print("="*70)
        
        # Phase 1: 建立代码上下文
        print("\n📊 Phase 1: 建立代码上下文")
        self.build_code_context()
        
        # Phase 2: 智能代码分析
        print("\n🔍 Phase 2: 智能代码分析")
        self.analyze_code_intelligently()
        
        # Phase 3: 语义验证
        print("\n💡 Phase 3: 语义验证")
        self.semantic_validation()
        
        # Phase 4: 业务逻辑验证
        print("\n📈 Phase 4: 业务逻辑验证")
        self.business_logic_validation()
        
        # Phase 5: 集成完整性
        print("\n🔗 Phase 5: 集成完整性验证")
        self.integration_validation()
        
        # Phase 6: 误报过滤
        print("\n🎯 Phase 6: 误报过滤")
        self.filter_false_positives()
        
        return self.generate_comprehensive_report()
    
    def build_code_context(self):
        """建立代码上下文，理解每个文件的性质"""
        kotlin_files = self.find_kotlin_files()
        
        for file_path in kotlin_files:
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                context = CodeContext(file_path=file_path)
                
                # 分析文件类型
                context.is_interface = 'interface ' in content
                context.is_abstract = 'abstract class' in content
                context.is_data_class = 'data class' in content
                context.is_test_file = '/test/' in file_path or 'Test' in file_path
                
                # 提取包名和类名
                package_match = re.search(r'package\s+([\w.]+)', content)
                if package_match:
                    context.package_name = package_match.group(1)
                
                class_match = re.search(r'(?:class|interface|object)\s+(\w+)', content)
                if class_match:
                    context.class_name = class_match.group(1)
                
                self.contexts[file_path] = context
                
            except Exception as e:
                print(f"  ⚠️ 无法分析文件 {file_path}: {e}")
    
    def analyze_code_intelligently(self):
        """智能分析代码，避免误判"""
        for file_path, context in self.contexts.items():
            self.analyze_kotlin_file(file_path, context)
    
    def analyze_kotlin_file(self, file_path: str, context: CodeContext):
        """分析Kotlin文件"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 使用改进的解析器
            functions = self.parse_kotlin_functions(content, context)
            
            for func in functions:
                key = f"{file_path}::{func.name}"
                self.functions[key] = func
                
                # 只对需要实现的函数进行检查
                if self.should_check_implementation(func):
                    self.check_function_implementation(func)
                    
        except Exception as e:
            print(f"  ⚠️ 解析文件失败 {file_path}: {e}")
    
    def parse_kotlin_functions(self, content: str, context: CodeContext) -> List[FunctionInfo]:
        """改进的Kotlin函数解析器"""
        functions = []
        lines = content.split('\n')
        
        i = 0
        while i < len(lines):
            line = lines[i]
            
            # 匹配函数定义（改进的正则）
            func_start = re.match(
                r'^\s*((?:override\s+)?(?:suspend\s+)?(?:inline\s+)?'
                r'(?:private\s+)?(?:public\s+)?(?:internal\s+)?'
                r'(?:protected\s+)?)'
                r'fun\s+'
                r'(?:<[^>]+>\s+)?'  # 泛型
                r'(?:(\w+)\.)?'     # 接收者类型
                r'(\w+)'            # 函数名
                r'\s*\(',           # 开始参数
                line
            )
            
            if func_start:
                func_name = func_start.group(3)
                is_extension = bool(func_start.group(2))
                modifiers = func_start.group(1)
                
                # 收集完整的函数签名
                signature_lines = [line]
                paren_count = line.count('(') - line.count(')')
                j = i + 1
                
                # 处理多行参数
                while j < len(lines) and paren_count > 0:
                    signature_lines.append(lines[j])
                    paren_count += lines[j].count('(') - lines[j].count(')')
                    j += 1
                
                signature = '\n'.join(signature_lines)
                
                # 判断函数体类型
                body_start_line = j
                is_single_expression = '=' in signature and '{' not in signature
                
                # 获取函数体
                body = ""
                if is_single_expression:
                    # 单行表达式函数
                    body = signature.split('=', 1)[1].strip()
                    # 去掉可能的尾部注释
                    body = re.sub(r'//.*$', '', body).strip()
                else:
                    # 多行函数体
                    if '{' in signature or (j < len(lines) and '{' in lines[j]):
                        brace_count = signature.count('{') - signature.count('}')
                        if brace_count == 0 and j < len(lines):
                            brace_count = lines[j].count('{') - lines[j].count('}')
                            body_start_line = j
                        
                        body_lines = []
                        k = body_start_line
                        while k < len(lines) and (brace_count > 0 or k == body_start_line):
                            body_lines.append(lines[k])
                            brace_count += lines[k].count('{') - lines[k].count('}')
                            k += 1
                        
                        body = '\n'.join(body_lines)
                
                # 创建函数信息
                func_info = FunctionInfo(
                    name=func_name,
                    file_path=file_path,
                    line_number=i + 1,
                    signature=signature,
                    body=body,
                    is_abstract='abstract' in modifiers or context.is_interface,
                    is_override='override' in modifiers,
                    is_suspend='suspend' in modifiers,
                    is_single_expression=is_single_expression,
                    is_extension=is_extension,
                    has_implementation=self.has_real_implementation(body, is_single_expression),
                    context=context
                )
                
                functions.append(func_info)
                i = j if j > i else i + 1
            else:
                i += 1
        
        return functions
    
    def has_real_implementation(self, body: str, is_single_expression: bool) -> bool:
        """判断函数是否有真实实现"""
        if not body:
            return False
        
        # 清理body
        clean_body = self.clean_code_body(body)
        
        if is_single_expression:
            # 单行表达式函数，检查是否有实际的表达式
            return len(clean_body) > 0 and clean_body not in ['{}', '{ }', '']
        else:
            # 多行函数，检查是否只有空括号或注释
            if clean_body in ['{}', '{ }', '']:
                return False
            
            # 检查是否只有TODO或类似标记
            if re.match(r'^\s*\{\s*(//.*TODO|//.*FIXME|/\*.*\*/)\s*\}\s*$', body, re.DOTALL):
                return False
            
            return True
    
    def clean_code_body(self, body: str) -> str:
        """清理代码体，去除注释和空白"""
        # 去除单行注释
        body = re.sub(r'//.*$', '', body, flags=re.MULTILINE)
        # 去除多行注释
        body = re.sub(r'/\*.*?\*/', '', body, flags=re.DOTALL)
        # 去除多余空白
        body = body.strip()
        return body
    
    def should_check_implementation(self, func: FunctionInfo) -> bool:
        """判断是否需要检查函数实现"""
        # 不检查抽象函数
        if func.is_abstract:
            return False
        
        # 不检查接口中的函数（除非有默认实现）
        if func.context and func.context.is_interface and not func.body:
            return False
        
        # 不检查测试文件中的某些模式
        if func.context and func.context.is_test_file:
            if func.name in ['setUp', 'tearDown', 'before', 'after']:
                return False
        
        # 不检查数据类的自动生成函数
        if func.context and func.context.is_data_class:
            if func.name in ['equals', 'hashCode', 'toString', 'copy']:
                return False
        
        return True
    
    def check_function_implementation(self, func: FunctionInfo):
        """检查函数实现的完整性"""
        # 1. 检查空实现
        if not func.has_implementation:
            self.add_issue(
                file_path=func.file_path,
                line_number=func.line_number,
                issue_type="empty_implementation",
                description=f"函数 '{func.name}' 缺少实现",
                level=IssueLevel.CRITICAL,
                evidence=func.signature,
                suggestion="添加函数实现逻辑"
            )
            return
        
        # 2. 检查可疑的实现
        suspicious_patterns = [
            (r'^\s*throw\s+NotImplementedError', "NotImplementedError", IssueLevel.CRITICAL),
            (r'^\s*TODO\s*\(\s*\)', "TODO() placeholder", IssueLevel.CRITICAL),
            (r'//\s*TODO(?![\w])', "TODO comment", IssueLevel.MAJOR),
            (r'//\s*FIXME(?![\w])', "FIXME comment", IssueLevel.MAJOR),
            (r'^\s*return\s+"[^"]*"\s*$', "hardcoded string return", IssueLevel.MINOR),
            (r'^\s*return\s+\d+\s*$', "hardcoded number return", IssueLevel.MINOR),
        ]
        
        clean_body = self.clean_code_body(func.body)
        
        for pattern, issue_name, level in suspicious_patterns:
            if re.search(pattern, clean_body, re.MULTILINE):
                # 特殊情况处理
                if issue_name == "hardcoded string return" and func.name.startswith("get"):
                    # getter返回常量是正常的
                    continue
                if issue_name == "hardcoded number return" and func.name in ['hashCode', 'compareTo']:
                    # 某些函数返回数字是正常的
                    continue
                
                self.add_issue(
                    file_path=func.file_path,
                    line_number=func.line_number,
                    issue_type="suspicious_implementation",
                    description=f"函数 '{func.name}' 包含 {issue_name}",
                    level=level,
                    evidence=pattern,
                    suggestion="实现真实的业务逻辑",
                    confidence=0.8
                )
    
    def semantic_validation(self):
        """语义验证 - 检查代码的逻辑完整性"""
        print("  检查ViewModel完整性...")
        self.check_viewmodel_completeness()
        
        print("  检查Repository实现...")
        self.check_repository_implementation()
        
        print("  检查导航完整性...")
        self.check_navigation_completeness()
    
    def check_viewmodel_completeness(self):
        """检查ViewModel的完整性"""
        viewmodels = {k: v for k, v in self.functions.items() 
                     if 'ViewModel' in k and not v.context.is_test_file}
        
        for key, func in viewmodels.items():
            # 检查异步函数的错误处理
            if func.is_suspend or 'viewModelScope.launch' in func.body:
                if not any(pattern in func.body for pattern in ['try', 'catch', '.onFailure', '.onError']):
                    self.add_issue(
                        file_path=func.file_path,
                        line_number=func.line_number,
                        issue_type="missing_error_handling",
                        description=f"异步函数 '{func.name}' 缺少错误处理",
                        level=IssueLevel.MAJOR,
                        evidence="No error handling found",
                        suggestion="添加 try-catch 或 .onFailure 处理"
                    )
            
            # 检查状态更新
            if any(action in func.name.lower() for action in ['load', 'fetch', 'save', 'update', 'delete']):
                if not any(state in func.body for state in ['_state', '_uiState', 'mutableStateOf']):
                    self.add_issue(
                        file_path=func.file_path,
                        line_number=func.line_number,
                        issue_type="missing_state_update",
                        description=f"函数 '{func.name}' 可能缺少状态更新",
                        level=IssueLevel.MAJOR,
                        evidence="No state update found",
                        suggestion="更新 UI 状态以反映操作结果",
                        confidence=0.7
                    )
    
    def check_repository_implementation(self):
        """检查Repository实现的完整性"""
        repos = {k: v for k, v in self.functions.items() 
                if 'Repository' in k and 'Impl' in k and not v.context.is_test_file}
        
        for key, func in repos.items():
            # 检查数据操作是否有持久化
            if any(op in func.name.lower() for op in ['save', 'insert', 'update', 'store']):
                if not any(storage in func.body for storage in ['dao', 'dataStore', 'database', 'sharedPreferences']):
                    self.add_issue(
                        file_path=func.file_path,
                        line_number=func.line_number,
                        issue_type="missing_persistence",
                        description=f"数据操作 '{func.name}' 可能缺少持久化",
                        level=IssueLevel.CRITICAL,
                        evidence="No persistence layer found",
                        suggestion="使用 DAO 或 DataStore 持久化数据"
                    )
    
    def check_navigation_completeness(self):
        """检查导航的完整性"""
        # 收集所有导航调用
        nav_calls = set()
        # 收集所有Screen定义
        screen_defs = set()
        
        for func in self.functions.values():
            if 'navigate' in func.body:
                # 提取导航目标
                nav_matches = re.findall(r'navigate\s*\(\s*["\']([\w/]+)["\']', func.body)
                nav_calls.update(nav_matches)
            
            if '@Composable' in func.signature and 'Screen' in func.name:
                screen_defs.add(func.name)
        
        # 检查NavHost中的路由定义
        nav_host_files = [f for f in self.contexts.keys() if 'NavHost' in f]
        composable_routes = set()
        
        for nav_file in nav_host_files:
            try:
                with open(nav_file, 'r') as f:
                    content = f.read()
                    route_matches = re.findall(r'composable\s*\(\s*["\']([\w/]+)["\']', content)
                    composable_routes.update(route_matches)
            except:
                pass
        
        # 找出缺失的路由
        missing_routes = nav_calls - composable_routes
        for route in missing_routes:
            self.add_issue(
                file_path="Navigation",
                line_number=0,
                issue_type="missing_route",
                description=f"导航目标 '{route}' 没有在 NavHost 中定义",
                level=IssueLevel.BLOCKER,
                evidence=f"navigate('{route}')",
                suggestion=f"在 NavHost 中添加 composable('{route}') {{ }}"
            )
    
    def business_logic_validation(self):
        """业务逻辑验证"""
        print("  验证核心用户流程...")
        self.validate_user_stories()
        
        print("  验证数据流完整性...")
        self.validate_data_flow()
    
    def validate_user_stories(self):
        """验证用户故事的完整性"""
        # 定义核心用户故事及其所需组件
        user_stories = [
            {
                "name": "AI故事生成",
                "required_components": {
                    "use_case": ["GenerateStoryUseCase"],
                    "repository": ["StoryRepository", "StoryRepositoryImpl"],
                    "viewmodel": ["StoryViewModel"],
                    "screen": ["StoryScreen"],
                    "api": ["generateStory", "AIApiService"]
                }
            },
            {
                "name": "智能对话",
                "required_components": {
                    "use_case": ["SendDialogueMessageUseCase"],
                    "repository": ["DialogueRepository", "DialogueRepositoryImpl"],
                    "viewmodel": ["DialogueViewModel"],
                    "screen": ["DialogueScreen"],
                    "persistence": ["DialogueMessageDao", "DialogueMessageEntity"]
                }
            },
            {
                "name": "拍照识别",
                "required_components": {
                    "use_case": ["RecognizeImageUseCase"],
                    "repository": ["ImageRecognitionRepository"],
                    "viewmodel": ["CameraViewModel"],
                    "screen": ["CameraScreen"],
                    "permission": ["CAMERA"]
                }
            }
        ]
        
        for story in user_stories:
            missing_components = []
            
            for component_type, components in story["required_components"].items():
                for component in components:
                    # 检查组件是否存在
                    found = False
                    
                    # 在函数中查找
                    for func_key in self.functions:
                        if component in func_key:
                            found = True
                            break
                    
                    # 在文件中查找（类、接口等）
                    if not found:
                        for context in self.contexts.values():
                            if component == context.class_name:
                                found = True
                                break
                    
                    if not found:
                        missing_components.append(f"{component_type}:{component}")
            
            if missing_components:
                self.add_issue(
                    file_path="UserStory",
                    line_number=0,
                    issue_type="incomplete_user_story",
                    description=f"用户故事 '{story['name']}' 缺少组件: {missing_components}",
                    level=IssueLevel.BLOCKER,
                    evidence=str(missing_components),
                    suggestion="实现缺失的组件以完成用户故事"
                )
    
    def validate_data_flow(self):
        """验证数据流的完整性"""
        # 检查 UseCase -> Repository -> API/DAO 的调用链
        use_cases = {k: v for k, v in self.functions.items() if 'UseCase' in k}
        
        for key, use_case in use_cases.items():
            # UseCase 应该调用 Repository
            if 'repository' not in use_case.body.lower():
                self.add_issue(
                    file_path=use_case.file_path,
                    line_number=use_case.line_number,
                    issue_type="broken_data_flow",
                    description=f"UseCase '{use_case.name}' 没有调用 Repository",
                    level=IssueLevel.MAJOR,
                    evidence="No repository call found",
                    suggestion="调用相应的 Repository 方法完成数据操作"
                )
    
    def integration_validation(self):
        """集成验证"""
        print("  验证依赖注入...")
        self.validate_dependency_injection()
        
        print("  验证API配置...")
        self.validate_api_configuration()
    
    def validate_dependency_injection(self):
        """验证依赖注入的完整性"""
        # 收集所有 @Inject 注解
        injections_needed = defaultdict(set)
        provides_available = defaultdict(set)
        
        for file_path in self.contexts:
            try:
                with open(file_path, 'r') as f:
                    content = f.read()
                
                # 查找 @Inject
                inject_matches = re.findall(
                    r'@Inject\s+(?:constructor|lateinit\s+var)\s+(\w+)\s*:\s*(\w+)',
                    content
                )
                for var_name, type_name in inject_matches:
                    injections_needed[type_name].add(file_path)
                
                # 查找 @Provides
                provides_matches = re.findall(
                    r'@Provides.*\n\s*fun\s+\w+\([^)]*\)\s*:\s*(\w+)',
                    content
                )
                for type_name in provides_matches:
                    provides_available[type_name].add(file_path)
                
                # 查找 @Binds
                binds_matches = re.findall(
                    r'@Binds.*\n\s*abstract\s+fun\s+\w+\([^)]*\)\s*:\s*(\w+)',
                    content
                )
                for type_name in binds_matches:
                    provides_available[type_name].add(file_path)
                    
            except Exception as e:
                pass
        
        # 检查缺失的提供者
        for needed_type, files in injections_needed.items():
            if needed_type not in provides_available:
                # 检查是否是常见的自动提供类型
                if needed_type not in ['Context', 'Application', 'Activity']:
                    self.add_issue(
                        file_path="DependencyInjection",
                        line_number=0,
                        issue_type="missing_di_provider",
                        description=f"类型 '{needed_type}' 需要注入但没有提供者",
                        level=IssueLevel.MAJOR,
                        evidence=f"Required in: {list(files)[:2]}",
                        suggestion=f"在 DI 模块中添加 @Provides 或 @Binds 方法提供 {needed_type}",
                        confidence=0.8
                    )
    
    def validate_api_configuration(self):
        """验证API配置"""
        # 检查 build.gradle 中的 API 配置
        build_gradle_path = os.path.join(self.project_root, "app/build.gradle.kts")
        
        if os.path.exists(build_gradle_path):
            try:
                with open(build_gradle_path, 'r') as f:
                    content = f.read()
                
                # 检查是否有本地URL
                if re.search(r'(localhost|127\.0\.0\.1|192\.168\.)', content):
                    self.add_issue(
                        file_path=build_gradle_path,
                        line_number=0,
                        issue_type="local_api_url",
                        description="API 配置使用了本地地址",
                        level=IssueLevel.CRITICAL,
                        evidence="localhost/127.0.0.1 found",
                        suggestion="使用生产环境的 API URL"
                    )
                
                # 检查是否配置了 API keys
                if 'API_KEY' in content and '""' in content:
                    self.add_issue(
                        file_path=build_gradle_path,
                        line_number=0,
                        issue_type="empty_api_key",
                        description="API Key 可能未配置",
                        level=IssueLevel.CRITICAL,
                        evidence="Empty API_KEY found",
                        suggestion="配置真实的 API Key"
                    )
                    
            except Exception as e:
                pass
    
    def filter_false_positives(self):
        """过滤误报"""
        filtered_issues = []
        
        for issue in self.issues:
            # 过滤接口中的"空实现"
            if issue.issue_type == "empty_implementation":
                if issue.file_path in self.contexts:
                    context = self.contexts[issue.file_path]
                    if context.is_interface:
                        continue
            
            # 过滤测试文件中的某些模式
            if issue.file_path in self.contexts:
                context = self.contexts[issue.file_path]
                if context.is_test_file:
                    if issue.issue_type in ["missing_error_handling", "hardcoded_return"]:
                        continue
            
            # 过滤数据类的某些警告
            if issue.file_path in self.contexts:
                context = self.contexts[issue.file_path]
                if context.is_data_class:
                    if issue.description.contains("equals") or issue.description.contains("hashCode"):
                        continue
            
            filtered_issues.append(issue)
        
        self.issues = filtered_issues
    
    def add_issue(self, **kwargs):
        """添加验证问题"""
        issue = ValidationIssue(**kwargs)
        self.issues.append(issue)
    
    def find_kotlin_files(self) -> List[str]:
        """查找所有Kotlin文件"""
        kotlin_files = []
        
        for root, dirs, files in os.walk(os.path.join(self.project_root, "app/src")):
            # 排除构建目录
            dirs[:] = [d for d in dirs if d not in ['build', '.gradle']]
            
            for file in files:
                if file.endswith('.kt'):
                    kotlin_files.append(os.path.join(root, file))
        
        return kotlin_files
    
    def generate_comprehensive_report(self) -> Dict:
        """生成综合报告"""
        # 按级别分类
        issues_by_level = defaultdict(list)
        for issue in self.issues:
            issues_by_level[issue.level].append(issue)
        
        # 统计信息
        total_files = len(self.contexts)
        total_functions = len(self.functions)
        
        # 显示报告
        print("\n" + "="*70)
        print("📊 验证报告汇总")
        print("="*70)
        
        print(f"\n扫描统计:")
        print(f"  📁 文件数: {total_files}")
        print(f"  🔧 函数数: {total_functions}")
        print(f"  ⚠️  问题数: {len(self.issues)}")
        
        # 按级别显示问题
        for level in [IssueLevel.BLOCKER, IssueLevel.CRITICAL, 
                     IssueLevel.MAJOR, IssueLevel.MINOR, IssueLevel.INFO]:
            issues = issues_by_level[level]
            if issues:
                print(f"\n{self.get_level_icon(level)} {level.value} 级别问题 ({len(issues)}个):")
                
                # 显示前5个问题
                for i, issue in enumerate(issues[:5]):
                    print(f"\n  [{i+1}] {issue.description}")
                    print(f"      📍 文件: {issue.file_path}")
                    print(f"      💡 建议: {issue.suggestion}")
                    if issue.confidence < 1.0:
                        print(f"      🎯 置信度: {issue.confidence:.0%}")
                
                if len(issues) > 5:
                    print(f"\n      ... 还有 {len(issues) - 5} 个问题")
        
        # 生成JSON报告
        report = {
            "version": "5.0",
            "success": len(issues_by_level[IssueLevel.BLOCKER]) == 0 and 
                      len(issues_by_level[IssueLevel.CRITICAL]) == 0,
            "statistics": {
                "total_files": total_files,
                "total_functions": total_functions,
                "total_issues": len(self.issues)
            },
            "issues_by_level": {
                level.value: len(issues) for level, issues in issues_by_level.items()
            },
            "critical_issues": [
                {
                    "file": issue.file_path,
                    "line": issue.line_number,
                    "type": issue.issue_type,
                    "description": issue.description,
                    "suggestion": issue.suggestion,
                    "confidence": issue.confidence
                } for issue in (issues_by_level[IssueLevel.BLOCKER] + 
                               issues_by_level[IssueLevel.CRITICAL])[:20]
            ]
        }
        
        # 保存报告
        with open("validation_report_v5.json", "w", encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        # 最终结论
        if report["success"]:
            print("\n" + "="*70)
            print("✅ 验证通过！项目已达到生产发布标准。")
            print("="*70)
        else:
            blocker_count = len(issues_by_level[IssueLevel.BLOCKER])
            critical_count = len(issues_by_level[IssueLevel.CRITICAL])
            print("\n" + "="*70)
            print(f"❌ 发现 {blocker_count + critical_count} 个必须修复的问题")
            print("="*70)
        
        return report
    
    def get_level_icon(self, level: IssueLevel) -> str:
        """获取级别图标"""
        icons = {
            IssueLevel.BLOCKER: "🚫",
            IssueLevel.CRITICAL: "❌",
            IssueLevel.MAJOR: "⚠️",
            IssueLevel.MINOR: "ℹ️",
            IssueLevel.INFO: "💡"
        }
        return icons.get(level, "•")

if __name__ == "__main__":
    validator = IntelligentValidatorV5()
    report = validator.validate()
    
    exit(0 if report["success"] else 1)