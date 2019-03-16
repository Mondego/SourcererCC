import collections
import datetime as dt
import hashlib
import io
import os
import re
import sys
import tarfile
import zipfile
from configparser import ConfigParser
from multiprocessing import Process, Queue

import extractJavaFunction
import extractPythonFunction

MULTIPLIER = 50000000

N_PROCESSES = 2
PROJECTS_BATCH = 20
FILE_projects_list = 'projects-list.txt'
FILE_priority_projects = None
PATH_stats_file_folder = 'files_stats'
PATH_bookkeeping_proj_folder = 'bookkeeping_projs'
PATH_tokens_file_folder = 'files_tokens'
PATH_logs = 'logs'

# Reading Language settings
separators = ''
comment_inline = ''
comment_inline_pattern = comment_inline + '.*?$'
comment_open_tag = ''
comment_close_tag = ''
comment_open_close_pattern = comment_open_tag + '.*?' + comment_close_tag
file_extensions = '.none'

file_count = 0


def hash_measuring_time(data):
    start_time = dt.datetime.now()
    m = hashlib.md5()
    m.update(data)
    hash = m.hexdigest()
    end_time = dt.datetime.now()
    time = (end_time - start_time).microseconds
    return hash, time


def read_config():
    global N_PROCESSES, PROJECTS_BATCH, FILE_projects_list, FILE_priority_projects
    global PATH_stats_file_folder, PATH_bookkeeping_proj_folder, PATH_tokens_file_folder, PATH_logs
    global separators, comment_inline, comment_inline_pattern, comment_open_tag, comment_close_tag, comment_open_close_pattern
    global file_extensions

    global init_file_id
    global init_proj_id
    global proj_id_flag

    config = ConfigParser()

    # parse existing file
    try:
        config.read(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'config.ini'))
    except IOError:
        print('ERROR - Config settings not found. Usage: $python this-script.py config-file.ini')
        sys.exit()

    # Get info from config.ini into global variables
    N_PROCESSES = config.getint('Main', 'N_PROCESSES')
    PROJECTS_BATCH = config.getint('Main', 'PROJECTS_BATCH')
    FILE_projects_list = config.get('Main', 'FILE_projects_list')
    if config.has_option('Main', 'FILE_priority_projects'):
        FILE_priority_projects = config.get('Main', 'FILE_priority_projects')
    PATH_stats_file_folder = config.get('Folders/Files', 'PATH_stats_file_folder')
    PATH_bookkeeping_proj_folder = config.get('Folders/Files', 'PATH_bookkeeping_proj_folder')
    PATH_tokens_file_folder = config.get('Folders/Files', 'PATH_tokens_file_folder')
    PATH_logs = config.get('Folders/Files', 'PATH_logs')

    # Reading Language settings
    separators = "; . [ ] ( ) ~ ! - + & * / % < > ^ | ? { } = # , \" \\ : $ ' ` @"
    comment_inline = re.escape(config.get('Language', 'comment_inline'))
    comment_inline_pattern = comment_inline + '.*?$'
    comment_open_tag = re.escape(config.get('Language', 'comment_open_tag'))
    comment_close_tag = re.escape(config.get('Language', 'comment_close_tag'))
    comment_open_close_pattern = comment_open_tag + '.*?' + comment_close_tag
    file_extensions = config.get('Language', 'File_extensions').split(' ')

    # Reading config settings
    init_file_id = config.getint('Config', 'init_file_id')
    init_proj_id = config.getint('Config', 'init_proj_id')

    # flag before proj_id
    proj_id_flag = config.getint('Config', 'init_proj_id')


def count_lines(string, count_empty = True):
    result = string.count('\n')
    if not string.endswith('\n') and (count_empty or string != ""):
        result += 1
    return result


def remove_comments(string, comment_open_close_pattern, comment_inline_pattern):
    start_time = dt.datetime.now()
    result_string = re.sub(comment_open_close_pattern, '', string, flags=re.DOTALL)  # Remove tagged comments
    result_string = re.sub(comment_inline_pattern, '', result_string, flags=re.MULTILINE)  # Remove end of line comments
    end_time = dt.datetime.now()
    time = (end_time - start_time).microseconds
    return result_string, time


