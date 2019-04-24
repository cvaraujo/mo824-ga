import os
import sys

instances = ["020", "040", "060", "080", "100", "200", "400"]
mode = sys.argv[1]

for instance in instances:
	os.system("java -jar " + mode + ".jar " + instance + ">results/" + mode + "/" + instance)