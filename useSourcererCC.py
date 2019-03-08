import requests, re, os

PROJECTS_DIRECTORY = "tokenizer-sample-input"

def download_project(url):
    archive_url = "{}/archive/master.zip".format(url)
    return requests.get(archive_url).content

project_list = []
os.makedirs("tokenizers/file-level/{}/".format(PROJECTS_DIRECTORY))
# TODO: specify urls file in args
with open("urls.txt") as urls_file:
    for url in urls_file:
        url = url.strip('\n')
        project_content = download_project(url)
        user, project = re.findall(r"https://github.com/(.*)/(.*)$", url)[0]
        filename = "{}--{}.zip".format(user, project)
        open("tokenizers/file-level/{}/{}".format(PROJECTS_DIRECTORY, filename), "wb+").write(project_content)
        project_list.append("{}/{}".format(PROJECTS_DIRECTORY, filename))
        print("Downloaded {}/{}".format(user, project))

open("tokenizers/file-level/project-list.txt", "w").write("\n".join(project_list))