def tokenize_string(string, separators):
    tokenized_string = string
    # Transform separators into spaces (remove them)
    start_time = dt.datetime.now()
    for x in separators:
        tokenized_string = tokenized_string.replace(x, ' ')
    end_time = dt.datetime.now()
    time = (end_time - start_time).microseconds

    tokens_list = tokenized_string.split()  # Create a list of tokens
    total_tokens = len(tokens_list)  # Total number of tokens
    tokens_counter = collections.Counter(tokens_list)  # Count occurrences
    tokens_bag = dict(tokens_counter)  # Converting Counter to dict, {token: occurences}
    unique_tokens = len(tokens_bag)  # Unique number of tokens
    return tokens_bag, total_tokens, unique_tokens, time


# SourcererCC formatting
def format_tokens(tokens_bag):
    start_time = dt.datetime.now()
    tokens = ','.join(['{}@@::@@{}'.format(k, v) for k, v in tokens_bag.items()])
    end_time = dt.datetime.now()
    time = (end_time - start_time).microseconds
    return tokens, time


def get_lines_stats(string, comment_open_close_pattern, comment_inline_pattern):
    lines = count_lines(string)

    string = "\n".join([s for s in string.splitlines() if s.strip()])
    lines_of_code = count_lines(string)

    string, remove_comments_time = remove_comments(string, comment_open_close_pattern, comment_inline_pattern)
    string = "\n".join([s for s in string.splitlines() if s.strip()]).strip()
    source_lines_of_code = count_lines(string, False)

    return string, lines, lines_of_code, source_lines_of_code, remove_comments_time


def process_tokenizer(string, comment_open_close_pattern, comment_inline_pattern, separators):
    hashsum, hash_time = hash_measuring_time(string)

    string, lines, lines_of_code, source_lines_of_code, remove_comments_time = get_lines_stats(string, comment_open_close_pattern, comment_inline_pattern)

    tokens_bag, tokens_count_total, tokens_count_unique, tokenization_time = tokenize_string(string, separators)  # get tokens bag
    tokens, format_time = format_tokens(tokens_bag)  # make formatted string with tokens

    tokens_hash, hash_delta_time = hash_measuring_time(tokens)
    hash_time += hash_delta_time

    return {
        "stats": (hashsum, lines, lines_of_code, source_lines_of_code),
        "final_tokens": (tokens_count_total, tokens_count_unique, tokens_hash, '@#@' + tokens),
        "times": [tokenization_time, format_time, hash_time, remove_comments_time]
    }


# return value: (
#   (file_hash, lines, lines_of_code, source_lines_of_code),
#   (tokens_count_total, tokens_count_unique, tokens_hash, formatted tokens),
#   [tokenization_time, formatting_time, hash_time, removing_comments_time]
# )
def tokenize_file_string(string, comment_inline_pattern, comment_open_close_pattern, separators):
    tmp = process_tokenizer(string, comment_inline_pattern, comment_open_close_pattern, separators)
    final_stats = tmp["stats"]
    final_tokens = tmp["final_tokens"]
    [tokenization_time, formating_time, hash_time, removing_comments_time] = tmp["times"]
    return final_stats, final_tokens, [tokenization_time, formating_time, hash_time, removing_comments_time]


