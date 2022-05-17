import sqlite3
from sqlite3 import Error

class Experiment:
    # init method or constructor 
    def __init__(self, e_id, name, config_file):
        self.name = name
        self.id = e_id 
        self.config_file = config_file

def create_connection(db_file):
    """ create a database connection to the SQLite database
        specified by db_file
    :param db_file: database file
    :return: Connection object or None
    """
    conn = None
    try:
        conn = sqlite3.connect(db_file)
        return conn
    except Error as e:
        print(e)

    return conn

def create_project(conn, project):
    """
    Create a new project into the projects table

    :param project:
    :return: project id
    """
    sql = ''' INSERT INTO project(name,date_added)
              VALUES(?,?) '''
    cur = conn.cursor()
    cur.execute(sql, project)
    conn.commit()
    return cur.lastrowid

def create_file(conn, file):
    """
    Create a new file into the file table
   
    """
    sql = ''' INSERT INTO file(experiment_id, name, date_added ,path_text,token_count,tokens, project_id , unique_token_count)
              VALUES(?,?,?,?,?,?,?,?) '''
    cur = conn.cursor()
    cur.execute(sql, file)
    conn.commit()
    return cur.lastrowid

def create_res(conn, res):
    """
    Create a new clone results into the clone_detection table
    
    """
    sql = ''' INSERT INTO clone_detection(file_id_1,file_id_2,date_added,project_id1,project_id2)
              VALUES(?,?,?,?,?) '''
    cur = conn.cursor()
    cur.execute(sql, res)
    conn.commit()
    return cur.lastrowid

def get_experiments(conn):
    """
    Create a new project into the projects table
    :param conn:
    :param project:
    :return: project id
    """
    sql = ''' SELECT id, name, config_file FROM experiment '''
    cur = conn.cursor()
    cur.execute(sql)
    rows = cur.fetchall()
    
    exp_list=[]
    for row in rows:
      temp_exp = Experiment(row[0],row[1],row[2])
      exp_list.append(temp_exp)

    return exp_list