#!/usr/bin/env python3

import datetime as dt
from optparse import OptionParser
import sys
import os
import re


def filter_files(path, extension):
    res = set()
    if os.path.isdir(path):
        filtered_files = filter(lambda x: x.endswith(extension), os.listdir(path))
        res.update(map(lambda x: os.path.join(path, x), filtered_files))
    elif os.path.isfile(path):
        res.add(path)
    else:
        print("ERROR: '{}' not found!".format(path))
    return res


def get_projects_info(bookkeeping_files_path):
    files = filter_files(bookkeeping_files_path, ".projs")
    projects_info = []
    for bookkeeping_file in files:
        with open(bookkeeping_file, "r") as file_descr:
            for line in map(lambda x: x.strip("\n"), file_descr):
                project_info = {
                    "project_id": line.split(",")[0],
                    "project_path": line.split(",")[1],
                    "project_url": line.split(",")[2]
                }
                projects_info.append(project_info)
    return projects_info


def get_tokens_info(tokens_files_path):
    files = filter_files(tokens_files_path, ".tokens")
    tokens_info = {}
    for tokens_file in files:
        with open(tokens_file, "r") as file_descr:
            for line in map(lambda x: x.strip("\n"), file_descr):
                (info_line, tokens_list) = line.split("@#@")
                project_id = info_line.split(",")[0]
                file_id = info_line.split(",")[1]
                full_id = "{},{}".format(project_id, file_id)
                file_info = {
                    "total_tokens": info_line.split(",")[2],
                    "unique_tokens": info_line.split(",")[3],
                    "tokens_hash": info_line.split(",")[4],
                    "tokens_list": {k: v for k, v in map(lambda x: x.split("@@::@@"), tokens_list.split(","))}
                }
                tokens_info[full_id] = file_info
    return tokens_info


def get_stats_info(stats_files_path):
    files = filter_files(stats_files_path, ".stats")
    stats_info = {}
    for stats_file in files:
        with open(stats_file, "r") as file_descr:
            for line in map(lambda x: x.strip("\n"), file_descr):
                project_id = line.split(",")[0]
                file_id = line.split(",")[1]
                full_id = "{},{}".format(project_id, file_id)
                stats = {
                    "file_path": line.split(",")[2],
                    "file_url": line.split(",")[3],
                    "file_hash": line.split(",")[4],
                    "file_size": line.split(",")[5],
                    "lines": line.split(",")[6],
                    "LOC": line.split(",")[7],
                    "SLOC": line.split(",")[8]
                }
                stats_info[full_id] = stats
    return stats_info


def merge_results(pairs):
    res = {}
    for x, y in pairs:
        if not x in res:
            res[x] = [y]
        else:
            res[x].append(y)
    return res


def get_results(results_file):
    results_pairs = []
    with open(results_file, "r") as file_descr:
        for line in map(lambda x: x.strip("\n"), file_descr):
            project_id_1 = line.split(",")[0]
            file_id_1 = line.split(",")[1]
            project_id_2 = line.split(",")[2]
            file_id_2 = line.split(",")[3]
            full_id_1 = "{},{}".format(project_id_1, file_id_1)
            full_id_2 = "{},{}".format(project_id_2, file_id_2)
            results_pairs.append((full_id_1, full_id_2))
    results = merge_results(results_pairs)
    return results


def get_file_name(file_path):
    projects_dir = "tokenizer-sample-input"
    return re.sub(r"\.zip/[a-zA-Z0-9-]+-master/", "/tree/master/", file_path.strip("\"")[len(projects_dir + "/"):].replace("--", "/"))


if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-b", "--bookkeepingFiles", dest="bookkeepingFiles", type="string", default=False, help="File or folder with bookkeeping files (*.projs).")
    parser.add_option("-t", "--tokensFiles", dest="tokensFiles", type="string", default=False, help="File or folder with tokens files (*.tokens).")
    parser.add_option("-s", "--statsFiles", dest="statsFiles", type="string", default=False, help="File or folder with stats files (*.stats).")
    parser.add_option("-r", "--resultsFile", dest="resultsFile", type="string", default=False, help="File with results of SourcererCC (results.pairs).")

    (options, args) = parser.parse_args()

    if len(sys.argv) <= 1:
        print("No arguments were passed. Try running with '--help'.")
        sys.exit(0)

    p_start = dt.datetime.now()

    if options.resultsFile:
        if not options.statsFiles:
            print("No stats files specified. Exiting")
            sys.exit(0)
        stats = get_stats_info(options.statsFiles)
        results = get_results(options.resultsFile)
        formatted_titles = {full_id: "{}({} SLOC)".format(get_file_name(stats[full_id]["file_path"]), stats[full_id]["SLOC"]) for full_id in stats.keys()}
        print("Results list:")
        for full_id, full_id_list in results.items():
            print("{} is similar to:".format(formatted_titles[full_id]))
            print("    " + "\n    ".join(map(lambda x: formatted_titles[x], full_id_list)))
            print()
    elif options.bookkeepingFiles:
        projects_info = get_projects_info(options.bookkeepingFiles)
        print("Projects list:")
        for project in projects_info:
            project_lines = ["{}: {}".format(k, v) for k, v in project.items()]
            print("    " + "\n    ".join(project_lines))
            print()
    elif options.tokensFiles:
        tokens_info = get_tokens_info(options.tokensFiles)
        print("Files/tokens info list:")
        for full_id, stat in tokens_info.items():
            print("    {}:".format(full_id))
            stat_line = ["{}: {}".format(k, v) for k, v in stat.items() if k != "tokens_list"]
            print("        " + "\n        ".join(stat_line))
            print("        tokens_list: ")
            tokens_lines = ["{}: {}".format(k, v) for k, v in stat["tokens_list"].items()]
            print("            " + "\n            ".join(tokens_lines))
    elif options.statsFiles:
        stats_info = get_stats_info(options.statsFiles)
        print("Stats list:")
        for full_id, stat in stats_info.items():
            print("    {}:".format(full_id))
            stat_lines = ["{}: {}".format(k, v) for k, v in stat.items()]
            print("        " + "\n        ".join(stat_lines))

    print("Processed in {}".format(dt.datetime.now() - p_start))