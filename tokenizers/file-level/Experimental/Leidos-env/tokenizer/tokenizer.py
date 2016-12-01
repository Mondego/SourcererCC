import logging
import multiprocessing
import re
import os
import collections
import tarfile
import sys
import hashlib
import datetime
import MySQLdb

class Tokenizer(object):

    PROJECT_ID = 1

    def __init__(self, DB_user, DB_pass, DB_name, logging):
        try:
            db = MySQLdb.connect(host="localhost", # your host, usually localhost
                                 db=DB_name,
                                 user=DB_user,     # your username
                                 passwd=DB_pass)   # your password
            cursor = db.cursor()

            cursor.execute("SELECT Max(projectId) FROM projects;")
            (PROJECT_ID, ) = cursor.fetchone()
            if PROJECT_ID is None:
                PROJECT_ID = 0
    
            PROJECT_ID += 1

        except Exception as e:
            print 'Error on Tokenizer.__init__'
            print e
            sys.exit(1)

        finally:
            cursor.close()
            db.close()

        logging.info('Tokenizer successfully initialized. Project index starting at %s' % (PROJECT_ID))


    def execute(self):
        print ('Tokenizer - execute')
