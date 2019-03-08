import requests, re

def download_project(url):
    archive_url = "{}/archive/master.zip".format(url)
    return requests.get(archive_url).content

with open("urls.txt") as urls_file:
    for url in urls_file:
        url = url.strip('\n')
        project_content = download_project(url)
        user, project = re.findall(r"https://github.com/(.*)/(.*)$", url)[0]
        filename = "{}--{}.zip".format(user, project)
        open("tmp/" + filename, "wb").write(project_content)