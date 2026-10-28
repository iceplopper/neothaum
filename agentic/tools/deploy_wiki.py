import os
import shutil
import subprocess
import tempfile

wiki_dir = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"

with tempfile.TemporaryDirectory() as tmp_dir:
    print(f"Working in temporary directory: {tmp_dir}")
    print("Cloning GitHub Wiki repository...")
    res = subprocess.run(["git", "clone", "https://github.com/iceplopper/neothaum.wiki.git", tmp_dir], capture_output=True, text=True)
    if res.returncode != 0:
        print("Clone failed:", res.stderr)
        exit(1)
    
    print("Configuring author identity...")
    subprocess.run(["git", "config", "user.name", "Ice Plopper"], cwd=tmp_dir)
    subprocess.run(["git", "config", "user.email", "iceplopper@users.noreply.github.com"], cwd=tmp_dir)

    print("Copying all wiki pages...")
    for item in os.listdir(wiki_dir):
        src = os.path.join(wiki_dir, item)
        dst = os.path.join(tmp_dir, item)
        if os.path.isfile(src):
            shutil.copy2(src, dst)
            
    print("Staging changes...")
    subprocess.run(["git", "add", "-A"], cwd=tmp_dir)
    
    print("Committing...")
    res = subprocess.run(["git", "commit", "-m", "docs(wiki): populate comprehensive content across all 44 wiki pages"], cwd=tmp_dir, capture_output=True, text=True)
    print(res.stdout)
    print(res.stderr)
    
    print("Pushing to GitHub Wiki (origin/master)...")
    res = subprocess.run(["git", "push", "origin", "master"], cwd=tmp_dir, capture_output=True, text=True)
    print(res.stdout)
    print(res.stderr)
    if res.returncode == 0:
        print("SUCCESS! Wiki is fully updated on GitHub.")
    else:
        print("Push error:", res.stderr)
