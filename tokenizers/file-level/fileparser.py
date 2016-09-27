import datetime as dt
import re
import os
import collections
import hashlib

def file_parser(file_string, file_bytes, comment_inline_pattern, comment_open_close_pattern, separators):

    final_stats = 'ERROR'
    final_tokens = 'ERROR'

    file_hash = 'ERROR'
    lines = 'ERROR'
    LOC = 'ERROR'
    SLOC = 'ERROR'

    h_time = dt.datetime.now()
    m = hashlib.md5()
    m.update(file_string)
    file_hash = m.hexdigest()
    hash_time = (dt.datetime.now() - h_time).microseconds

    lines = str(file_string.count('\n'))
    file_string = os.linesep.join( [s for s in file_string.splitlines() if s] )
    LOC = str(file_string.count('\n'))

    re_time = dt.datetime.now()
    # Remove tagged comments
    file_string = re.sub(comment_open_close_pattern, '', file_string, flags=re.DOTALL)
    # Remove end of line comments
    file_string = re.sub(comment_inline_pattern, '', file_string, flags=re.DOTALL)
    re_time = (dt.datetime.now() - re_time).microseconds

    SLOC = str(file_string.count('\n'))

    final_stats = ','.join([file_hash,file_bytes,lines,LOC,SLOC])

    # Rather a copy of the file string here for tokenization
    file_string_for_tokenization = file_string

    #Transform separators into spaces (remove them)
    s_time = dt.datetime.now()
    for x in separators:
        file_string_for_tokenization = file_string_for_tokenization.replace(x,' ')
    s_time = (dt.datetime.now() - s_time).microseconds

    ##Create a list of tokens
    file_string_for_tokenization = file_string_for_tokenization.split()
    ## Total number of tokens
    tokens_count_total = str(len(file_string_for_tokenization))
    ##Count occurrences
    file_string_for_tokenization = collections.Counter(file_string_for_tokenization)
    ##Converting Counter to dict because according to StackOverflow is better
    file_string_for_tokenization=dict(file_string_for_tokenization)
    ## Unique number of tokens
    tokens_count_unique = str(len(file_string_for_tokenization))

    t_time = dt.datetime.now()
    #SourcererCC formatting
    tokens = ','.join(['{}@@::@@{}'.format(k, v) for k,v in file_string_for_tokenization.iteritems()])
    t_time = (dt.datetime.now() - t_time).microseconds

    # MD5
    h_time = dt.datetime.now()
    m = hashlib.md5()
    m.update(tokens)
    hash_time += (dt.datetime.now() - h_time).microseconds

    final_tokens = ','.join([tokens_count_total,tokens_count_unique,m.hexdigest()+'@#@'+tokens])

    return (final_stats, final_tokens, [s_time, t_time, hash_time, re_time])

