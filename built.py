import os
import shutil
import zipfile
import tarfile
import requests
import json
from pathlib import Path

def prepare_cross_platform_release():
    # 定义变量
    os.remove(list(Path('./build/libs').glob('*plain.jar'))[0])
    jar_files = list(Path('./build/libs').glob('*.jar'))
    if not jar_files:
        raise FileNotFoundError("No JAR file found in ./build/libs directory")
    jar_file = str(jar_files[0])
    
    platforms = ["windows", "linux", "macos"]
    # 使用直接下载链接（跳过API查询）
    jre_urls = [
        "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x64_windows_hotspot_17.0.9_9.zip",
        "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jre_x64_linux_hotspot_17.0.9_9.tar.gz",
        "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jre_x64_mac_hotspot_17.0.9_9.tar.gz"
    ]
    jre_exts = ["zip", "tar.gz", "tar.gz"]
    script_names = ["start.bat", "start.sh", "start.sh"]
    
    # 设置请求头（模拟浏览器访问）
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    
    # 遍历所有平台
    for i, platform_name in enumerate(platforms):
        print(f"🚀 Preparing package for {platform_name}...")
        
        # 创建目录结构
        pkg_dir = f"release-package-{platform_name}"
        os.makedirs(pkg_dir, exist_ok=True)
        shutil.copy2(jar_file, os.path.join(pkg_dir, os.path.basename(jar_file)))
        
        # 下载JRE
        print(f"⬇️ Downloading JRE for {platform_name}...")
        jre_url = jre_urls[i]
        jre_ext = jre_exts[i]
        jre_filename = f"jre-{platform_name}.{jre_ext}"
        
        try:
            with requests.get(jre_url, headers=headers, stream=True) as r:
                r.raise_for_status()
                with open(jre_filename, 'wb') as f:
                    for chunk in r.iter_content(chunk_size=8192):
                        f.write(chunk)
        except Exception as e:
            print(f"❌ Failed to download JRE: {str(e)}")
            raise
        
        # 解压JRE
        java_dir = os.path.join(pkg_dir, "java")
        os.makedirs(java_dir, exist_ok=True)
        
        try:
            if platform_name == "windows":
                # Windows处理zip
                with zipfile.ZipFile(jre_filename, 'r') as zip_ref:
                    # 提取到临时目录然后移动
                    temp_dir = f"{java_dir}_temp"
                    zip_ref.extractall(temp_dir)
                    # 找到jre目录并移动内容
                    for item in os.listdir(temp_dir):
                        if item.startswith(("jre-", "jdk-")):
                            src = os.path.join(temp_dir, item)
                            for subitem in os.listdir(src):
                                shutil.move(os.path.join(src, subitem), os.path.join(java_dir, subitem))
                            break
                    shutil.rmtree(temp_dir)
            else:
                # Linux/macOS处理tar.gz
                with tarfile.open(jre_filename, 'r:gz') as tar_ref:
                    # 提取到临时目录然后移动
                    temp_dir = f"{java_dir}_temp"
                    tar_ref.extractall(temp_dir)
                    # 找到jre目录并移动内容
                    for item in os.listdir(temp_dir):
                        if item.startswith(("jre-", "jdk-")):
                            src = os.path.join(temp_dir, item)
                            for subitem in os.listdir(src):
                                shutil.move(os.path.join(src, subitem), os.path.join(java_dir, subitem))
                            break
                    shutil.rmtree(temp_dir)
        except Exception as e:
            print(f"❌ Failed to extract JRE: {str(e)}")
            raise
        
        # 创建启动脚本
        script_name = script_names[i]
        script_path = os.path.join(pkg_dir, script_name)
        jar_basename = os.path.basename(jar_file)
        
        if platform_name == "windows":
            with open(script_path, 'w', encoding='utf-8') as f:
                f.write(f'@echo off\n".\\java\\bin\\java.exe" -jar "{jar_basename}"\npause\n')
        else:
            with open(script_path, 'w', encoding='utf-8') as f:
                f.write(f'#!/bin/bash\n"./java/bin/java" -jar "{jar_basename}"\n')
            # 设置可执行权限
            os.chmod(script_path, 0o755)
        
        # 打包
        output_filename = f"CABM4J-{platform_name}-x64-with-jre"
        try:
            if platform_name == "windows":
                with zipfile.ZipFile(f"{output_filename}.zip", 'w', zipfile.ZIP_DEFLATED) as zipf:
                    for root, dirs, files in os.walk(pkg_dir):
                        for file in files:
                            file_path = os.path.join(root, file)
                            arcname = os.path.relpath(file_path, pkg_dir)
                            zipf.write(file_path, arcname)
            else:
                with tarfile.open(f"{output_filename}.tar.gz", 'w:gz') as tar:
                    tar.add(pkg_dir, arcname=os.path.basename(pkg_dir))
        except Exception as e:
            print(f"❌ Failed to create package: {str(e)}")
            raise
        
        # 清理临时文件
        os.remove(jre_filename)
        print(f"✅ Package created: {output_filename}.{'zip' if platform_name == 'windows' else 'tar.gz'}")

if __name__ == "__main__":
    try:
        prepare_cross_platform_release()
    except Exception as e:
        print(f"❌ Build failed: {str(e)}")
        exit(1)