def tokenize_blocks(file_string, comment_inline_pattern, comment_open_close_pattern, separators, file_path):
    # This function will return (file_stats, [(blocks_tokens,blocks_stats)], file_parsing_times]
    block_linenos = None
    blocks = None
    experimental_values = ''
    if '.py' in file_extensions:
        (block_linenos, blocks) = extractPythonFunction.getFunctions(file_string, file_path)
    if '.java' in file_extensions:
        (block_linenos, blocks, experimental_values) = extractJavaFunction.getFunctions(file_string, file_path, separators, comment_inline_pattern)

    if block_linenos is None:
        print("[INFO] Returning None on tokenize_blocks for file {}".format(file_path))
        return None, None, None
    
    se_time = 0
    token_time = 0
    blocks_data = []
    file_hash, hash_time = hash_measuring_time(file_string.encode("utf-8"))
    file_string, lines, LOC, SLOC, re_time = get_lines_stats(file_string, comment_open_close_pattern, comment_inline_pattern)
    final_stats = (file_hash, lines, LOC, SLOC)

    for i, block_string in enumerate(blocks):
        (start_line, end_line) = block_linenos[i]

        tmp = process_tokenizer(block_string, comment_open_close_pattern, comment_inline_pattern, separators)
        block_tokens = tmp["final_tokens"]
        block_stats = (*tmp["stats"], start_line, end_line)

        se_time += tmp["times"][0]
        token_time += tmp["times"][1]
        re_time += tmp["times"][3]
        blocks_data.append((block_tokens, block_stats, experimental_values[i]))
    return final_stats, blocks_data, [se_time, token_time, hash_time, re_time]


def process_file_contents(file_string, proj_id, file_id, container_path, file_path, file_bytes, proj_url, file_tokens_file, file_stats_file):
    global file_count
    file_count += 1

    if project_format in ['zipblocks', 'folderblocks']:
        (final_stats, blocks_data, file_parsing_times) = tokenize_blocks(file_string, comment_inline_pattern, comment_open_close_pattern, separators, os.path.join(container_path, file_path))
        if (final_stats is None) or (blocks_data is None) or (file_parsing_times is None):
            print("[WARNING] " + 'Problems tokenizing file ' + os.path.join(container_path, file_path))
            return [0, 0, 0, 0, 0]

        if len(blocks_data) > 90000:
            print("[WARNING] " + 'File ' + os.path.join(container_path, file_path) + ' has ' + str(len(blocks_data)) + ' blocks, more than 90000. Range MUST be increased.')
            return [0, 0, 0, 0, 0]

        # write file stats
        file_url = proj_url + '/' + file_path.replace(' ', '%20')
        file_path = os.path.join(container_path, file_path)

        # file stats start with a letter 'f'
        (file_hash, lines, LOC, SLOC) = final_stats
        file_stats_file.write('f{},{},\"{}\",\"{}\",\"{}\",{},{},{},{}\n'.format(proj_id, file_id, file_path, file_url, file_hash, file_bytes, lines, LOC, SLOC))
        blocks_data = zip(range(10000, 99999), blocks_data)

        ww_time = dt.datetime.now()
        try:
            for relative_id, block_data in blocks_data:
                (blocks_tokens, blocks_stats, experimental_values) = block_data
                block_id = "{}{}".format(relative_id, file_id)

                (block_hash, block_lines, block_LOC, block_SLOC, start_line, end_line) = blocks_stats
                (tokens_count_total, tokens_count_unique, token_hash, tokens) = blocks_tokens

                # Adjust the blocks stats written to the files, file stats start with a letter 'b'
                file_stats_file.write('b,{},{},\"{}\",{},{},{},{},{}\n'.format(proj_id, block_id, block_hash, block_lines, block_LOC, block_SLOC, start_line, end_line))
                file_tokens_file.write(','.join([proj_id, block_id, str(tokens_count_total), str(tokens_count_unique)]))
                if len(experimental_values) != 0:
                    file_tokens_file.write("," + experimental_values)
                file_tokens_file.write("," + token_hash + tokens + '\n')
            w_time = (dt.datetime.now() - ww_time).microseconds
        except Exception as e:
            print("[WARNING] Error on step3 of process_file_contents")
            print(e)
    return file_parsing_times + [w_time]  # [s_time, t_time, w_time, hash_time, re_time]


