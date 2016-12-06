import sys
import MySQLdb

class DB:
    db = None
    cursor = None

    def __init__(self, DB_name, DB_user, DB_pass, logging):
        self.logging = logging
        self.DB_name = DB_name
        self.DB_user = DB_user
        self.DB_pass = DB_pass

        self.connect()

    def connect(self):
        try:
            db = MySQLdb.connect(host    = "localhost", # your host, usually localhost
                                 user    = self.DB_user,   # your username
                                 passwd  = self.DB_pass,   # your password
                                 db      = self.DB_name)   # name of the data base

            self.db = db
            self.cursor = db.cursor()
        except Exception as e:
            self.logging.error('Error on DB.connect')
            self.logging.error(e)
            sys.exit(1)

    def commit(self):
        try:
            self.db.commit()
        except:
            self.connect()
            self.commit()

    def execute(self, sql_query):
        try:
            self.cursor.execute(sql_query)
            return self.cursor
        except:
            self.connect()
            self.execute(sql_query)

    def lastrowid(self):
        return self.cursor.lastrowid

    def fetchone(self):
        return self.cursor.fetchone()

    def close(self):
        try:
            self.cursor.close()
            self.db.commit()
            self.db.close()
        except:
            self.connect()
            self.close()
