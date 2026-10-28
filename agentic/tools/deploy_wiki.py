import subprocess
import shutil
import os

wiki_dir = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\wiki"
tmp_dir = r"C:\Users\harri\Documents\Phoenix Code\thaumvibe-4-gemini\agentic\tmp_wiki"

if os.path.exists(tmp_dir):
    shutil.rmtree(tmp_dir, ignore_errors=True)

print("Cloning wiki repo...")
res = subprocess.run(["git", "clone", "https://github.com/iceplopper/neothaum.wiki.git", tmp_dir], capture_output=True, text=True)
print(res.stdout)
print(res.stderr)

print("Configuring git user identity...")
subprocess.run(["git", "config", "user.name", "Ice Plopper"], cwd=tmp_dir)
subprocess.run(["git", "config", "user.email", "iceplopper@users.noreply.github.com"], cwd=tmp_dir)

print("Copying wiki markdown files...")
for fname in os.listdir(wiki_dir):
    src = os.path.join(wiki_dir, fname)
    dst = os.path.join(tmp_dir, fname)
    if os.path.isfile(src):
        shutil.copy2(src, dst)

print("Staging and committing...")
subprocess.run(["git", "add", "-A"], cwd=tmp_dir)
res = subprocess.run(["git", "commit", "-m", "docs(wiki): initialize comprehensive NeoThaum wiki framework and sidebar"], cwd=tmp_dir, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)

print("Pushing to GitHub Wiki...")
res = subprocess.run(["git", "push", "origin", "master"], cwd=tmp_dir, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)

if os.path.exists(tmp_dir):
    shutil.rmtree(tmp_dir, ignore_errors=True)

print("Finished!")
