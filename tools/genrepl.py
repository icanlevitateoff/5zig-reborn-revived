#!/usr/bin/env python3
"""Regenerates the stonecutter replacements block in build.gradle.kts from tools/replacements.txt."""
import re, sys
root = sys.argv[1]
rows = [l.split('|') for l in open(f'{root}/tools/replacements.txt').read().split('\n') if l.strip() and not l.startswith('#')]
def lit(p): return re.sub(r'\\(.)', r'\1', p.replace(r'\b', ''))
def k(x): return '"' + x.replace('\\', '\\\\').replace('"', '\\"').replace('$', '\\$') + '"'
out = []
for c, a, b in rows:
    rev = re.sub(r'\\([^\w.()\[\]])', r'\1', re.escape(b))
    rev = (r'\b' if re.match(r'\w', b) else '') + rev + (r'\b' if re.search(r'\w$', b) else '')
    assert re.sub(rev, lit(a), re.sub(a, b, lit(a))) == lit(a), a
    out.append(f'        regex(eval(current.version, "{c}")) {{\n            replace({k(a)}, {k(b)})\n            reverse({k(rev)}, {k(lit(a))})\n        }}')
p = f'{root}/build.gradle.kts'; s = open(p).read()
start = s.index('    replacements {\n') + len('    replacements {\n')
end = s.index('\n    }\n}\n', start)
s = s[:start] + '\n'.join(out) + s[end:]
open(p, 'w').write(s)
print(len(rows), 'replacements')
