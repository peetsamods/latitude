from __future__ import annotations

import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location('suite_registry_gate', Path(__file__).resolve().parents[1] / 'verify_policy_suite_registry.py')
gate = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(gate)


class RegistryGateTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name)
        self.previous_root = gate.ROOT
        gate.ROOT = self.root
        (self.root / 'build.gradle').write_text("mainClass = 'sample.RootTest'\n")

    def tearDown(self):
        gate.ROOT = self.previous_root
        self.temporary.cleanup()

    def source(self, suite, name, body, package='sample'):
        file = self.root / 'src' / suite / 'java' / package.replace('.', '/') / (name + '.java')
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text('package ' + package + ';\npublic class ' + name + ' {\n' + body + '\n}\n')

    def run_gate(self):
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            code = gate.main()
        return code, output.getvalue()

    def test_registered_root_and_transitive_entries_pass(self):
        self.source('onePolicyTest', 'RootTest', 'static void main() { MiddleTest.run(); }')
        self.source('onePolicyTest', 'MiddleTest', 'static void run() { LeafTest.verify(); }')
        self.source('onePolicyTest', 'LeafTest', 'static void verify() {}')
        self.assertEqual(self.run_gate()[0], 0)

    def test_duplicate_full_name_fails_instead_of_overwriting(self):
        self.source('onePolicyTest', 'RootTest', 'static void main() {}')
        self.source('twoPolicyTest', 'RootTest', 'static void main() {}')
        code, output = self.run_gate()
        self.assertEqual(code, 1)
        self.assertIn('duplicate test class identities', output)

    def test_empty_discovery_is_not_green(self):
        self.assertEqual(self.run_gate()[0], 1)

    def test_unwired_class_fails(self):
        self.source('onePolicyTest', 'RootTest', 'static void main() {}')
        self.source('onePolicyTest', 'OrphanTest', 'static void run() {}')
        self.assertEqual(self.run_gate()[0], 1)

    def test_comments_and_strings_do_not_create_wiring(self):
        for reference in ['// OrphanTest.run();', 'String example = "OrphanTest.run();";', '/* OrphanTest.run(); */']:
            with self.subTest(reference=reference):
                self.source('onePolicyTest', 'RootTest', 'static void main() {\n' + reference + '\n}')
                self.source('onePolicyTest', 'OrphanTest', 'static void run() {}')
                self.assertEqual(self.run_gate()[0], 1)

    def test_helper_call_does_not_count_as_test_entrypoint(self):
        self.source('onePolicyTest', 'RootTest', 'static void main() { OrphanTest.fixture(); }')
        self.source('onePolicyTest', 'OrphanTest', 'static void run() {} static void fixture() {}')
        self.assertEqual(self.run_gate()[0], 1)


if __name__ == '__main__':
    unittest.main()
