import re
import os
import collections
import sys
import unittest
import tokenizer

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

class TestParser(unittest.TestCase):

    #Input is something like: @#@print@@::@@1,include@@::@@1,sys@@::@@1
    def assert_common_properties(self, list_tokens_string):
        self.assertTrue(list_tokens_string.startswith('@#@'))

        if len(list_tokens_string) > 3:
            split = list_tokens_string[3:].split(',')
            REGEX = re.compile('\w+@@::@@+\d')
            for pair in split:
                self.assertTrue(REGEX.match(pair))

    def test_comments(self):
        input = "/* this is a // comment */ /* Lala */ // PORTUGAL"
        (final_stats, final_tokens, file_times) = tokenizer.tokenize(input, comment_inline_pattern, comment_open_close_pattern, separators)
        (file_hash,lines,LOC,SLOC) = final_stats
        (tokens_count_total,tokens_count_unique,token_hash,tokens) = final_tokens
        print tokens

        self.assert_common_properties(tokens)

        self.assertEqual(lines,0)
        self.assertEqual(LOC,0)
        self.assertEqual(SLOC,0)

        self.assertEqual(tokens_count_total,0)
        self.assertEqual(tokens_count_unique,0)
        self.assertEqual(len(tokens),3)

    def test_multiline_comment(self):
        #input = "/* first line\n second line\n", '0')
        self.assertTrue(True)

if __name__ == '__main__':
    unittest.main()