def process_regular_folder(process_num, proj_id, proj_path, proj_url, base_file_id, file_tokens_file, file_stats_file):
    zip_time = file_time = string_time = tokens_time = hash_time = write_time = regex_time = 0
    result = [f for dp, dn, filenames in os.walk(proj_path) for f in filenames if (os.path.splitext(f)[1] in file_extensions)]

    for file_path in result:
        z_time = dt.datetime.now()
        try:
            my_file = io.open(os.path.join(proj_path, file_path), encoding='utf-8', errors='ignore')
            file_bytes = str(os.stat(os.path.join(proj_path, file_path)).st_size)
        except Exception as e:
            print("[WARNING] Unable to open file (1) <{}> (process {})".format(file_path, process_num))
            print(e)
            continue

        zip_time += (dt.datetime.now() - z_time).microseconds

        if my_file is None:
            print("[WARNING] " + 'Unable to open file (2) <' + file_path + '> (process ' + str(process_num) + ')')
            continue

        try:
            f_time = dt.datetime.now()
            file_string = my_file.read()
            file_time += (dt.datetime.now() - f_time).microseconds
        except Exception as e:
            print("[WARNING] " + 'Unable to read contents of file %s' % (os.path.join(proj_path, file_path)))
            print(e)
            continue

        file_id = process_num * MULTIPLIER + base_file_id + file_count
        try:
            times = process_file_contents(file_string, proj_id, file_id, proj_path, file_path, file_bytes, proj_url, file_tokens_file, file_stats_file)
        except Exception as e:
            print("[WARNING] " + 'Unable to process file %s. %s' % (os.path.join(proj_path, file_path), e))
            continue

        string_time += times[0]
        tokens_time += times[1]
        hash_time += times[2]
        regex_time += times[3]
        write_time += times[4]
    return zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time


def process_tgz_ball(process_num, tar_file, proj_id, proj_path, proj_url, base_file_id, file_tokens_file, file_stats_file):
    zip_time = file_time = string_time = tokens_time = hash_time = write_time = regex_time = 0
    try:
        with tarfile.open(tar_file, 'r|*') as my_tar_file:
            for file in my_tar_file:
                if not file.isfile():
                    continue

                file_path = file.name
                # Filter by the correct extension
                if not os.path.splitext(file.name)[1] in file_extensions:
                    continue

                file_id = process_num * MULTIPLIER + base_file_id + file_count

                z_time = dt.datetime.now()
                try:
                    myfile = my_tar_file.extractfile(file)
                except Exception as e:
                    print("[WARNING] Unable to open file (1) <{},{},{}> (process {})".format(proj_id, file_id, os.path.join(tar_file, file_path), process_num))
                    print(e)
                    continue
                zip_time += (dt.datetime.now() - z_time).microseconds

                if myfile is None:
                    print("[WARNING] Unable to open file (2) <{},{},{}> (process {})".format(proj_id, file_id, os.path.join(tar_file, file_path), process_num))
                    continue

                f_time = dt.datetime.now()
                file_string = myfile.read()
                file_time += (dt.datetime.now() - f_time).microseconds

                file_bytes = str(file.size)
                times = process_file_contents(file_string, proj_id, file_id, tar_file, file_path, file_bytes, proj_url, file_tokens_file, file_stats_file)
                string_time += times[0]
                tokens_time += times[1]
                hash_time += times[2]
                regex_time += times[3]
                write_time += times[4]
    except Exception as e:
        print("[WARNING] Unable to open tar on <{},{}> (process {})".format(proj_id, proj_path, process_num))
        print("[WARNING] {}".format(e))
        return
    return zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time


def process_zip_ball(process_num, proj_id, proj_path, proj_url, base_file_id, file_tokens_file, file_bookkeeping_proj, file_stats_file):
    zip_time = file_time = string_time = tokens_time = hash_time = write_time = regex_time = 0
    with zipfile.ZipFile(proj_path, 'r') as my_file:
        for file in my_file.infolist():
            if not os.path.splitext(file.filename)[1] in file_extensions:
                continue

            z_time = dt.datetime.now()
            try:
                my_zip_file = my_file.open(file.filename, 'r')
            except Exception as e:
                print("[WARNING] Unable to open file (1) <{}> (process {})".format(os.path.join(proj_path, file), process_num))
                print(e)
                continue
            zip_time += (dt.datetime.now() - z_time).microseconds

            if my_zip_file is None:
                print("[WARNING] Unable to open file (2) <{}> (process {})".format(os.path.join(proj_path, file), process_num))
                continue

            f_time = dt.datetime.now()
            file_string = my_zip_file.read().decode("utf-8", 'ignore')
            file_time += (dt.datetime.now() - f_time).microseconds

            file_id = process_num * MULTIPLIER + base_file_id + file_count
            file_path = file.filename
            file_bytes = str(file.file_size)
            times = process_file_contents(file_string, proj_id, file_id, proj_path, file_path, file_bytes, proj_url, file_tokens_file, file_stats_file)
            string_time += times[0]
            tokens_time += times[1]
            hash_time += times[2]
            regex_time += times[3]
            write_time += times[4]

    return zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time


