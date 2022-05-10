from flask import Flask, render_template, request, Response
from werkzeug.utils import secure_filename
import os
import sqlite3
from sqlite3 import Error
# from flask_table import Table, Col
from datetime import date
from werkzeug.wsgi import LimitedStream
import csv
from io import StringIO
import configurationData as configPath
import db_helper as db_h

# STATIC_DIR = os.path.abspath('../static')
# app = Flask(__name__, static_folder=STATIC_DIR)
app = Flask(__name__)
# Not needed. Only for development
app.config["TEMPLATES_AUTO_RELOAD"] = True


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



database = configPath.sourcerer_path + "/WebApp/pythonsqlite.db"
res_list =[]

exp=db_h.Experiment('Default',1,'')

@app.route('/upload', methods=['GET'])
def upload_file():
   conn = db_h.create_connection(database)
   with conn:
      exp_config=db_h.get_experiments(conn)
   conn.close()
   return render_template('upload.html', exp_config=exp_config)
    
@app.route('/uploader', methods = ['GET', 'POST'])
def uploader_file():
   try:
      if request.method == 'POST':

         exp = request.form.get('exp')
         #ToDO: write the configuration file
         files = request.files.getlist('file')

         os.chdir(configPath.sourcerer_path)
         os.chdir("tokenizers/file-level")
         write_projectList(files)

         return_code = os.system(configPath.run_environment+" tokenizer.py zip")
         test = []
         if(return_code != 0):
            return render_template('error_page.html',title='Error Handling', msg="Got error code = "+str(return_code)+", or the tokenizer might be in use!")
         else:
            os.system("cat files_tokens/* > blocks.file")
            os.chdir(configPath.sourcerer_path)
            os.system("cp ./tokenizers/file-level/blocks.file ./clone-detector/input/dataset/")

            os.chdir("clone-detector")
            os.system(configPath.run_environment+" controller.py")
            os.system("cat NODE_*/output*/query_* > results.pairs")

            test = renderingObject()
            #db_import()
            #print(test)
            clean_up()
   except FileNotFoundError as e:
      clean_up()
      return render_template('error_page.html',title='Error Handling', msg=e)

      #return render_template('uploader.html')
   return render_template('uploader.html',title='Clones Detected',test=test)

def clean_up():
   res_list =[]
   os.system("bash ./cleanup.sh")
   if ("clone-detector" in os.getcwd()):
      os.chdir("../tokenizers/file-level/")
   else:
      os.chdir(configPath.sourcerer_path)
      os.chdir("tokenizers/file-level")
   os.system("rm blocks.file")
   os.system("bash ./cleanup.sh ")

def write_projectList(files):
   try:
      if(not os.path.exists('data')):
             os.mkdir('data')
      prev_path = os.getcwd()
      os.chdir('data')
      my_file = open(configPath.project_list_path, "w")
      for f in files:
         f.save(secure_filename(f.filename))
         my_file.write(os.path.join(os.getcwd(),f.filename))
         my_file.write("\n")
      os.chdir(prev_path)
   except FileNotFoundError:
      raise FileNotFoundError("write_projectList():: file path doesn't exist.")

def renderingObject():
   try:
      results = open(configPath.result_pair_path, "r")
      r = results.read()
      r = r.replace('\n',',')
      result_val= r.split(',')
      project_map = {}
      files_map ={}
      with open(configPath.sourcerer_path + "/tokenizers/file-level/bookkeeping_projs/bookkeeping-proj-0.projs", "r") as file:
         for line in file:
            project = line.split(',')
            if( project[0] not in project_map):
               project_map[project[0]]=project[1]
               files_map[project[0]]={}
      with open(configPath.sourcerer_path + "/tokenizers/file-level/files_stats/files-stats-0.stats", "r") as file:
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
   except FileNotFoundError:
      raise FileNotFoundError("renderingObject():: file path doesn't exist.")

   return res_list

def db_import():
   conn = db_h.create_connection(database)
   project_ids={}
   file_ids ={}
   tokens_map = {}
   tokensCount_map = {}
   unique_tokens_count_map ={}
   files_map={}
   try:
      with conn:
         with open(configPath.sourcerer_path + "/tokenizers/file-level/bookkeeping_projs/bookkeeping-proj-0.projs", "r") as file:
            for line in file:
               project = line.split(',')
               project_db= (project[1],date.today())
               project_ids[project[0]] = db_h.create_project(conn,project_db)

         with open(configPath.sourcerer_path + "/tokenizers/file-level/blocks.file", "r") as file:
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

         with open(configPath.sourcerer_path + "/tokenizers/file-level/files_stats/files-stats-0.stats", "r") as file:
            for line in file:
               file_data = line.split(',')
               file_db = (exp.id,file_data[3],date.today(),file_data[2],tokensCount_map[file_data[0]][file_data[1]],tokens_map[file_data[0]][file_data[1]],project_ids[file_data[0]],unique_tokens_count_map[file_data[0]][file_data[1]])
               if not file_data[0] in file_ids.keys():
                  file_ids[file_data[0]]={}
               file_ids[file_data[0]][file_data[1]]=db_h.create_file(conn,file_db)

         with open(configPath.result_pair_path, "r") as file:
            for line in file:
               line = line.replace('\n',',')
               res = line.split(',')
               res_db=(file_ids[res[0]][res[1]],file_ids[res[2]][res[3]],date.today(),project_ids[res[0]],project_ids[res[2]])
               db_h.create_res(conn,res_db)
   except FileNotFoundError:
      raise FileNotFoundError("db_import():: file path doesn't exist.")

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
