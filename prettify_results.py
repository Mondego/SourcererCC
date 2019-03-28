#!/usr/bin/env python3

import datetime as dt
from argparse import ArgumentParser
import sys
import os
import re


def get_file_name(file_path):
    projects_dir = "tokenizer-sample-input"
    return re.sub(r"\.zip/[a-zA-Z0-9-.]+-master/", "/tree/master/", file_path.strip("\"")[len(projects_dir + "/"):].replace("--", "/"))


def get_file_lines(filename):
    with open(filename, "r") as file_descr:
        for line in file_descr:
            yield line.strip("\n")


def merge_results(pairs):
    res = {}
    for x, y in pairs:
        if not x in res:
            res[x] = [y]
        else:
            res[x].append(y)
    return res


def filter_files(path, extension):
    res = set()
    if os.path.isdir(path):
        filtered_files = filter(lambda x: x.endswith(extension), os.listdir(path))
        res.update(map(lambda x: os.path.join(path, x), filtered_files))
    elif os.path.isfile(path):
        res.add(path)
    else:
        print("ERROR: '{}' not found!".format(path))
        sys.exit()
    return res


def get_projects_info(bookkeeping_files_path):
    files = filter_files(bookkeeping_files_path, ".projs")
    projects_info = []
    for bookkeeping_file in files:
        for line in get_file_lines(bookkeeping_file):
            project_info = {
                "project_id": line.split(",")[0],
                "project_path": line.split(",")[1],
                "project_url": line.split(",")[2]
            }
            projects_info.append(project_info)
    return projects_info


def get_tokens_info(tokens_files_path, blocks_mode, experimental):
    files = filter_files(tokens_files_path, ".tokens")
    tokens_info = {}
    for tokens_file in files:
        for line in get_file_lines(tokens_file):
            (info_line, tokens_list) = line.split("@#@")
            code_id = info_line.split(",")[1]
            if blocks_mode and experimental:
                file_info = {
                    "project_id": info_line.split(",")[0],
                    "relative_id": code_id[:5],
                    "file_id": code_id[5:],
                    "total_tokens": info_line.split(",")[2],
                    "unique_tokens": info_line.split(",")[3],
                    "experimental": info_line.split(",")[4],
                    "tokens_hash": info_line.split(",")[5],
                    "tokens_list": {k: v for k, v in map(lambda x: x.split("@@::@@"), tokens_list.split(","))}
                }
            else:
                file_info = {
                    "project_id": info_line.split(",")[0],
                    "total_tokens": info_line.split(",")[2],
                    "unique_tokens": info_line.split(",")[3],
                    "tokens_hash": info_line.split(",")[4],
                    "tokens_list": {k: v for k, v in map(lambda x: x.split("@@::@@"), tokens_list.split(","))}
                }
            tokens_info[code_id] = file_info
    return tokens_info


def get_stats_info(stats_files_path, blocks_mode):
    files = filter_files(stats_files_path, ".stats")
    stats_info = {}
    for stats_file in files:
        for line in get_file_lines(stats_file):
            if blocks_mode:
                if line.split(",")[0] == "f":
                    code_id = line.split(",")[2]
                    stats = {
                        "project_id": line.split(",")[1],
                        "file_path": line.split(",")[3],
                        "file_url": line.split(",")[4],
                        "file_hash": line.split(",")[5],
                        "file_size": line.split(",")[6],
                        "lines": line.split(",")[7],
                        "LOC": line.split(",")[8],
                        "SLOC": line.split(",")[9]
                    }
                elif line.split(",")[0] == "b":
                    code_id = line.split(",")[2]
                    relative_id = code_id[:5]
                    file_id = code_id[5:]
                    stats = {
                        "project_id": line.split(",")[1],
                        "relative_id": relative_id,
                        "file_id": file_id,
                        "block_hash": line.split(",")[3],
                        "block_lines": line.split(",")[4],
                        "block_LOC": line.split(",")[5],
                        "block_SLOC": line.split(",")[6],
                        "start_line": line.split(",")[7],
                        "end_line": line.split(",")[8]
                    }
            else:
                code_id = line.split(",")[1]
                stats = {
                    "project_id": line.split(",")[1],
                    "file_path": line.split(",")[2],
                    "file_url": line.split(",")[3],
                    "file_hash": line.split(",")[4],
                    "file_size": line.split(",")[5],
                    "lines": line.split(",")[6],
                    "LOC": line.split(",")[7],
                    "SLOC": line.split(",")[8]
                }
            if code_id in stats_info:
                print("intersection on id {}".format(code_id))
                print("old: {}".format(stats_info[code_id]))
                print("new: {}".format(stats))
            stats_info[code_id] = stats
    return stats_info


