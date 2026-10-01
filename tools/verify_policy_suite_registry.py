#!/usr/bin/env python3
"""Check static wiring from registered JavaExec entrypoints to policy-test classes.

Reject missing wiring, empty discovery and duplicate or ambiguous class identities. This is a
source-wiring check; native suite execution and assertion counts remain separate evidence.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def code_only(source: str) -> str:
    tokens = r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\x27(?:\\.|[^\x27\\])*\x27|//[^\n]*|/\*[\s\S]*?\*/'
    return re.sub(tokens, lambda match: ' ' * len(match.group()), source)


def main() -> int:
    gradle = (ROOT / 'build.gradle').read_text(encoding='utf-8')
    roots = set(re.findall(r"mainClass\s*=\s*['\"]([\w.]+)['\"]", gradle))
    candidates: dict[str, Path] = {}
    duplicates: list[tuple[str, Path, Path]] = []
    for src_set in sorted(ROOT.glob('src/*PolicyTest/java')):
        for file in sorted(src_set.rglob('*.java')):
            source = code_only(file.read_text(encoding='utf-8'))
            package = re.search(r'^\s*package ([\w.]+);', source, re.M)
            fqn = (package.group(1) + '.' if package else '') + file.stem
            if fqn in candidates:
                duplicates.append((fqn, candidates[fqn], file))
            else:
                candidates[fqn] = file
    if duplicates:
        print('verify_policy_suite_registry: FAIL — duplicate test class identities:')
        for fqn, first, second in duplicates:
            print(f'  - {fqn}: {first.relative_to(ROOT)} and {second.relative_to(ROOT)}')
        return 1
    if not candidates:
        print('verify_policy_suite_registry: FAIL — no policy-test classes discovered')
        return 1

    simple_to_fqn: dict[str, str] = {}
    for fqn in candidates:
        simple = fqn.rsplit('.', 1)[-1]
        if simple in simple_to_fqn:
            print(f'verify_policy_suite_registry: FAIL — ambiguous test reference name {simple}')
            return 1
        simple_to_fqn[simple] = fqn
    reached: set[str] = set()
    frontier = [fqn for fqn in roots if fqn in candidates]
    while frontier:
        fqn = frontier.pop()
        if fqn in reached:
            continue
        reached.add(fqn)
        source = code_only(candidates[fqn].read_text(encoding='utf-8'))
        for simple, target in simple_to_fqn.items():
            if target in reached or target == fqn:
                continue
            if re.search(rf'\b{re.escape(simple)}\.(?:main|run|runAll|verify)\s*\(', source):
                frontier.append(target)
    orphans = sorted(set(candidates) - reached)
    if orphans:
        print('verify_policy_suite_registry: FAIL — policy-test classes have no static entrypoint wiring:')
        for fqn in orphans:
            print(f'  - {fqn} ({candidates[fqn].relative_to(ROOT)})')
        return 1
    print(f'verify_policy_suite_registry: OK — {len(candidates)} policy-test classes statically wired '
          f'from {len(roots & set(candidates))} JavaExec roots; execution is verified by the native suites.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
