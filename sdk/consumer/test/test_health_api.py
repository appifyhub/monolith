# coding: utf-8

"""
    Appify Hub's Consumer API

    The full specification of the service's API used by the end-users.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from appifyhub.api.health_api import HealthApi


class TestHealthApi(unittest.TestCase):
    """HealthApi unit test stubs"""

    def setUp(self) -> None:
        self.api = HealthApi()

    def tearDown(self) -> None:
        pass

    def test_heartbeat(self) -> None:
        """Test case for heartbeat

        Check the heartbeat
        """
        pass


if __name__ == '__main__':
    unittest.main()
