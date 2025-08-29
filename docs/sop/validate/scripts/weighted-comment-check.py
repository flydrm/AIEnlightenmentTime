#!/usr/bin/env python3
"""
AI启蒙时光项目 - 加权注释质量检查脚本
根据不同层次和类型的代码赋予不同权重
"""

import os
import re
from dataclasses import dataclass
from typing import Dict, List, Tuple
from collections import defaultdict

@dataclass
class ClassInfo:
    """类信息"""
    name: str
    type: str  # class/interface/object/data class等
    file_path: str
    line_num: int
    has_comment: bool
    weight: float
    layer: str  # domain/presentation/data/test/other

@dataclass
class MethodInfo:
    """方法信息"""
    name: str
    file_path: str
    line_num: int
    line_count: int
    complexity: int
    has_comment: bool
    is_test: bool

class WeightedCommentChecker:
    """加权注释质量检查器"""
    
    # 层级权重定义
    LAYER_WEIGHTS = {
        'domain': {
            'usecase': 1.5,      # UseCase类
            'repository': 1.5,   # Repository接口
            'model': 1.5,        # 领域模型
            'default': 1.3       # 其他Domain层类
        },
        'presentation': {
            'viewmodel': 1.3,    # ViewModel
            'screen': 1.3,       # Screen组件
            'state': 1.3,        # State类
            'default': 1.1       # 其他Presentation层类
        },
        'data': {
            'repository_impl': 1.2,  # Repository实现
            'manager': 1.2,          # Manager类
            'entity': 1.2,           # Entity/DAO
            'default': 1.0           # 其他Data层类
        },
        'test': 0.5,                 # 测试类统一权重
        'other': 1.0                 # 其他类默认权重
    }
    
    # 中文注释要求（按层级）
    CHINESE_REQUIREMENTS = {
        'domain': 0.8,      # Domain层要求80%
        'presentation': 0.7, # Presentation层要求70%
        'data': 0.6,        # Data层要求60%
        'test': 0.4,        # 测试类要求40%
        'other': 0.5        # 其他类要求50%
    }
    
    def __init__(self):
        self.classes: List[ClassInfo] = []
        self.methods: List[MethodInfo] = []
        self.stats = defaultdict(lambda: defaultdict(int))
        
    def analyze_project(self, root_path: str = 'app/src/main/java'):
        """分析整个项目"""
        for root, dirs, files in os.walk(root_path):
            for file in files:
                if file.endswith('.kt'):
                    file_path = os.path.join(root, file)
                    self.analyze_file(file_path)
    
    def get_layer(self, file_path: str) -> str:
        """根据文件路径判断所属层级"""
        if '/test/' in file_path or '/androidTest/' in file_path:
            return 'test'
        elif '/domain/' in file_path:
            return 'domain'
        elif '/presentation/' in file_path:
            return 'presentation'
        elif '/data/' in file_path:
            return 'data'
        else:
            return 'other'
    
    def get_class_type(self, class_name: str, file_path: str) -> str:
        """根据类名和路径判断类的具体类型"""
        layer = self.get_layer(file_path)
        
        if layer == 'domain':
            if 'UseCase' in class_name:
                return 'usecase'
            elif 'Repository' in class_name and 'Impl' not in class_name:
                return 'repository'
            elif any(keyword in class_name for keyword in ['Model', 'Entity', 'State']):
                return 'model'
        elif layer == 'presentation':
            if 'ViewModel' in class_name:
                return 'viewmodel'
            elif 'Screen' in class_name:
                return 'screen'
            elif 'State' in class_name or 'UiState' in class_name:
                return 'state'
        elif layer == 'data':
            if 'RepositoryImpl' in class_name:
                return 'repository_impl'
            elif 'Manager' in class_name:
                return 'manager'
            elif 'Entity' in class_name or 'Dao' in class_name:
                return 'entity'
        
        return 'default'
    
    def get_weight(self, class_name: str, file_path: str) -> float:
        """获取类的权重"""
        layer = self.get_layer(file_path)
        
        if layer == 'test':
            return self.LAYER_WEIGHTS['test']
        
        class_type = self.get_class_type(class_name, file_path)
        layer_weights = self.LAYER_WEIGHTS.get(layer, {})
        
        if isinstance(layer_weights, dict):
            return layer_weights.get(class_type, layer_weights.get('default', 1.0))
        else:
            return layer_weights
    
    def check_class_comment(self, lines: List[str], line_num: int) -> bool:
        """检查类是否有注释"""
        # 向前查找最多15行
        for i in range(max(0, line_num - 15), line_num):
            if '*/' in lines[i]:
                # 找到注释结束符，检查是否是文档注释
                for j in range(max(0, i - 20), i + 1):
                    if '/**' in lines[j]:
                        return True
        return False
    
    def check_method_complexity(self, lines: List[str], start_line: int) -> Tuple[int, int]:
        """检查方法复杂度，返回(行数, 控制流数量)"""
        brace_count = 0
        line_count = 0
        control_flows = 0
        
        for i in range(start_line, min(len(lines), start_line + 100)):
            line = lines[i]
            
            if '{' in line:
                brace_count += line.count('{')
            if '}' in line:
                brace_count -= line.count('}')
                
            # 统计控制流
            if re.search(r'\b(if|when|for|while|try)\b', line):
                control_flows += 1
            
            line_count += 1
            
            if brace_count == 0 and i > start_line:
                break
        
        return line_count, control_flows
    
    def analyze_file(self, file_path: str):
        """分析单个文件"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except:
            return
        
        lines = content.split('\n')
        layer = self.get_layer(file_path)
        is_test = layer == 'test'
        
        # 统计类
        class_pattern = re.compile(r'^\s*(class|interface|object|data\s+class|sealed\s+class|enum\s+class)\s+(\w+)')
        method_pattern = re.compile(r'^\s*(fun|suspend\s+fun)\s+(\w+)')
        
        # 类注释统计
        for i, line in enumerate(lines):
            match = class_pattern.match(line)
            if match:
                class_type = match.group(1)
                class_name = match.group(2)
                has_comment = self.check_class_comment(lines, i)
                weight = self.get_weight(class_name, file_path)
                
                class_info = ClassInfo(
                    name=class_name,
                    type=class_type,
                    file_path=file_path,
                    line_num=i + 1,
                    has_comment=has_comment,
                    weight=weight,
                    layer=layer
                )
                self.classes.append(class_info)
                
                # 更新统计
                self.stats[layer]['total_classes'] += 1
                self.stats[layer]['total_weight'] += weight
                if has_comment:
                    self.stats[layer]['commented_classes'] += 1
                    self.stats[layer]['commented_weight'] += weight
        
        # 方法复杂度统计（不包括测试方法）
        if not is_test:
            for i, line in enumerate(lines):
                match = method_pattern.match(line)
                if match:
                    method_name = match.group(2)
                    line_count, complexity = self.check_method_complexity(lines, i)
                    
                    # 判断是否为复杂方法
                    if line_count > 20 or complexity > 3:
                        has_comment = self.check_class_comment(lines, i)  # 复用类注释检查逻辑
                        
                        method_info = MethodInfo(
                            name=method_name,
                            file_path=file_path,
                            line_num=i + 1,
                            line_count=line_count,
                            complexity=complexity,
                            has_comment=has_comment,
                            is_test=is_test
                        )
                        self.methods.append(method_info)
        
        # 中文注释统计
        chinese_comment_lines = 0
        total_comment_lines = 0
        
        for line in lines:
            if re.match(r'^\s*(//|/\*|\*)', line):
                total_comment_lines += 1
                if re.search(r'[\u4e00-\u9fa5]', line):
                    chinese_comment_lines += 1
        
        self.stats[layer]['chinese_comment_lines'] += chinese_comment_lines
        self.stats[layer]['total_comment_lines'] += total_comment_lines
    
    def calculate_scores(self) -> Dict[str, float]:
        """计算各项得分"""
        scores = {}
        
        # 1. 类注释得分（35分）
        total_weight = sum(stats['total_weight'] for stats in self.stats.values())
        commented_weight = sum(stats['commented_weight'] for stats in self.stats.values())
        
        if total_weight > 0:
            class_coverage = commented_weight / total_weight
            scores['class_score'] = class_coverage * 35
        else:
            scores['class_score'] = 0
        
        # 2. 复杂方法注释得分（25分）
        complex_methods = len(self.methods)
        commented_methods = sum(1 for m in self.methods if m.has_comment)
        
        if complex_methods > 0:
            method_coverage = commented_methods / complex_methods
            scores['method_score'] = method_coverage * 25
        else:
            scores['method_score'] = 25  # 没有复杂方法则满分
        
        # 3. UI组件交互注释得分（20分）
        # 简化处理：检查Presentation层的Screen类注释情况
        ui_classes = [c for c in self.classes if c.layer == 'presentation' and 'Screen' in c.name]
        ui_commented = sum(1 for c in ui_classes if c.has_comment)
        
        if ui_classes:
            ui_coverage = ui_commented / len(ui_classes)
            scores['ui_score'] = ui_coverage * 20
        else:
            scores['ui_score'] = 20  # 没有UI组件则满分
        
        # 4. 中文注释使用率得分（20分）
        # 按层级加权计算
        chinese_score = 0
        layer_count = 0
        
        for layer, requirement in self.CHINESE_REQUIREMENTS.items():
            if layer in self.stats and self.stats[layer]['total_comment_lines'] > 0:
                layer_count += 1
                chinese_ratio = self.stats[layer]['chinese_comment_lines'] / self.stats[layer]['total_comment_lines']
                
                # 根据要求计算得分
                if chinese_ratio >= requirement:
                    layer_score = 20  # 满足要求得满分
                else:
                    layer_score = (chinese_ratio / requirement) * 20
                
                # 根据层级重要性加权
                if layer == 'domain':
                    chinese_score += layer_score * 0.3
                elif layer == 'presentation':
                    chinese_score += layer_score * 0.3
                elif layer == 'data':
                    chinese_score += layer_score * 0.2
                elif layer == 'test':
                    chinese_score += layer_score * 0.1
                else:
                    chinese_score += layer_score * 0.1
        
        scores['chinese_score'] = min(20, chinese_score)  # 确保不超过20分
        
        # 总分
        scores['total'] = (scores['class_score'] + 
                          scores['method_score'] + 
                          scores['ui_score'] + 
                          scores['chinese_score'])
        
        return scores
    
    def print_report(self):
        """打印详细报告"""
        print("=" * 70)
        print("🔍 AI启蒙时光项目 - 加权注释质量检查报告")
        print("=" * 70)
        
        # 按层级统计
        print("\n📊 各层级注释统计：")
        print("-" * 70)
        print(f"{'层级':<15} {'类数量':<10} {'注释覆盖':<15} {'权重覆盖':<15} {'中文比例':<15}")
        print("-" * 70)
        
        for layer in ['domain', 'presentation', 'data', 'test', 'other']:
            if layer in self.stats:
                stats = self.stats[layer]
                total_classes = stats['total_classes']
                commented_classes = stats['commented_classes']
                total_weight = stats['total_weight']
                commented_weight = stats['commented_weight']
                chinese_lines = stats['chinese_comment_lines']
                total_lines = stats['total_comment_lines']
                
                class_coverage = f"{commented_classes}/{total_classes} ({commented_classes/total_classes*100:.1f}%)" if total_classes > 0 else "N/A"
                weight_coverage = f"{commented_weight:.1f}/{total_weight:.1f} ({commented_weight/total_weight*100:.1f}%)" if total_weight > 0 else "N/A"
                chinese_ratio = f"{chinese_lines}/{total_lines} ({chinese_lines/total_lines*100:.1f}%)" if total_lines > 0 else "N/A"
                
                print(f"{layer:<15} {total_classes:<10} {class_coverage:<15} {weight_coverage:<15} {chinese_ratio:<15}")
        
        # 核心类注释情况
        print("\n🎯 核心功能类注释情况：")
        print("-" * 70)
        
        core_classes = [c for c in self.classes if c.weight >= 1.3]
        missing_core = [c for c in core_classes if not c.has_comment]
        
        if missing_core:
            print("❌ 以下核心类缺少注释（必须立即补充）：")
            for c in missing_core[:10]:  # 只显示前10个
                print(f"   - {c.name} (权重:{c.weight}) @ {c.file_path}:{c.line_num}")
            if len(missing_core) > 10:
                print(f"   ... 还有 {len(missing_core) - 10} 个核心类缺少注释")
        else:
            print("✅ 所有核心功能类都有注释！")
        
        # 复杂方法统计
        print("\n📈 复杂方法注释情况：")
        print("-" * 70)
        
        if self.methods:
            commented_methods = sum(1 for m in self.methods if m.has_comment)
            print(f"复杂方法总数：{len(self.methods)}")
            print(f"已注释方法数：{commented_methods}")
            print(f"注释覆盖率：{commented_methods/len(self.methods)*100:.1f}%")
            
            missing_methods = [m for m in self.methods if not m.has_comment]
            if missing_methods:
                print("\n❌ 以下复杂方法缺少注释：")
                for m in missing_methods[:5]:
                    print(f"   - {m.name} (行数:{m.line_count}, 复杂度:{m.complexity}) @ {m.file_path}:{m.line_num}")
                if len(missing_methods) > 5:
                    print(f"   ... 还有 {len(missing_methods) - 5} 个复杂方法缺少注释")
        else:
            print("✅ 未发现复杂方法")
        
        # 计算得分
        scores = self.calculate_scores()
        
        print("\n🏆 质量评分：")
        print("-" * 70)
        print(f"类注释得分：      {scores['class_score']:.1f}/35")
        print(f"方法注释得分：    {scores['method_score']:.1f}/25")
        print(f"UI交互注释得分：  {scores['ui_score']:.1f}/20")
        print(f"中文注释得分：    {scores['chinese_score']:.1f}/20")
        print("-" * 70)
        print(f"总分：           {scores['total']:.1f}/100")
        
        # 评级
        total_score = scores['total']
        if total_score >= 95:
            grade = "A+ (卓越)"
            color = "🟢"
        elif total_score >= 90:
            grade = "A (优秀)"
            color = "🟢"
        elif total_score >= 85:
            grade = "B (良好)"
            color = "🟡"
        elif total_score >= 75:
            grade = "C (及格)"
            color = "🟡"
        else:
            grade = "D (不及格)"
            color = "🔴"
        
        print(f"\n{color} 最终评级：{grade}")
        
        if total_score < 95:
            print("\n💡 改进建议：")
            if scores['class_score'] < 35:
                print("1. 优先为Domain层和Presentation层的核心类添加注释")
            if scores['method_score'] < 25:
                print("2. 为复杂方法添加详细的功能说明和流程注释")
            if scores['chinese_score'] < 20:
                print("3. 增加中文注释比例，特别是Domain层要达到80%以上")
        else:
            print("\n🎉 恭喜！代码注释质量已达到生产标准！")
        
        print("\n" + "=" * 70)
        
        return total_score >= 95

def main():
    """主函数"""
    checker = WeightedCommentChecker()
    checker.analyze_project()
    success = checker.print_report()
    
    # 返回状态码
    exit(0 if success else 1)

if __name__ == "__main__":
    main()