def process_one_project(process_num, proj_id, proj_path, base_file_id, file_tokens_file, file_bookkeeping_proj, file_stats_file, project_format):
    p_start = dt.datetime.now()

    proj_url = 'NULL'
    proj_id = str(proj_id_flag) + proj_id
    if project_format == 'zipblocks':
        if not os.path.isfile(proj_path):
            print("[WARNING] " + 'Unable to open project <' + proj_id + ',' + proj_path + '> (process ' + str(process_num) + ')')
            return
        times = process_zip_ball(process_num, proj_id, proj_path, proj_url, base_file_id, file_tokens_file, file_bookkeeping_proj, file_stats_file)
    elif project_format == 'folderblocks':
        if not os.path.exists(proj_path):
            print("[WARNING] " + 'Unable to open project <' + proj_id + ',' + proj_path + '> (process ' + str(process_num) + ')')
            return
        times = process_regular_folder(process_num, proj_id, proj_path, proj_url, base_file_id, file_tokens_file, file_stats_file)
    if times is None:
        times = (-1, -1, -1, -1, -1, -1, -1)
    zip_time, file_time, string_time, tokens_time, write_time, hash_time, regex_time = times
    file_bookkeeping_proj.write("{},\"{}\",\"{}\"\n".format(proj_id, proj_path, proj_url))

    p_elapsed = dt.datetime.now() - p_start
    print("[INFO] " + 'Project finished <{},{}> (process {})'.format(proj_id, proj_path, process_num))
    print("[INFO] " + ' ({}): Total: {} ms'.format(process_num, p_elapsed))
    print("[INFO] " + '     Zip: {}'.format(zip_time))
    print("[INFO] " + '     Read: {}'.format(file_time))
    print("[INFO] " + '     Separators: {} ms'.format(string_time))
    print("[INFO] " + '     Tokens: {} ms'.format(tokens_time))
    print("[INFO] " + '     Write: {} ms'.format(write_time))
    print("[INFO] " + '     Hash: {}'.format(hash_time))
    print("[INFO] " + '     regex: {}'.format(regex_time))


def process_projects(process_num, list_projects, base_file_id, global_queue, project_format):
    file_files_tokens_file = os.path.join(PATH_tokens_file_folder, 'files-tokens-{}.tokens'.format(process_num))
    file_bookkeeping_proj_name = os.path.join(PATH_bookkeeping_proj_folder, 'bookkeeping-proj-{}.projs'.format(process_num))
    file_files_stats_file = os.path.join(PATH_stats_file_folder, 'files-stats-{}.stats'.format(process_num))

    global file_count
    file_count = 0
    with open(file_files_tokens_file, 'a+') as tokens_file, \
            open(file_bookkeeping_proj_name, 'a+') as bookkeeping_file, \
            open(file_files_stats_file, 'a+') as stats_file:
        print("[INFO] Process {} starting".format(process_num))
        p_start = dt.datetime.now()
        for proj_id, proj_path in list_projects:
            process_one_project(process_num, str(proj_id), proj_path, base_file_id, tokens_file, bookkeeping_file, stats_file, project_format)

    p_elapsed = (dt.datetime.now() - p_start).seconds
    print("[INFO] " + 'Process {} finished. {} files in {} s'.format(process_num, file_count, p_elapsed))

    # Let parent know
    global_queue.put((process_num, file_count))
    sys.exit(0)


