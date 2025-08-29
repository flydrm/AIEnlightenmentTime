#!/usr/bin/env python3
"""
多维度智能验证系统 V5.1 - 修复版
"""

import os
import re
import json
from typing import List, Dict, Set, Optional
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

class ValidatorV5:
    def __init__(self, root="/workspace"):
        self.root = root
        self.issues = []
        self.files_analyzed = 0
        self.functions_found = 0
        
    def validate(self):
        print("\n" + "="*60)
        print("🧠 多维度智能验证系统 V5.1")
        print("="*60)
        
        print("\n📊 Phase 1: 代码扫描与分析")
        self.scan_code()
        
        print("\n🔍 Phase 2: 深度问题检测")
        self.deep_analysis()
        
        print("\n✅ Phase 3: 生成报告")
        return self.generate_report()
    
    def scan_code(self):
        """扫描代码，查找具体问题"""
        
        # 1. 查找所有Kotlin文件
        kotlin_files = []
        for root, dirs, files in os.walk(os.path.join(self.root, "app/src/main")):
            dirs[:] = [d for d in dirs if d not in ['.git', 'build']]
            for file in files:
                if file.endswith('.kt'):
                    kotlin_files.append(os.path.join(root, file))
        
        self.files_analyzed = len(kotlin_files)
        print(f"  扫描到 {self.files_analyzed} 个Kotlin文件")
        
        # 2. 分析每个文件
        for file_path in kotlin_files:
            self.analyze_file(file_path)
    
    def analyze_file(self, file_path):
        """分析单个文件"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')
            
            # 检查是否是接口或抽象类
            is_interface = 'interface ' in content and 'class' not in lines[0]
            is_abstract = 'abstract class' in content
            is_test = '/test/' in file_path
            
            # 查找函数
            for i, line in enumerate(lines):
                # 改进的函数匹配
                if re.search(r'\bfun\s+\w+\s*\(', line):
                    self.functions_found += 1
                    
                    # 检查函数实现
                    if not is_interface and not is_abstract:
                        # 查找函数体
                        if '{' in line:
                            # 单行函数检查
                            if line.count('{') == line.count('}') and line.strip().endswith('{}'):
                                # 空实现
                                func_name = re.search(r'fun\s+(\w+)', line)
                                if func_name and not self.is_expected_empty(func_name.group(1), file_path):
                                    self.add_issue(
                                        file=file_path,
                                        line=i+1,
                                        type="empty_function",
                                        desc=f"函数 {func_name.group(1)} 是空实现",
                                        level=IssueLevel.CRITICAL,
                                        evidence=line.strip(),
                                        fix="添加函数实现"
                                    )
                        
                        # 单表达式函数 fun foo() = ...
                        elif '=' in line and not line.strip().endswith('='):
                            # 这是正常的单表达式函数
                            pass
                
                # 检查TODO/FIXME
                if re.search(r'//\s*(TODO|FIXME|XXX|HACK)', line, re.IGNORECASE):
                    self.add_issue(
                        file=file_path,
                        line=i+1,
                        type="todo_marker",
                        desc=f"发现TODO/FIXME标记",
                        level=IssueLevel.MAJOR,
                        evidence=line.strip(),
                        fix="完成TODO项"
                    )
                
                # 检查硬编码的延迟
                if 'delay(' in line and not is_test:
                    match = re.search(r'delay\s*\(\s*(\d+)\s*\)', line)
                    if match:
                        delay_value = int(match.group(1))
                        if delay_value >= 1000:  # 1秒以上
                            self.add_issue(
                                file=file_path,
                                line=i+1,
                                type="hardcoded_delay",
                                desc=f"硬编码延迟 {delay_value}ms",
                                level=IssueLevel.MINOR,
                                evidence=line.strip(),
                                fix="使用配置或常量"
                            )
                
                # 检查NotImplementedError
                if 'NotImplementedError' in line:
                    self.add_issue(
                        file=file_path,
                        line=i+1,
                        type="not_implemented",
                        desc="抛出NotImplementedError",
                        level=IssueLevel.CRITICAL,
                        evidence=line.strip(),
                        fix="实现该功能"
                    )
                
        except Exception as e:
            print(f"  ⚠️ 分析文件失败 {file_path}: {e}")
    
    def is_expected_empty(self, func_name, file_path):
        """判断函数是否预期为空"""
        # 某些函数可能合理地为空
        expected_empty = [
            'onCleared',  # ViewModel
            'onCreate', 'onDestroy',  # 生命周期
            'beforeTextChanged', 'onTextChanged',  # TextWatcher
        ]
        
        if func_name in expected_empty:
            return True
        
        # 测试文件中的setup/teardown
        if '/test/' in file_path and func_name in ['setUp', 'tearDown']:
            return True
        
        return False
    
    def deep_analysis(self):
        """深度分析项目完整性"""
        
        # 1. 检查关键文件是否存在
        self.check_critical_files()
        
        # 2. 检查导航完整性
        self.check_navigation()
        
        # 3. 检查API配置
        self.check_api_config()
        
        # 4. 检查权限配置
        self.check_permissions()
    
    def check_critical_files(self):
        """检查关键文件"""
        critical_files = [
            ("app/src/main/AndroidManifest.xml", "Android清单文件"),
            ("app/proguard-rules.pro", "ProGuard规则"),
            ("app/src/main/res/xml/network_security_config.xml", "网络安全配置"),
        ]
        
        for file_path, desc in critical_files:
            full_path = os.path.join(self.root, file_path)
            if not os.path.exists(full_path):
                self.add_issue(
                    file=file_path,
                    line=0,
                    type="missing_file",
                    desc=f"缺少{desc}",
                    level=IssueLevel.CRITICAL,
                    evidence="File not found",
                    fix=f"创建{desc}"
                )
    
    def check_navigation(self):
        """检查导航完整性"""
        nav_file = os.path.join(self.root, "app/src/main/java/com/enlightenment/ai/presentation/navigation/EnlightenmentNavHost.kt")
        
        if os.path.exists(nav_file):
            with open(nav_file, 'r') as f:
                content = f.read()
            
            # 检查是否所有主要Screen都已注册
            required_screens = [
                "HomeScreen", "StoryScreen", "DialogueScreen", 
                "ProfileScreen", "CameraScreen", "ParentLoginScreen"
            ]
            
            for screen in required_screens:
                if screen not in content:
                    self.add_issue(
                        file=nav_file,
                        line=0,
                        type="missing_navigation",
                        desc=f"{screen} 未在导航中注册",
                        level=IssueLevel.MAJOR,
                        evidence="Not found in NavHost",
                        fix=f"在NavHost中添加{screen}的路由"
                    )
    
    def check_api_config(self):
        """检查API配置"""
        build_file = os.path.join(self.root, "app/build.gradle.kts")
        
        if os.path.exists(build_file):
            with open(build_file, 'r') as f:
                content = f.read()
            
            # 检查API URL
            if 'localhost' in content or '127.0.0.1' in content:
                self.add_issue(
                    file=build_file,
                    line=0,
                    type="local_api",
                    desc="使用了本地API地址",
                    level=IssueLevel.CRITICAL,
                    evidence="localhost/127.0.0.1",
                    fix="配置生产环境API地址"
                )
            
            # 检查API Key配置
            if re.search(r'buildConfigField.*".*_KEY".*""', content):
                self.add_issue(
                    file=build_file,
                    line=0,
                    type="empty_api_key",
                    desc="API Key为空",
                    level=IssueLevel.CRITICAL,
                    evidence="Empty API_KEY",
                    fix="配置真实的API Key"
                )
    
    def check_permissions(self):
        """检查权限配置"""
        manifest = os.path.join(self.root, "app/src/main/AndroidManifest.xml")
        
        if os.path.exists(manifest):
            with open(manifest, 'r') as f:
                content = f.read()
            
            # 相机功能需要相机权限
            camera_files = []
            for root, dirs, files in os.walk(os.path.join(self.root, "app/src/main")):
                for file in files:
                    if 'Camera' in file and file.endswith('.kt'):
                        camera_files.append(file)
            
            if camera_files and 'android.permission.CAMERA' not in content:
                self.add_issue(
                    file=manifest,
                    line=0,
                    type="missing_permission",
                    desc="缺少相机权限",
                    level=IssueLevel.BLOCKER,
                    evidence="Camera feature without permission",
                    fix="添加 <uses-permission android:name=\"android.permission.CAMERA\" />"
                )
    
    def add_issue(self, **kwargs):
        """添加问题"""
        self.issues.append(Issue(**kwargs))
    
    def generate_report(self):
        """生成报告"""
        # 按级别分类
        by_level = defaultdict(list)
        for issue in self.issues:
            by_level[issue.level].append(issue)
        
        # 统计
        total_issues = len(self.issues)
        critical_count = len(by_level[IssueLevel.BLOCKER]) + len(by_level[IssueLevel.CRITICAL])
        
        print("\n" + "="*60)
        print("📊 验证报告")
        print("="*60)
        
        print(f"\n统计信息:")
        print(f"  📁 分析文件: {self.files_analyzed}")
        print(f"  🔧 发现函数: {self.functions_found}")
        print(f"  ⚠️  问题总数: {total_issues}")
        print(f"  ❌ 严重问题: {critical_count}")
        
        # 显示问题
        for level in [IssueLevel.BLOCKER, IssueLevel.CRITICAL, IssueLevel.MAJOR, IssueLevel.MINOR]:
            issues = by_level[level]
            if issues:
                print(f"\n{level.value} ({len(issues)}个):")
                for i, issue in enumerate(issues[:5]):
                    print(f"\n  [{i+1}] {issue.desc}")
                    print(f"      文件: {issue.file}")
                    if issue.line > 0:
                        print(f"      行号: {issue.line}")
                    print(f"      建议: {issue.fix}")
                
                if len(issues) > 5:
                    print(f"\n  ... 还有 {len(issues)-5} 个{level.value}问题")
        
        # 结论
        success = critical_count == 0
        
        if success:
            print("\n✅ 验证通过！项目质量良好。")
        else:
            print(f"\n❌ 发现 {critical_count} 个严重问题需要修复。")
        
        # 保存报告
        report = {
            'success': success,
            'files_analyzed': self.files_analyzed,
            'functions_found': self.functions_found,
            'total_issues': total_issues,
            'critical_issues': critical_count,
            'issues': [
                {
                    'file': i.file,
                    'line': i.line,
                    'type': i.type,
                    'desc': i.desc,
                    'level': i.level.value,
                    'fix': i.fix
                } for i in self.issues
            ]
        }
        
        with open('validation_report_v5.json', 'w') as f:
            json.dump(report, f, indent=2)
        
        return report

if __name__ == "__main__":
    validator = ValidatorV5()
    report = validator.validate()
    exit(0 if report['success'] else 1)