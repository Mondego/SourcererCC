import sys
import MySQLdb

class DB:
    db = None

    def __init__(self, DB_name, DB_user, DB_pass, logging):
        self.DB_name = DB_name
        self.DB_user = DB_user
        self.DB_pass = DB_pass

        self.logging = logging

        self.connect()

    def connect(self):
        try:
            db = MySQLdb.connect(host    = "localhost", # your host, usually localhost
                                 user    = self.DB_user,   # your username
                                 passwd  = self.DB_pass,   # your password
                                 db      = self.DB_name)   # name of the data base

            self.db = db
        except Exception as e:
            self.logging.error('Error on DB.connect')
            self.logging.error(e)
            sys.exit(1)

    def commit(self):
        try:
            if self.db.is_connected():
                self.db.commit()
            else:
                self.connect()
                self.commit()
        except Exception as e:
            self.logging.error('Error on DB.commit')
            self.logging.error(e)

    def execute(self, sql_query):
        try:
            if self.db.is_connected():
                cursor = self.db.cursor()
                cursor.execute(sql_query)
                cursor.close()
                return cursor
            else:
                self.connect()
                self.execute(sql_query)
        except Exception as e:
            self.logging.error('Error on DB.execute')
            self.logging.error(sql_query)
            self.logging.error(e)

    def execute_and_fetchone(self, sql_query):
        try:
            if self.db.is_connected():
                cursor = self.db.cursor()
                cursor.execute(sql_query)
                res = cursor.fetchone()
                cursor.close()
                return res
            else:
                self.connect()
                self.execute_and_fetchone(sql_query)
        except Exception as e:
            self.logging.error('Error on DB.execute')
            self.logging.error(sql_query)
            self.logging.error(e)

    def execute_and_lastrowid(self, sql_query):
        try:
            if self.db.is_connected():
                cursor = self.db.cursor()
                cursor.execute(sql_query)
                res = cursor.lastrowid
                cursor.close()
                return res
            else:
                self.connect()
                self.execute_and_lastrowid(sql_query)
        except Exception as e:
            self.logging.error('Error on DB.execute')
            self.logging.error(sql_query)
            self.logging.error(e)

    def close(self):
        try:
            if self.db.is_connected():
                self.commit()
                self.db.close()
        except Exception as e:
            self.logging.error('Error on DB.close')
            self.logging.error(e)
