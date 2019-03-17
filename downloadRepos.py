#!/usr/bin/python
import requests
import re
import os
import sys
import shutil


def download_project(url):
    archive_url = "{}/archive/master.zip".format(url)
    return requests.get(archive_url).content


def save_project(url, projects_dir):
    project_content = download_project(url)
    user, project = re.findall(r"https://github.com/(.*)/(.*)$", url)[0]
    filename = "{}--{}.zip".format(user, project)
    open("{}/{}".format(projects_dir, filename), "wb+").write(project_content)
    return filename


def delete_folder(path):
    for sub in path.iterdir():
        if sub.is_dir():
            delete_folder(sub)
        else:
            sub.unlink()
    path.rmdir()


def create_folder(folder_name):
    folder_name = os.path.abspath(folder_name)
    if os.path.isdir(folder_name):
        shutil.rmtree(folder_name)
    os.makedirs(folder_name)


if len(sys.argv) < 4:
    print("This script downloads projects by urls in specified file and puts them in directories for tokenizers")
    print("Usage:")
    print("{} [URL_FILE] [DESTINATION_DIR] [PROJECTS_DIR]".format(sys.argv[0]))
    print("Where [URL_FILE] is file with project urls")
    print("[DESTINATION_DIR] is directory where to save archives")
    print("[PROJECTS_DIR] is directory name with project archives")
    exit(0)
urls_filename = sys.argv[1]
destination_directory = sys.argv[2]
projects_directory = sys.argv[3]
full_projects_dir_path = "{}/{}".format(destination_directory, projects_directory)
project_list = []
create_folder(full_projects_dir_path)
with open(urls_filename) as urls_file:
    for url in urls_file:
        url = url.strip('\n')
        filename = save_project(url, full_projects_dir_path)
        project_list.append("{}/{}".format(projects_directory, filename))
        user, project = re.findall(r"https://github.com/(.*)/(.*)$", url)[0]
        print("Downloaded {}/{}".format(user, project))

open("{}/project-list.txt".format(destination_directory), "w").write("\n".join(project_list))
