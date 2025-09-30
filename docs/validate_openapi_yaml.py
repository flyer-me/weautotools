import yaml,sys,glob
paths=glob.glob('docs/openapi/**/*.yaml',recursive=True)+['docs/openapi.yaml']
paths=list(set(paths))
ok=True
for p in sorted(paths):
    try:
        with open(p,'r',encoding='utf-8') as fh:
            yaml.safe_load(fh)
        print(p+" -> PARSE OK")
    except Exception as e:
        print(p+" -> PARSE ERROR: "+str(e))
        ok=False
if not ok:
    sys.exit(2)
print('ALL PARSE OK')
