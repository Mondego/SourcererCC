from flask import Flask, render_template, request, Response
from werkzeug.utils import secure_filename
import os
import sqlite3
from sqlite3 import Error
from flask_table import Table, Col
from datetime import date
from werkzeug.wsgi import LimitedStream
import csv
from io import StringIO
app = Flask(__name__)


class StreamConsumingMiddleware(object):

    def __init__(self, app):
        self.app = app

    def __call__(self, environ, start_response):
      len=0
      if 'CONTENT_LENGTH' in environ.keys():
         len= environ['CONTENT_LENGTH']
      stream = LimitedStream(environ['wsgi.input'],int(len))
      environ['wsgi.input'] = stream
      app_iter = self.app(environ, start_response)
      try:
         stream.exhaust()
         for event in app_iter:
            yield event
      finally:
         if hasattr(app_iter, 'close'):
            app_iter.close()
app.wsgi_app = StreamConsumingMiddleware(app.wsgi_app)



database = r"/path/SourcererCC/WebApp/pythonsqlite.db"
res_list =[]

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
class Experiment:
   
    # init method or constructor 
    def __init__(self, e_id, name, config_file):
        self.name = name
        self.id = e_id 
        self.config_file = config_file
exp=Experiment('Default',1,'')

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


@app.route('/upload', methods=['GET'])
def upload_file():
   
   conn = create_connection(database)
   with conn:
      exp_config=get_experiments(conn)
   conn.close()
   return render_template('upload.html', exp_config=exp_config)
    
@app.route('/uploader', methods = ['GET', 'POST'])
def uploader_file():
   if request.method == 'POST':
      
      exp = request.form.get('exp')
      #ToDO: write the configuration file
      files = request.files.getlist('file')
      os.chdir("/path/SourcererCC/tokenizers/file-level/")
      write_projectList(files)

      os.system("python3 tokenizer.py zip") 
      os.system("cat files_tokens/* > blocks.file") 
      os.system("cp blocks.file /path/SourcererCC/clone-detector/input/dataset/")
      
      os.chdir("/path/SourcererCC/clone-detector")
      os.system("python controller.py")
      os.system("cat NODE_*/output*/query_* > results.pairs")
      
      test = renderingObject()
      #db_import()
      #print(test)
      os.system("./cleanup.sh")
      os.chdir("/path/SourcererCC/tokenizers/file-level/")
      os.system("rm blocks.file")
      os.system("./cleanup.sh ")
      

      #return render_template('uploader.html')

      return render_template('uploader.html',title='Clones Detected',test=test)

def write_projectList(files):
   my_file = open("/path/SourcererCC/tokenizers/file-level/project-list.txt", "w")
   for f in files:
      f.save(secure_filename(f.filename))
      my_file.write("/path/SourcererCC/tokenizers/file-level/"+f.filename)
      my_file.write("\n")

def renderingObject():
   results = open("/path/SourcererCC/clone-detector/results.pairs", "r")
   r = results.read()
   r = r.replace('\n',',')
   result_val= r.split(',')
   project_map = {}
   files_map ={}
   with open("/path/SourcererCC/tokenizers/file-level/bookkeeping_projs/bookkeeping-proj-0.projs", "r") as file:
      for line in file:
         project = line.split(',')
         if( project[0] not in project_map):
            project_map[project[0]]=project[1]
            files_map[project[0]]={}
   with open("/path/SourcererCC/tokenizers/file-level/files_stats/files-stats-0.stats", "r") as file:
      for line in file:
         
         file_data = line.split(',')
         if( file_data[0] in files_map):
            temp =files_map[file_data[0]]
            temp[file_data[1]]=file_data[3]

   

   #Putting in the result set
   for i in range(0,len(result_val)-3,4):
      temp=[]
      temp.append(project_map[result_val[i]])
      temp.append(files_map[result_val[i]][result_val[i+1]])
      temp.append(project_map[result_val[i+2]])
      temp.append(files_map[result_val[i+2]][result_val[i+3]])
      res_list.append(temp)

   return res_list

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

def db_import():
   conn = create_connection(database)
   project_ids={}
   file_ids ={}
   tokens_map = {}
   tokensCount_map = {}
   unique_tokens_count_map ={}
   files_map={}
   with conn:
      with open("/path/SourcererCC/tokenizers/file-level/bookkeeping_projs/bookkeeping-proj-0.projs", "r") as file:
         for line in file:
            project = line.split(',')
            project_db= (project[1],date.today())
            project_ids[project[0]] = create_project(conn,project_db)

      with open("/path/SourcererCC/tokenizers/file-level/blocks.file", "r") as file:
         for line in file:
            tokens= line.split(',')
            token_string = ''
            
            for i in range(4,len(tokens)):
               token_string+=str(tokens[i])
            '''tokens[0]=int(tokens[0])
            tokens[1]=int(tokens[1])
            if not tokens[1] in tokens_map[tokens[0]].keys:
               tokens_map[tokens[0]]={}
            if not tokens[1] in tokensCount_map[tokens[0]].keys:
               tokensCount_map[tokens[0]]={}
            if not tokens[1] in unique_tokens_count_map[tokens[0]].keys:
               unique_tokens_count_map[tokens[0]]={}
            '''
            if not tokens[0] in tokens_map.keys():
               tokens_map[tokens[0]]={}
            if not tokens[0] in tokensCount_map.keys():
               tokensCount_map[tokens[0]]={}
            if not tokens[0] in unique_tokens_count_map.keys():
               unique_tokens_count_map[tokens[0]]={}
            tokens_map[tokens[0]][tokens[1]]=token_string
            tokensCount_map[tokens[0]][tokens[1]]=tokens[2]
            unique_tokens_count_map[tokens[0]][tokens[1]]=tokens[3]

      with open("/path/SourcererCC/tokenizers/file-level/files_stats/files-stats-0.stats", "r") as file:
         for line in file:
            file_data = line.split(',')
            file_db = (exp.id,file_data[3],date.today(),file_data[2],tokensCount_map[file_data[0]][file_data[1]],tokens_map[file_data[0]][file_data[1]],project_ids[file_data[0]],unique_tokens_count_map[file_data[0]][file_data[1]])
            if not file_data[0] in file_ids.keys():
               file_ids[file_data[0]]={}
            file_ids[file_data[0]][file_data[1]]=create_file(conn,file_db)

      with open("/path/SourcererCC/clone-detector/results.pairs", "r") as file:
         for line in file:
            line = line.replace('\n',',')
            res = line.split(',')
            res_db=(file_ids[res[0]][res[1]],file_ids[res[2]][res[3]],date.today(),project_ids[res[0]],project_ids[res[2]])
            create_res(conn,res_db)

@app.route("/getCSV")
def getPlotCSV():
    # with open("outputs/Adjacency.csv") as fp:
    #     csv = fp.read()
    si = StringIO()
    cw = csv.writer(si)
    cw.writerows(res_list)
    return Response(
        si.getvalue(),
        mimetype="text/csv",
        headers={"Content-disposition":
                 "attachment; filename=myplot.csv"})

  
        
if __name__ == '__main__':
   app.run(debug = True)



