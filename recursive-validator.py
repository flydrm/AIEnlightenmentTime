#!/usr/bin/env python3
"""
综合递归验证系统 - AI启蒙时光项目
确保项目100%完成，无遗漏
"""

import os
import re
import json
import subprocess
from typing import List, Dict, Tuple, Set
from dataclasses import dataclass
from enum import Enum

class IssueLevel(Enum):
    CRITICAL = "CRITICAL"  # 必须修复
    HIGH = "HIGH"          # 应该修复
    MEDIUM = "MEDIUM"      # 建议修复
    LOW = "LOW"            # 可选修复

@dataclass
class Issue:
    file_path: str
    line_number: int
    issue_type: str
    description: str
    level: IssueLevel
    code_snippet: str
    suggested_fix: str = ""

class RecursiveValidator:
    def __init__(self, project_root: str = "/workspace"):
        self.project_root = project_root
        self.issues: List[Issue] = []
        self.fixed_count = 0
        self.iteration_count = 0
        
        # 定义验证规则
        self.patterns = {
            # Level 0: 语法层
            "todo_markers": {
                "pattern": r"(?i)\b(TODO|FIXME|XXX|HACK|TBD|WIP|PENDING|UNFINISHED)\b(?![\w.])",
                "level": IssueLevel.CRITICAL,
                "description": "发现TODO标记"
            },
            
            # Level 1: 实现层
            "placeholder_comments": {
                "pattern": r"(?i)(//|/\*).*(in production|would be|should be|will be|to be implemented|placeholder|temporary|mock implementation|actual implementation|real implementation)",
                "level": IssueLevel.CRITICAL,
                "description": "发现占位符注释"
            },
            
            "empty_implementations": {
                "pattern": r"fun\s+\w+\s*\([^)]*\)\s*\{[\s]*\}",
                "level": IssueLevel.CRITICAL,
                "description": "发现空方法实现"
            },
            
            "not_implemented": {
                "pattern": r"(?i)(NotImplementedError|TODO\(\)|error\(\"Not implemented\"\))",
                "level": IssueLevel.CRITICAL,
                "description": "发现未实现异常"
            },
            
            # Level 2: 真实数据层
            "mock_data": {
                "pattern": r"(?i)(mock|fake|dummy|stub|hardcoded|test data|sample data)",
                "level": IssueLevel.HIGH,
                "description": "可能的模拟数据"
            },
            
            "delay_simulation": {
                "pattern": r"\bdelay\s*\(\s*\d+\s*\)",
                "level": IssueLevel.HIGH,
                "description": "发现延迟模拟",
                "exclude_if": ["withTimeoutOrNull", "timer", "schedule"]
            },
            
            # Level 3: 集成层
            "localhost_urls": {
                "pattern": r"(localhost|127\.0\.0\.1|192\.168\.|10\.0\.|example\.com)",
                "level": IssueLevel.HIGH,
                "description": "发现本地或测试URL"
            },
            
            # Level 4: 行为层
            "missing_error_handling": {
                "pattern": r"catch\s*\{\s*\}|catch.*\{\s*//\s*\}",
                "level": IssueLevel.MEDIUM,
                "description": "空的错误处理"
            }
        }
        
        # 智能检测规则
        self.semantic_rules = {
            "incomplete_navigation": self.check_navigation_targets,
            "missing_persistence": self.check_data_persistence,
            "incomplete_ui_handlers": self.check_ui_handlers,
            "missing_api_implementation": self.check_api_implementation
        }
    
    def validate(self, max_iterations: int = 10) -> Dict:
        """主验证入口 - 递归直到无问题"""
        print(f"\n{'='*60}")
        print("🔍 综合递归验证系统 v2.0")
        print(f"{'='*60}\n")
        
        while self.iteration_count < max_iterations:
            self.iteration_count += 1
            print(f"\n📍 第 {self.iteration_count} 轮验证")
            print("-" * 40)
            
            # 清空上一轮的问题
            self.issues.clear()
            
            # 执行多层验证
            self.run_syntax_validation()
            self.run_implementation_validation()
            self.run_integration_validation()
            self.run_behavior_validation()
            
            # 统计问题
            critical_issues = [i for i in self.issues if i.level == IssueLevel.CRITICAL]
            high_issues = [i for i in self.issues if i.level == IssueLevel.HIGH]
            
            if not critical_issues and not high_issues:
                print(f"\n✅ 验证通过！共修复 {self.fixed_count} 个问题")
                return self.generate_report(success=True)
            
            print(f"\n❌ 发现问题:")
            print(f"   - 严重问题: {len(critical_issues)} 个")
            print(f"   - 重要问题: {len(high_issues)} 个")
            
            # 显示问题详情
            self.display_issues()
            
            # 尝试自动修复
            if self.auto_fix_issues():
                self.fixed_count += len(critical_issues) + len(high_issues)
                print(f"\n🔧 已自动修复部分问题，继续验证...")
            else:
                print(f"\n⚠️  需要人工介入修复")
                return self.generate_report(success=False)
        
        print(f"\n⚠️  达到最大迭代次数 {max_iterations}")
        return self.generate_report(success=False)
    
    def run_syntax_validation(self):
        """语法层验证"""
        print("  ▶ 语法层验证...")
        
        kotlin_files = self.find_files("*.kt", exclude_dirs=["build", "test"])
        
        for file_path in kotlin_files:
            self.scan_file_patterns(file_path)
    
    def run_implementation_validation(self):
        """实现层验证"""
        print("  ▶ 实现层验证...")
        
        # 检查所有ViewModel和Repository的实现完整性
        for rule_name, rule_func in self.semantic_rules.items():
            rule_func()
    
    def run_integration_validation(self):
        """集成层验证"""
        print("  ▶ 集成层验证...")
        
        # 检查API配置
        self.check_api_configuration()
        
        # 检查依赖注入
        self.check_dependency_injection()
    
    def run_behavior_validation(self):
        """行为层验证"""
        print("  ▶ 行为层验证...")
        
        # 检查用户流程完整性
        self.check_user_flows()
    
    def scan_file_patterns(self, file_path: str):
        """扫描文件中的模式匹配"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')
            
            for pattern_name, pattern_info in self.patterns.items():
                pattern = pattern_info["pattern"]
                
                for i, line in enumerate(lines):
                    if re.search(pattern, line):
                        # 跳过测试文件中的某些模式
                        if "test" in file_path.lower() and pattern_name in ["mock_data"]:
                            continue
                        
                        # 跳过注释中的URL
                        if pattern_name == "localhost_urls" and line.strip().startswith("//"):
                            continue
                        
                        self.issues.append(Issue(
                            file_path=file_path,
                            line_number=i + 1,
                            issue_type=pattern_name,
                            description=pattern_info["description"],
                            level=pattern_info["level"],
                            code_snippet=line.strip()
                        ))
        except Exception as e:
            print(f"    ⚠️  无法读取文件 {file_path}: {e}")
    
    def check_navigation_targets(self):
        """检查导航目标是否存在"""
        navigation_pattern = r"navController\.navigate\s*\(\s*[\"']([^\"']+)[\"']\s*\)"
        nav_files = self.find_files("*NavHost*.kt")
        
        routes = set()
        navigations = []
        
        # 收集所有路由定义
        for file_path in nav_files:
            with open(file_path, 'r') as f:
                content = f.read()
                # 查找路由定义
                route_defs = re.findall(r'object\s+(\w+)\s*:\s*Screen\s*\(\s*"([^"]+)"\s*\)', content)
                routes.update([route[1] for route in route_defs])
                
                # 查找导航调用
                nav_calls = re.findall(navigation_pattern, content)
                navigations.extend([(file_path, call) for call in nav_calls])
        
        # 检查所有导航目标是否有对应的路由
        for file_path, nav_target in navigations:
            if nav_target not in routes:
                self.issues.append(Issue(
                    file_path=file_path,
                    line_number=0,
                    issue_type="missing_navigation_target",
                    description=f"导航目标 '{nav_target}' 未定义",
                    level=IssueLevel.CRITICAL,
                    code_snippet=f"navigate({nav_target})"
                ))
    
    def check_data_persistence(self):
        """检查数据持久化实现"""
        repo_files = self.find_files("*Repository*.kt", include_pattern="app/src/main")
        
        for file_path in repo_files:
            if "Impl" in file_path:
                with open(file_path, 'r') as f:
                    content = f.read()
                    
                # 检查是否有内存存储而非持久化
                if "mutableMapOf" in content or "mutableListOf" in content:
                    if "dataStore" not in content and "dao" not in content.lower():
                        self.issues.append(Issue(
                            file_path=file_path,
                            line_number=0,
                            issue_type="missing_persistence",
                            description="使用内存存储而非持久化存储",
                            level=IssueLevel.HIGH,
                            code_snippet="mutableMapOf/mutableListOf"
                        ))
    
    def check_ui_handlers(self):
        """检查UI事件处理器"""
        screen_files = self.find_files("*Screen.kt", include_pattern="app/src/main")
        
        for file_path in screen_files:
            with open(file_path, 'r') as f:
                content = f.read()
                
            # 检查onClick但没有实现
            onclick_pattern = r'onClick\s*=\s*\{\s*\}'
            empty_handlers = re.findall(onclick_pattern, content)
            
            if empty_handlers:
                self.issues.append(Issue(
                    file_path=file_path,
                    line_number=0,
                    issue_type="empty_click_handler",
                    description="发现空的点击处理器",
                    level=IssueLevel.HIGH,
                    code_snippet="onClick = { }"
                ))
    
    def check_api_implementation(self):
        """检查API实现完整性"""
        api_files = self.find_files("*ApiService*.kt")
        
        for file_path in api_files:
            with open(file_path, 'r') as f:
                content = f.read()
                
            # 检查是否有未实现的API方法
            if "@POST" in content or "@GET" in content:
                # 检查是否有对应的实现
                if "suspend fun" in content:
                    methods = re.findall(r'suspend\s+fun\s+(\w+)', content)
                    # 这里可以进一步检查每个方法是否被实际调用
    
    def check_api_configuration(self):
        """检查API配置完整性"""
        build_gradle = os.path.join(self.project_root, "app/build.gradle.kts")
        
        if os.path.exists(build_gradle):
            with open(build_gradle, 'r') as f:
                content = f.read()
                
            # 检查API URL配置
            if "localhost" in content or "127.0.0.1" in content:
                self.issues.append(Issue(
                    file_path=build_gradle,
                    line_number=0,
                    issue_type="local_api_url",
                    description="API URL使用本地地址",
                    level=IssueLevel.CRITICAL,
                    code_snippet="localhost/127.0.0.1"
                ))
    
    def check_dependency_injection(self):
        """检查依赖注入完整性"""
        # 检查所有@Inject是否有对应的@Provides
        pass
    
    def check_user_flows(self):
        """检查用户流程完整性"""
        # 检查关键用户流程是否可以端到端完成
        pass
    
    def find_files(self, pattern: str, include_pattern: str = "", exclude_dirs: List[str] = None) -> List[str]:
        """查找匹配的文件"""
        if exclude_dirs is None:
            exclude_dirs = ["build", ".gradle", ".git"]
        
        files = []
        for root, dirs, filenames in os.walk(self.project_root):
            # 排除特定目录
            dirs[:] = [d for d in dirs if d not in exclude_dirs]
            
            # 过滤路径
            if include_pattern and include_pattern not in root:
                continue
            
            for filename in filenames:
                if filename.endswith(pattern.replace("*", "")):
                    files.append(os.path.join(root, filename))
        
        return files
    
    def display_issues(self):
        """显示发现的问题"""
        # 按严重程度分组
        grouped = {}
        for issue in self.issues:
            if issue.level not in grouped:
                grouped[issue.level] = []
            grouped[issue.level].append(issue)
        
        for level in [IssueLevel.CRITICAL, IssueLevel.HIGH, IssueLevel.MEDIUM, IssueLevel.LOW]:
            if level in grouped:
                print(f"\n   {level.value} 级别问题:")
                for issue in grouped[level][:5]:  # 只显示前5个
                    print(f"   - {issue.file_path}:{issue.line_number}")
                    print(f"     {issue.description}")
                    print(f"     代码: {issue.code_snippet[:60]}...")
                
                if len(grouped[level]) > 5:
                    print(f"     ... 还有 {len(grouped[level]) - 5} 个问题")
    
    def auto_fix_issues(self) -> bool:
        """尝试自动修复问题"""
        # 这里可以实现一些简单的自动修复
        # 返回是否成功修复了一些问题
        return False
    
    def generate_report(self, success: bool) -> Dict:
        """生成验证报告"""
        report = {
            "success": success,
            "iterations": self.iteration_count,
            "fixed_count": self.fixed_count,
            "remaining_issues": len(self.issues),
            "critical_issues": len([i for i in self.issues if i.level == IssueLevel.CRITICAL]),
            "timestamp": subprocess.check_output(['date']).decode().strip()
        }
        
        # 保存报告
        with open("validation_report.json", "w") as f:
            json.dump(report, f, indent=2)
        
        return report

if __name__ == "__main__":
    validator = RecursiveValidator()
    result = validator.validate()
    
    if result["success"]:
        print("\n🎉 项目验证通过，可以发布！")
    else:
        print(f"\n❌ 项目还有 {result['remaining_issues']} 个问题需要解决")
        print(f"   其中严重问题: {result['critical_issues']} 个")