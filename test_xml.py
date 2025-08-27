import xml.etree.ElementTree as ET

try:
    # 尝试解析TaskMapper.xml文件
    tree = ET.parse(r'c:\Users\ccclo\Desktop\bangong\employee-backend\src\main\resources\mapper\TaskMapper.xml')
    root = tree.getroot()
    print("✅ TaskMapper.xml 解析成功!")
    print(f"Namespace: {root.get('namespace')}")
    
    # 检查select元素
    selects = root.findall('select')
    print(f"发现 {len(selects)} 个select元素:")
    for select in selects:
        print(f"  - {select.get('id')}")
        
except ET.ParseError as e:
    print(f"❌ XML解析错误: {e}")
except FileNotFoundError:
    print("❌ 文件未找到")
except Exception as e:
    print(f"❌ 其他错误: {e}")