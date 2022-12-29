import os
import sys

socos_dir = os.path.dirname(os.path.realpath(__file__))
parsetab_dir = os.path.join(socos_dir,"parsetabs")
base_dir = os.path.dirname(socos_dir)

sys.path.append(base_dir)
sys.path.append(parsetab_dir)


if __name__=="__main__":
    # print PYTHONPATH value
    print "%s:%s"%(base_dir,parsetab_dir)
