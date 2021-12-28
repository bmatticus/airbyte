#
# Copyright (c) 2021 Airbyte, Inc., all rights reserved.
#
import os

from ..build_static_checkers_reports import build_static_checkers_reports


def test_build_static_checkers_reports() -> None:
    changed_module_path = "tools/ci_static_check_reports"
    build_static_checkers_reports([changed_module_path])

    assert os.path.exists(changed_module_path)
    assert os.path.exists(os.path.join(changed_module_path, "black.txt"))
    assert os.path.exists(os.path.join(changed_module_path, "coverage.xml"))
    assert os.path.exists(os.path.join(changed_module_path, "flake.xml"))
    assert os.path.exists(os.path.join(changed_module_path, "isort.txt"))
    assert os.path.exists(os.path.join(changed_module_path, "cobertura.xml"))
    assert os.path.exists(os.path.join(changed_module_path, "pytest.xml"))