def get_results(results_file):
    results_pairs = []
    for line in get_file_lines(results_file):
        code_id_1 = line.split(",")[1]
        code_id_2 = line.split(",")[3]
        results_pairs.append((code_id_1, code_id_2))
    results = merge_results(results_pairs)
    return results


def print_results(results_file, stats_file, blocks_mode, experimental):
    stats = get_stats_info(stats_files, blocks_mode)
    results = get_results(results_file)
    formatted_titles = {}
    if blocks_mode:
        for code_id in stats.keys():
            if "start_line" in stats[code_id]:
                formatted_titles[code_id] = "{}(lines {}-{}, total {})".format(get_file_name(stats[stats[code_id]["file_id"]]["file_path"]), stats[code_id]["start_line"], stats[code_id]["end_line"], int(stats[code_id]["end_line"]) - int(stats[code_id]["start_line"]) + 1)
    else:
        formatted_titles = {code_id: "{}({} SLOC)".format(get_file_name(stats[code_id]["file_path"]), stats[code_id]["SLOC"]) for code_id in stats.keys()}
    print("Results list:")
    for code_id, code_id_list in results.items():
        print("{} is similar to:".format(formatted_titles[code_id]))
        print("    " + "\n    ".join(map(lambda x: formatted_titles[x], code_id_list)))
        print()


def print_projects_list(bookkeeping_files):
    projects_info = get_projects_info(bookkeeping_files)
    print("Projects list:")
    for project in projects_info:
        project_lines = ["{}: {}".format(k, v) for k, v in project.items()]
        print("    " + "\n    ".join(project_lines))
        print()


def print_tokens(tokens_files, blocks_mode, experimental):
    tokens_info = get_tokens_info(tokens_files, blocks_mode, experimental)
    print("Files/tokens info list:")
    for code_id, stat in tokens_info.items():
        print("    {}:".format(code_id))
        stat_line = ["{}: {}".format(k, v) for k, v in stat.items() if k != "tokens_list"]
        print("        " + "\n        ".join(stat_line))
        print("        tokens_list: ")
        tokens_lines = ["{}: {}".format(k, v) for k, v in stat["tokens_list"].items()]
        print("            " + "\n            ".join(tokens_lines))


def print_stats(stats_file, blocks_mode):
    stats_info = get_stats_info(options.stats_files, options.blocks_mode)
    print("Stats list:")
    for code_id, stat in stats_info.items():
        print("    {}:".format(code_id))
        stat_lines = ["{}: {}".format(k, v) for k, v in stat.items()]
        print("        " + "\n        ".join(stat_lines))


if __name__ == "__main__":
    parser = ArgumentParser()
    parser.add_argument("--blocks-mode", dest="blocks_mode", nargs="?", const=True, default=False, help="Specify if files produced in blocks-mode")
    parser.add_argument("--experimental", dest="experimental", nargs="?", const=True, default=False, help="If experimental values included in blocks-mode")
    parser.add_argument("-b", "--bookkeepingFiles", dest="bookkeeping_files", default=False, help="File or folder with bookkeeping files (*.projs).")
    parser.add_argument("-t", "--tokensFiles", dest="tokens_files", default=False, help="File or folder with tokens files (*.tokens).")
    parser.add_argument("-s", "--statsFiles", dest="stats_files", default=False, help="File or folder with stats files (*.stats).")
    parser.add_argument("-r", "--resultsFile", dest="results_file", default=False, help="File with results of SourcererCC (results.pairs).")

    options = parser.parse_args(sys.argv[1:])

    if len(sys.argv) == 1:
        print("No arguments were passed. Try running with '--help'.")
        sys.exit(0)
    elif not options.blocks_mode and options.experimental:
        print("--experimental can be used only with --blocks-mode")
        sys.exit(0)

    p_start = dt.datetime.now()

    if options.results_file:
        if not options.stats_files:
            print("No stats files specified. Exiting")
            sys.exit(0)
        print_results(options.results_file, options.stats_file, options.blocks_mode, options.experimental)
    elif options.bookkeeping_files:
        print_projects_list(options.bookkeeping_files)
    elif options.tokens_files:
        print_tokens(options.token_files, options.blocks_mode, options.experimental)
    elif options.stats_files:
        print_stats(options.stats_files, options.blocks_mode)

    print("Processed in {}".format(dt.datetime.now() - p_start))