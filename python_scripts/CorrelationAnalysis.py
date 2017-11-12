import pandas as pd
import numpy as np
import time as time

file_path="D:/PhD/Clone/MLCC-Datasets/New-Train/consolidated_metrics.csv"

# colNames=["Id","System","Package","Class","Name","COMP","NOCL","NOS","HLTH","HVOC","HEFF","HBUG","CREF","XMET","LMET","NLOC","NOC","NOA","MOD","UniqueName",
#                   "HDIF","VDEC","EXCT","EXCR","CAST","TDN","HVOL","NAND","SimpleUniqueName","VREF","NOPR","MDN","NEXP","LOOP"]

dataset = pd.read_csv(file_path, header=0,delimiter="~~",engine='python')
dataset.drop(dataset.columns[[[0,1,2,3,4,19,28]]], axis=1, inplace=True)
print(dataset.head(5))
corr_matrix = dataset.corr()
# file_output=open('correlation.txt','w')
# file_output.write(corr_matrix)
# file_output.close()
np.savetxt("correlation.txt",corr_matrix,delimiter="\t",
           header="COMP\tNOCL\tNOS\tHLTH\tHVOC\tHEFF\tHBUG\tCREF\tXMET\tLMET\tNLOC\tNOC\tNOA\tMOD"
                  "\tHDIF\tVDEC\tEXCT\tEXCR\tCAST\tTDN\tHVOL\tNAND\tVREF\tNOPR\tMDN\tNEXP\tLOOP")