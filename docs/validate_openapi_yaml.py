import sys
import yaml
p = r'f:/User_Data/Code/weautotools/docs/openapi_v2.yaml'
try:
    with open(p, 'r', encoding='utf-8') as f:
        yaml.safe_load(f)
    print('YAML PARSE: OK')
except Exception as e:
    print('YAML PARSE: ERROR')
    print(e)
    sys.exit(1)
