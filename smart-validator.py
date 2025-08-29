#!/usr/bin/env python3
"""
智能验证系统 - 深度语义分析
不依赖外部库，原生Python实现
"""

import os
import re
import json
import subprocess
from typing import List, Dict, Set, Tuple, Optional
from dataclasses import dataclass
from enum import Enum
from collections import defaultdict

class IssueLevel(Enum):
    BLOCKER = "BLOCKER"
    CRITICAL = "CRITICAL"
    MAJOR = "MAJOR"
    MINOR = "MINOR"

@dataclass
class Issue:
    file: str
    line: int
    type: str
    desc: str
    level: IssueLevel
    evidence: str
    fix: str

class SmartValidator:
    def __init__(self, root="/workspace"):
        self.root = root
        self.issues = []
        self.functions = {}  # file:func -> details
        self.screens = set()
        self.navigations = set()
        
    def validate(self):
        """主验证流程"""
        print("\n🧠 智能验证系统 - 深度分析")
        print("=" * 50)
        
        # 1. 收集项目信息
        print("\n📊 Phase 1: 项目扫描")
        self.scan_project()
        
        # 2. 语义分析
        print("\n🔍 Phase 2: 语义分析")
        self.semantic_analysis()
        
        # 3. 完整性验证
        print("\n✅ Phase 3: 完整性验证")
        self.completeness_check()
        
        # 4. 生成报告
        return self.generate_report()
    
    def scan_project(self):
        """扫描项目结构"""
        kotlin_files = []
        for root, dirs, files in os.walk(os.path.join(self.root, "app/src/main")):
            dirs[:] = [d for d in dirs if d not in ['.git', 'build']]
            for file in files:
                if file.endswith('.kt'):
                    kotlin_files.append(os.path.join(root, file))
        
        print(f"  扫描到 {len(kotlin_files)} 个Kotlin文件")
        
        for file in kotlin_files:
            self.analyze_file(file)
    
    def analyze_file(self, filepath):
        """分析单个文件"""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 1. 提取函数
            self.extract_functions(filepath, content)
            
            # 2. 提取导航
            self.extract_navigation(filepath, content)
            
            # 3. 检查常见问题
            self.check_common_issues(filepath, content)
            
        except Exception as e:
            print(f"  ⚠️ 分析失败 {filepath}: {e}")
    
    def extract_functions(self, filepath, content):
        """提取并分析函数"""
        # 匹配函数定义
        func_pattern = r'(override\s+)?(suspend\s+)?fun\s+(\w+)\s*\(([^)]*)\)(\s*:\s*[^{]+)?\s*(\{[^}]*\}|=)'
        
        for match in re.finditer(func_pattern, content, re.DOTALL):
            func_name = match.group(3)
            func_body = match.group(6) if match.group(6) else ""
            
            # 分析函数特征
            func_info = {
                'name': func_name,
                'file': filepath,
                'is_suspend': bool(match.group(2)),
                'has_params': bool(match.group(4).strip()),
                'has_return': bool(match.group(5)),
                'body': func_body,
                'is_empty': self.is_empty_function(func_body),
                'has_todo': self.has_todo_marker(func_body),
                'is_fake': self.is_fake_implementation(func_body),
                'calls_api': 'api' in func_body.lower() or 'service' in func_body.lower(),
                'handles_error': 'try' in func_body or 'catch' in func_body or 'onFailure' in func_body,
                'updates_ui': '_state' in func_body or 'mutableStateOf' in func_body
            }
            
            key = f"{filepath}::{func_name}"
            self.functions[key] = func_info
            
            # 检查问题
            self.check_function_issues(func_info)
    
    def is_empty_function(self, body):
        """检查是否空函数"""
        if not body or body == '=':
            return True
        
        # 去掉注释和空白
        clean_body = re.sub(r'//.*', '', body)
        clean_body = re.sub(r'/\*.*?\*/', '', clean_body, re.DOTALL)
        clean_body = clean_body.strip()
        
        # 检查是否只有大括号
        if clean_body == '{}' or clean_body == '{ }':
            return True
        
        # 检查是否只有注释
        if clean_body.replace('{', '').replace('}', '').strip() == '':
            return True
        
        return False
    
    def has_todo_marker(self, body):
        """检查TODO标记"""
        markers = ['TODO', 'FIXME', 'XXX', 'HACK', 'TBD']
        for marker in markers:
            if marker in body.upper():
                return True
        return False
    
    def is_fake_implementation(self, body):
        """检查是否假实现"""
        if not body:
            return False
        
        fake_patterns = [
            r'return\s+"[^"]*"$',              # 直接返回字符串
            r'return\s+\d+$',                  # 直接返回数字  
            r'return\s+(true|false)$',         # 直接返回布尔
            r'delay\s*\(\s*\d+\s*\)',          # 只有延迟
            r'return\s+.*[Mm]ock',             # Mock数据
            r'return\s+.*[Ff]ake',             # Fake数据
            r'//\s*TODO',                      # TODO注释
            r'throw\s+NotImplementedError',   # 未实现异常
        ]
        
        for pattern in fake_patterns:
            if re.search(pattern, body, re.MULTILINE | re.IGNORECASE):
                return True
                
        return False
    
    def check_function_issues(self, func_info):
        """检查函数问题"""
        file = func_info['file']
        name = func_info['name']
        
        # 1. 空实现
        if func_info['is_empty']:
            self.add_issue(
                file=file,
                line=0,
                type="empty_function",
                desc=f"函数 {name} 是空实现",
                level=IssueLevel.CRITICAL,
                evidence=f"fun {name}() {{ }}",
                fix="添加实际的实现逻辑"
            )
        
        # 2. TODO标记
        elif func_info['has_todo']:
            self.add_issue(
                file=file,
                line=0,
                type="todo_marker",
                desc=f"函数 {name} 包含TODO标记",
                level=IssueLevel.MAJOR,
                evidence="TODO/FIXME in function body",
                fix="完成TODO项"
            )
        
        # 3. 假实现
        elif func_info['is_fake']:
            self.add_issue(
                file=file,
                line=0,
                type="fake_implementation",
                desc=f"函数 {name} 可能是假实现",
                level=IssueLevel.CRITICAL,
                evidence="Suspicious return pattern",
                fix="实现真实的业务逻辑"
            )
        
        # 4. ViewModel特定检查
        if 'ViewModel' in file:
            # API调用但不更新UI
            if func_info['calls_api'] and not func_info['updates_ui']:
                self.add_issue(
                    file=file,
                    line=0,
                    type="missing_ui_update",
                    desc=f"{name} 调用API但未更新UI状态",
                    level=IssueLevel.MAJOR,
                    evidence="API call without state update",
                    fix="添加 _state.value = ... 更新UI"
                )
            
            # API调用但无错误处理
            if func_info['calls_api'] and not func_info['handles_error']:
                self.add_issue(
                    file=file,
                    line=0,
                    type="missing_error_handling",
                    desc=f"{name} 缺少错误处理",
                    level=IssueLevel.MAJOR,
                    evidence="API call without try-catch",
                    fix="添加 try-catch 或 .onFailure"
                )
    
    def extract_navigation(self, filepath, content):
        """提取导航信息"""
        # 导航调用
        nav_calls = re.findall(r'navigate\s*\(\s*["\']([^"\']+)["\']', content)
        self.navigations.update(nav_calls)
        
        # Screen定义
        screen_defs = re.findall(r'composable\s*\(\s*["\']([^"\']+)["\']', content)
        self.screens.update(screen_defs)
    
    def check_common_issues(self, filepath, content):
        """检查常见问题"""
        # 1. 硬编码延迟
        if re.search(r'delay\s*\(\s*\d{3,}\s*\)', content):
            self.add_issue(
                file=filepath,
                line=0,
                type="hardcoded_delay",
                desc="发现硬编码延迟",
                level=IssueLevel.MINOR,
                evidence="delay(1000)",
                fix="使用常量或配置"
            )
        
        # 2. 本地URL
        if re.search(r'(localhost|127\.0\.0\.1|192\.168\.)', content):
            if not '://' in content:  # 排除注释中的URL
                return
            self.add_issue(
                file=filepath,
                line=0,
                type="local_url",
                desc="发现本地URL",
                level=IssueLevel.CRITICAL,
                evidence="localhost/127.0.0.1",
                fix="使用生产环境URL"
            )
        
        # 3. 打印日志
        if 'println(' in content:
            self.add_issue(
                file=filepath,
                line=0,
                type="debug_print",
                desc="发现println调试语句",
                level=IssueLevel.MINOR,
                evidence="println(...)",
                fix="使用正式的日志框架"
            )
    
    def semantic_analysis(self):
        """语义分析 - 深度检查"""
        print("  分析函数语义...")
        
        # 1. 检查Repository完整性
        self.check_repository_completeness()
        
        # 2. 检查UseCase完整性
        self.check_usecase_completeness()
        
        # 3. 检查Screen完整性
        self.check_screen_completeness()
    
    def check_repository_completeness(self):
        """检查Repository实现"""
        repos = {k: v for k, v in self.functions.items() if 'Repository' in k}
        
        for key, func in repos.items():
            # 检查CRUD操作
            if any(op in func['name'].lower() for op in ['save', 'insert', 'update', 'delete']):
                if 'dao' not in func['body'] and 'dataStore' not in func['body']:
                    self.add_issue(
                        file=func['file'],
                        line=0,
                        type="missing_persistence",
                        desc=f"{func['name']} 声称保存但未调用持久化",
                        level=IssueLevel.CRITICAL,
                        evidence="No dao/dataStore call",
                        fix="调用DAO或DataStore保存数据"
                    )
    
    def check_usecase_completeness(self):
        """检查UseCase完整性"""
        usecases = {k: v for k, v in self.functions.items() if 'UseCase' in k}
        
        for key, func in usecases.items():
            # UseCase应该调用Repository
            if 'repository' not in func['body'].lower():
                self.add_issue(
                    file=func['file'],
                    line=0,
                    type="incomplete_usecase",
                    desc=f"UseCase {func['name']} 未调用Repository",
                    level=IssueLevel.MAJOR,
                    evidence="No repository call",
                    fix="调用相应的Repository方法"
                )
    
    def check_screen_completeness(self):
        """检查Screen完整性"""
        screens = {k: v for k, v in self.functions.items() if 'Screen' in k and '@Composable' in open(v['file']).read()}
        
        for key, func in screens.items():
            content = open(func['file']).read()
            
            # 检查是否有ViewModel
            if 'viewModel' not in content and 'ViewModel' not in content:
                self.add_issue(
                    file=func['file'],
                    line=0,
                    type="missing_viewmodel",
                    desc=f"Screen {func['name']} 没有使用ViewModel",
                    level=IssueLevel.MAJOR,
                    evidence="No viewModel found",
                    fix="添加对应的ViewModel"
                )
    
    def completeness_check(self):
        """完整性检查"""
        print("  检查功能完整性...")
        
        # 1. 导航完整性
        self.check_navigation_completeness()
        
        # 2. 用户流程完整性
        self.check_user_flows()
        
        # 3. 依赖注入完整性
        self.check_dependency_injection()
    
    def check_navigation_completeness(self):
        """检查导航完整性"""
        missing = self.navigations - self.screens
        
        for route in missing:
            self.add_issue(
                file="Navigation",
                line=0,
                type="missing_screen",
                desc=f"导航目标 '{route}' 无对应Screen",
                level=IssueLevel.BLOCKER,
                evidence=f"navigate('{route}')",
                fix=f"实现 {route} Screen"
            )
    
    def check_user_flows(self):
        """检查关键用户流程"""
        flows = [
            {
                'name': '故事生成',
                'required': ['GenerateStoryUseCase', 'StoryRepository', 'StoryViewModel', 'StoryScreen']
            },
            {
                'name': '拍照识别',
                'required': ['CameraScreen', 'ImageRecognitionRepository', 'RecognizeImageUseCase']
            },
            {
                'name': '家长登录',
                'required': ['ParentLoginScreen', 'ParentLoginViewModel', 'ParentDashboard']
            }
        ]
        
        for flow in flows:
            missing = []
            for component in flow['required']:
                found = any(component in k for k in self.functions.keys())
                if not found:
                    missing.append(component)
            
            if missing:
                self.add_issue(
                    file="UserFlow",
                    line=0,
                    type="incomplete_flow",
                    desc=f"用户流程 '{flow['name']}' 缺少: {missing}",
                    level=IssueLevel.BLOCKER,
                    evidence=str(missing),
                    fix="实现缺失的组件"
                )
    
    def check_dependency_injection(self):
        """检查依赖注入"""
        # 简化检查 - 查找@Inject但没有@Provides
        inject_count = 0
        provides_count = 0
        
        for _, func in self.functions.items():
            content = open(func['file']).read()
            inject_count += len(re.findall(r'@Inject', content))
            provides_count += len(re.findall(r'@Provides', content))
        
        if inject_count > provides_count * 2:  # 粗略估计
            self.add_issue(
                file="DependencyInjection",
                line=0,
                type="di_imbalance",
                desc=f"依赖注入可能不完整 (@Inject: {inject_count}, @Provides: {provides_count})",
                level=IssueLevel.MAJOR,
                evidence=f"Inject/Provides ratio",
                fix="检查DI模块是否完整"
            )
    
    def add_issue(self, **kwargs):
        """添加问题"""
        self.issues.append(Issue(**kwargs))
    
    def generate_report(self):
        """生成报告"""
        # 分类统计
        blockers = [i for i in self.issues if i.level == IssueLevel.BLOCKER]
        criticals = [i for i in self.issues if i.level == IssueLevel.CRITICAL]
        majors = [i for i in self.issues if i.level == IssueLevel.MAJOR]
        minors = [i for i in self.issues if i.level == IssueLevel.MINOR]
        
        print("\n" + "="*50)
        print("📊 验证报告")
        print("="*50)
        
        print(f"\n扫描统计:")
        print(f"  函数数量: {len(self.functions)}")
        print(f"  Screen数: {len(self.screens)}")
        print(f"  导航数: {len(self.navigations)}")
        
        print(f"\n问题统计:")
        print(f"  🚫 阻塞: {len(blockers)}")
        print(f"  ❌ 严重: {len(criticals)}")
        print(f"  ⚠️  重要: {len(majors)}")
        print(f"  ℹ️  次要: {len(minors)}")
        
        # 显示严重问题
        if blockers or criticals:
            print(f"\n必须修复的问题:")
            for issue in (blockers + criticals)[:10]:
                print(f"\n[{issue.level.value}] {issue.desc}")
                print(f"  文件: {issue.file}")
                print(f"  建议: {issue.fix}")
        
        # 保存详细报告
        report = {
            'success': len(blockers) + len(criticals) == 0,
            'stats': {
                'functions': len(self.functions),
                'screens': len(self.screens),
                'navigations': len(self.navigations)
            },
            'issues': {
                'blocker': len(blockers),
                'critical': len(criticals),
                'major': len(majors),
                'minor': len(minors),
                'total': len(self.issues)
            },
            'details': [
                {
                    'file': i.file,
                    'type': i.type,
                    'desc': i.desc,
                    'level': i.level.value,
                    'fix': i.fix
                } for i in self.issues[:50]
            ]
        }
        
        with open('smart_validation_report.json', 'w') as f:
            json.dump(report, f, indent=2)
        
        if report['success']:
            print("\n✅ 验证通过！")
        else:
            print(f"\n❌ 发现 {len(blockers) + len(criticals)} 个必须修复的问题")
        
        return report

if __name__ == "__main__":
    validator = SmartValidator()
    report = validator.validate()
    exit(0 if report['success'] else 1)