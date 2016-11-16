import re
import os
import collections
import sys
import unittest
import tokenizer
import hashlib

try:
    from configparser import ConfigParser
except ImportError:
    from ConfigParser import ConfigParser # ver. < 3.0

    config = ConfigParser()
    # parse existing file
    try:
        config.read('config.ini')
    except IOError:
        print 'ERROR - Config settings not found. Usage: $python this-script.py config.ini'
        sys.exit()

separators = config.get('Language', 'separators').strip('"').split(' ')
comment_inline = config.get('Language', 'comment_inline')
comment_inline_pattern = comment_inline + '.*?$'
comment_open_tag = re.escape(config.get('Language', 'comment_open_tag'))
comment_close_tag = re.escape(config.get('Language', 'comment_close_tag'))
comment_open_close_pattern = comment_open_tag + '.*?' + comment_close_tag

REGEX = re.compile('\w+@@::@@+\d')

class TestParser(unittest.TestCase):

    #Input is something like: @#@print@@::@@1,include@@::@@1,sys@@::@@1
    def assert_common_properties(self, list_tokens_string):
        self.assertTrue(list_tokens_string.startswith('@#@'))

        if len(list_tokens_string) > 3:
            split = list_tokens_string[3:].split(',')
            for pair in split:
                self.assertTrue(REGEX.match(pair))

    def test_line_counts_1(self):
        input = """ line 1
                    line 2
                    line 3 """
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats

        self.assertEqual(lines,3)
        self.assertEqual(LOC,3)
        self.assertEqual(SLOC,3)

    def test_line_counts_2(self):
        input = """ line 1
                    line 2
                    line 3
"""
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats

        self.assertEqual(lines,3)
        self.assertEqual(LOC,3)
        self.assertEqual(SLOC,3)

    def test_line_counts_3(self):
        input = """ line 1

                    // line 2
                    line 3 
                """
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats

        self.assertEqual(lines,5)
        self.assertEqual(LOC,3)
        self.assertEqual(SLOC,2)

    def test_comments(self):
        input = "// Hello\n // World"
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats
        (tokens_count_total,tokens_count_unique,token_hash,tokens) = final_tokens


        self.assertEqual(lines,2)
        self.assertEqual(LOC,2)
        self.assertEqual(SLOC,0)

        self.assertEqual(tokens_count_total,0)
        self.assertEqual(tokens_count_unique,0)
        self.assert_common_properties(tokens)

    def test_multiline_comment(self):
        input = '/* this is a \n comment */ /* Last one */ '
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats
        (tokens_count_total,tokens_count_unique,token_hash,tokens) = final_tokens


        self.assertEqual(lines,2)
        self.assertEqual(LOC,2)
        self.assertEqual(SLOC,0)

        self.assertEqual(tokens_count_total,0)
        self.assertEqual(tokens_count_unique,0)
        self.assert_common_properties(tokens)

    def test_simple_file(self):
        input = """#include GLFW_INCLUDE_GLU
                   #include <GLFW/glfw3.h>
                   #include <cstdio>
                   
                   /* Random function */
                   static void glfw_key_callback(int key, int scancode, int action, int mod){
                     if(glfw_key_callback){
                       // Comment here
                       input_event_queue->push(inputaction);   
                     }
                   }"""
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats
        (tokens_count_total,tokens_count_unique,token_hash,tokens) = final_tokens

        self.assertEqual(lines,11)
        self.assertEqual(LOC,10)
        self.assertEqual(SLOC,8)

        self.assertEqual(tokens_count_total,24)
        self.assertEqual(tokens_count_unique,18)
        self.assert_common_properties(tokens)

        hard_tokens = set(['int@@::@@4','void@@::@@1','cstdio@@::@@1','action@@::@@1','static@@::@@1','key@@::@@1','glfw_key_callback@@::@@1','mod@@::@@1','if@@::@@1','glfw3@@::@@1','scancode@@::@@1','h@@::@@1','GLFW_INCLUDE_GLU@@::@@1','input_event_queue@@::@@2','GLFW@@::@@1','push@@::@@1','inputaction@@::@@1','include@@::@@3'])
        this_tokens = set(tokens[3:].split(','))
        self.assertTrue(len(hard_tokens - this_tokens),0)

        m = hashlib.md5()
        m.update(tokens[3:])
        self.assertEqual(m.hexdigest(),token_hash)

if __name__ == '__main__':
    unittest.main()

