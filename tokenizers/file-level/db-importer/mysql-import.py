#Usage $python this_script.py username passwd schema folder-result-of-tokenizer [list-of-pairs-from-CC]

import sys, os, csv
from db import DB
import logging

pattern = r'\"(.+?)\"'

def import_tokenizer_output(db, output_path, logging):
    bookkeeping_file_path = os.path.join(output_path,'bookkeeping_projs')
    files_stats_path      = os.path.join(output_path,'files_stats')
    files_tokens_path     = os.path.join(output_path,'files_tokens')

    try:
        logging.info('## Warming up token values')
        token_info = {}
        for file in os.listdir(files_tokens_path):
            if file.endswith('.tokens'):
                file = os.path.join(files_tokens_path,file)
                logging.info('Getting info from '+file)
                with open(file, 'r') as csvfile:
                    for line in csvfile:
                        pid, fid, total_tokens, unique_tokens, thash = line.split('@#@')[0].split(',')
                        token_info[fid] = [total_tokens, unique_tokens, thash]

        logging.info('## Import into database')

        logging.info('## Importing files and stats')
        # Insert files and stats
        for file in os.listdir(files_stats_path):
            if file.endswith('.stats'):
                file = os.path.join(files_stats_path,file)
                logging.info('Importing from '+file)
                with open(file, 'r') as csvfile:
                    for entry in csvfile:
                        entry_split = (entry[:-1]).split(',')
                        if len(entry_split) == 9: 
                            proj_id, file_id, path, url, file_hash, bytess, lines, loc, sloc = entry_split

                        else: # Something went wrong with parsing
                            proj_id = entry_split[0]; del entry_split[0]
                            file_id = entry_split[0]; del entry_split[0]

                            sloc      = entry_split[-1:][0]; del entry_split[-1:]
                            loc       = entry_split[-1:][0]; del entry_split[-1:]
                            lines     = entry_split[-1:][0]; del entry_split[-1:]
                            bytess    = entry_split[-1:][0]; del entry_split[-1:]
                            file_hash = entry_split[-1:][0]; del entry_split[-1:]

                            if (len(entry_split) % 2 != 0):
                                logging.error('Problems parsing file: '+str(entry_split))
                                continue
                            else:
                                path = ','.join(entry_split[:len(entry_split)/2])
                                url  = ','.join(entry_split[len(entry_split)/2:])

                                logging.warning('String partitioned into:'+file_id+'|'+proj_id+path+'|'+url+'|'+file_hash+'|'+bytess+'|'+lines+'|'+loc+'|'+sloc)

                        path = path.strip('"')
                        url = url.strip('"')
                        file_hash = file_hash.strip('"')

                        db.insert_file(file_id, proj_id, path, url, file_hash)
                        db.insert_stats_ignore_repetition(file_hash, bytess, lines, loc, sloc, token_info[file_id][0], token_info[file_id][1], token_info[file_id][2])

        db.flush_files_and_stats()

        logging.info('## Importing projects')
        # Insert projects 
        for file in os.listdir(bookkeeping_file_path):
            if file.endswith('.projs'):
                file = os.path.join(bookkeeping_file_path,file)
                logging.info('Importing from '+file)
                with open(file, 'r') as csvfile:
                    for line in csvfile:
                        entry_split = (line[:-1]).split(',')

                        proj_id = entry_split[0]
                        del entry_split[0]

                        if(len(entry_split) == 2):
                            db.insert_project(proj_id,entry_split[0][1:-1],entry_split[1][1:-1])
                        else:
                            if (len(entry_split) % 2 != 0):
                                logging.warning('Problems parsing project: '+str(entry_split))
                                path = ','.join(entry_split[:len(entry_split)/2])
                                path = path[1:-1]
                                url  = ','.join(entry_split[len(entry_split)/2:])
                                url = url[1:-1]
                                logging.warning('String partitioned into:'+proj_id+'|'+path+'|'+url)
                                logging.warning('Previous warning was solved')
                                db.insert_project(proj_id,path,url)
                            else:
                                logging.error('Problems parsing project: '+str(entry_split))


    except Exception as e:
        logging.error('Error accessing Database')
        logging.error(e)
        sys.exit(1)

def import_pairs(db, pairs_path):
    try:

        commit_interval = 1000
        pair_number = 0

        print '## Importing pairs from',pairs_path
        with open(pairs_path, 'r') as file:
            for line in file:
                pair_number += 1
                line_split = line[:-1].split(',')

                db.insert_CCPairs(line_split[0], line_split[1], line_split[2], line_split[3])

                if pair_number%commit_interval == 0:
                    print '    ',pair_number,'pairs committed'

    except Exception as e:
        print 'Error accessing Database'
        print e
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) == 3:
        logging.error('Usage: mysql-import.py user passwd database')
        sys.exit(1)

    user  = sys.argv[1]
    passw = sys.argv[2]
    DB_name = sys.argv[3]

    log_path = 'LOG-db-importer.log'

    if os.path.isfile(log_path):
        print 'ERROR: Log file:',log_path,'already exists'
        sys.exit(1)

    FORMAT = '[%(levelname)s] (%(threadName)s) %(message)s'
    logging.basicConfig(level=logging.DEBUG,format=FORMAT)
    file_handler = logging.FileHandler(log_path)
    file_handler.setFormatter(logging.Formatter(FORMAT))
    logging.getLogger().addHandler(file_handler)


    if len(sys.argv) >= 5:
        output_path = sys.argv[4]
    if len(sys.argv) >= 6:
        pairs_path  = sys.argv[5]
    
    try:
        db_object = DB(user, DB_name, passw, logging)

        logging.info('Starting DB: '+DB_name+' with '+user+':'+passw)

        if len(sys.argv) >= 4:
            logging.info('### Creating Tables')
            db_object = DB(user, DB_name, passw, logging)
        if len(sys.argv) >= 5:
            logging.info('### Importing output from tokenizer')
            import_tokenizer_output(db_object,output_path,logging)
        if len(sys.argv) >= 6:
            logging.info('### Importing output from tokenizer')
            #import_pairs(pairs_path)
        
        db_object.close()

    except Exception as e:
        print 'Error on __main__'
        print e
    




