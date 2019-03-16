#!/usr/bin/python
import requests
import re
import os
import sys
import shutil

PROJECTS_DIRECTORY = "tokenizer-sample-input"


def download_project(url):
    archive_url = "{}/archive/master.zip".format(url)
    return requests.get(archive_url).content


def save_project(url):
    project_content = download_project(url)
    user, project = re.findall(r"https://github.com/(.*)/(.*)$", url)[0]
    filename = "{}--{}.zip".format(user, project)
    open("tokenizers/file-level/{}/{}".format(PROJECTS_DIRECTORY, filename), "wb+").write(project_content)
    open("tokenizers/block-level/{}/{}".format(PROJECTS_DIRECTORY, filename), "wb+").write(project_content)
    return filename


def delete_folder(pth):
    for sub in pth.iterdir():
        if sub.is_dir():
            delete_folder(sub)
        else:
            sub.unlink()
    pth.rmdir()


def create_folder(folder_name):
    folder_name = os.path.abspath(folder_name)
    if os.path.isdir(folder_name):
        shutil.rmtree(folder_name)
    os.makedirs(folder_name)


if len(sys.argv) < 2:
    print("This script downloads projects by urls in specified file and puts them in directories for tokenizers")
    print("Usage:")
    print("{} <urls.txt>".format(sys.argv[0]))
    print("Where <urls.txt> is file with project urls")
    exit(0)
project_list = []
create_folder("tokenizers/file-level/{}/".format(PROJECTS_DIRECTORY))
create_folder("tokenizers/block-level/{}/".format(PROJECTS_DIRECTORY))
urls_filename = sys.argv[1]
with open(urls_filename) as urls_file:
    for url in urls_file:
        url = url.strip('\n')
        filename = save_project(url)
        project_list.append("{}/{}".format(PROJECTS_DIRECTORY, filename))
        user, project = re.findall(r"https://github.com/(.*)/(.*)$", url)[0]
        print("Downloaded {}/{}".format(user, project))

open("tokenizers/file-level/project-list.txt", "w").write("\n".join(project_list))
open("tokenizers/block-level/project-list.txt", "w").write("\n".join(project_list))