def start_child(processes, global_queue, proj_paths, batch, project_format):
    # This is a blocking get. If the queue is empty, it waits
    pid, n_files_processed = global_queue.get()
    # OK, one of the processes finished. Let's get its data and kill it
    kill_child(processes, pid, n_files_processed)

    # Get a new batch of project paths ready
    paths_batch = proj_paths[:batch]
    del proj_paths[:batch]

    print("Starting new process {}".format(pid))
    p = Process(name='Process ' + str(pid), target=process_projects, args=(pid, paths_batch, processes[pid][1], global_queue, project_format))
    processes[pid][0] = p
    p.start()


def kill_child(processes, pid, n_files_processed):
    global file_count
    file_count += n_files_processed
    if processes[pid][0] is not None:
        processes[pid][0] = None
        processes[pid][1] += n_files_processed
        print("Process {} finished, {} files processed {}. Current total: {}".format(pid, n_files_processed, processes[pid][1], file_count))


def active_process_count(processes):
    return len([p for p in processes if p[0] is not None])


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: {} [MODE]".format(sys.argv[0]))
        print("Where MODE is zipblocks or folderblocks")
        exit(0)

    global project_format
    project_format = sys.argv[1]

    if project_format not in ['zipblocks', 'folderblocks']:
        print("MODE must be zipblocks or folderblocks")
        sys.exit()

    read_config()
    p_start = dt.datetime.now()

    prio_proj_paths = []
    if FILE_priority_projects is not None:
        with open(FILE_priority_projects) as f:
            for line in f:
                line_split = line[:-1].split(',')  # [:-1] to strip final character which is '\n'
                prio_proj_paths.append((line_split[0], line_split[4]))
        prio_proj_paths = zip(range(init_proj_id, len(prio_proj_paths) + init_proj_id), prio_proj_paths)

    proj_paths = []

    with open(FILE_projects_list) as f:
        for line in f:
            proj_paths.append(line.strip("\n"))
    proj_paths = list(zip(range(1, len(proj_paths) + 1), proj_paths))
    # it will diverge the process flow on process_file()

    if os.path.exists(PATH_stats_file_folder) or os.path.exists(PATH_bookkeeping_proj_folder) or os.path.exists(
            PATH_tokens_file_folder) or os.path.exists(PATH_logs):
        print('ERROR - Folder [{}] or [{}] or [{}] or [{}] already exists!'.format(PATH_stats_file_folder, PATH_bookkeeping_proj_folder, PATH_tokens_file_folder, PATH_logs))
        sys.exit(1)
    else:
        os.makedirs(PATH_stats_file_folder)
        os.makedirs(PATH_bookkeeping_proj_folder)
        os.makedirs(PATH_tokens_file_folder)
        os.makedirs(PATH_logs)

    # Split list of projects into N_PROCESSES lists
    # proj_paths_list = [ proj_paths[i::N_PROCESSES] for i in xrange(N_PROCESSES) ]

    # Multiprocessing with N_PROCESSES
    # [process, file_count]
    processes = [[None, init_file_id] for i in range(N_PROCESSES)]
    # Multiprocessing shared variable instance for recording file_id
    # file_id_global_var = Value('i', 1)
    # The queue for processes to communicate back to the parent (this process)
    # Initialize it with N_PROCESSES number of (process_id, n_files_processed)
    global_queue = Queue()
    for i in range(N_PROCESSES):
        global_queue.put((i, 0))

    # Start the priority projects
    print("*** Starting priority projects...")
    while len(prio_proj_paths) > 0:
        start_child(processes, global_queue, prio_proj_paths, 1, project_format)

    # Start all other projects
    print("*** Starting regular projects...")
    while len(proj_paths) > 0:
        start_child(processes, global_queue, proj_paths, PROJECTS_BATCH, project_format)

    print("*** No more projects to process. Waiting for children to finish...")
    while active_process_count(processes) > 0:
        pid, n_files_processed = global_queue.get()
        kill_child(processes, pid, n_files_processed)

    p_elapsed = dt.datetime.now() - p_start
    print("*** All done. %s files in %s" % (file_count, p_elapsed))
